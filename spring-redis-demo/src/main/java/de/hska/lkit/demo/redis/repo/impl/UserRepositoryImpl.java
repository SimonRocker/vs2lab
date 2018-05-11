package de.hska.lkit.demo.redis.repo.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands.Range;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;

import de.hska.lkit.demo.redis.model.User;
import de.hska.lkit.demo.redis.model.Post;
import de.hska.lkit.demo.redis.model.Token;
import de.hska.lkit.demo.redis.repo.UserRepository;

/**
 * @author knad0001
 *
 */
/**
 * @author knad0001
 *
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

	/**
	 *
	 */
	private static final String KEY_SET_ALL_USERNAMES = "all:usernames";

	private static final String KEY_SET_ALL_TOKENS = "all:tokens";

	private static final String KEY_SET_ALL_POSTS = "all:posts";

	private static final String KEY_ZSET_ALL_USERNAMES = "all:usernames:sorted";

	private static final String KEY_HASH_ALL_USERS = "all:user";

	private static final String KEY_HASH_ALL_POSTS = "all:post";

	private static final String KEY_PREFIX_USER = "user:";

	private static final String KEY_PREFIX_POST = "post:";

	/**
	 * to generate unique ids for user
	 */
	private RedisAtomicLong userid;

	private RedisAtomicLong postid;
	/**
	 * to save data in String format
	 */
	private StringRedisTemplate stringRedisTemplate;

	/**
	 * to save user data as object
	 */
	private RedisTemplate<String, Object> redisTemplate;


	/**
	 * hash operations for stringRedisTemplate
	 */
	private HashOperations<String, String, String> srt_hashOps;

	/**
	 * set operations for stringRedisTemplate
	 */
	private SetOperations<String, String> srt_setOps;

	/**
	 * zset operations for stringRedisTemplate
	 */
	private ZSetOperations<String, String> srt_zSetOps;


	/**
	 * hash operations for redisTemplate
	 */
	@Resource(name = "redisTemplate")
	private HashOperations<String, String, User> rt_hashOps;

	/**
	 * hash operations for redisTemplate
	 */
	@Resource(name = "redisTemplate")
	private HashOperations<String, String, Post> rt_hashOps_post;




	/*
	 *
	 */
	@Autowired
	public UserRepositoryImpl(RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
		this.redisTemplate = redisTemplate;
		this.stringRedisTemplate = stringRedisTemplate;
		this.userid = new RedisAtomicLong("userid", stringRedisTemplate.getConnectionFactory());
	}


	@PostConstruct
	private void init() {
		srt_hashOps = stringRedisTemplate.opsForHash();
		srt_setOps = stringRedisTemplate.opsForSet();
		srt_zSetOps = stringRedisTemplate.opsForZSet();
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * hska.iwi.vslab.repo.UserRepository#saveUser(hska.iwi.vslab.model.User)
	 */
	@Override
	public void saveUser(User user) {
		// generate a unique id
		String id = String.valueOf(userid.incrementAndGet());

		user.setId(id);

		// to show how objects can be saved
		// be careful, if username already exists it's not added another time
		String key = KEY_PREFIX_USER + user.getUsername();
		srt_hashOps.put(key, "id", id);
		srt_hashOps.put(key, "firstName", user.getFirstname());
		srt_hashOps.put(key, "lastName", user.getLastname());
		srt_hashOps.put(key, "username", user.getUsername());
		srt_hashOps.put(key, "password", user.getPassword());

		// the key for a new user is added to the set for all usernames
		srt_setOps.add(KEY_SET_ALL_USERNAMES, user.getUsername());

		// the key for a new user is added to the sorted set for all usernames
		srt_zSetOps.add(KEY_ZSET_ALL_USERNAMES, user.getUsername(), 0);

		// to show how objects can be saved
		rt_hashOps.put(KEY_HASH_ALL_USERS, key, user);

	}

	@Override
	public Map<String, User> getAllUsers() {
		return rt_hashOps.entries(KEY_HASH_ALL_USERS);
	}


	@Override
	public User getUser(String username) {
		User user = new User();

		// if username is in set for all usernames, 
		if (srt_setOps.isMember(KEY_SET_ALL_USERNAMES, username)) {

			// get the user data out of the hash object with key "'user:' + username"
			String key = "user:" + username;
			user.setId(srt_hashOps.get(key, "id"));
			user.setFirstname(srt_hashOps.get(key, "firstName"));
			user.setLastname(srt_hashOps.get(key, "lastName"));
			user.setUsername(srt_hashOps.get(key, "username"));
			user.setPassword(srt_hashOps.get(key, "password"));
		} else
			user = null;
		return user;
	}


	@Override
	public Map<String, User> findUsersWith(String pattern) {

		System.out.println("Searching for pattern  " + pattern);

		Set<byte[]> result = null;
		Map<String, User> mapResult = new HashMap<String, User>();

		if (pattern.equals("")) {

			// get all user
			mapResult = rt_hashOps.entries(KEY_HASH_ALL_USERS);

		} else {
			// search for user with pattern

			char[] chars = pattern.toCharArray();
			chars[pattern.length() - 1] = (char) (chars[pattern.length() - 1] + 1);
			String searchto = new String(chars);

			Set<String> sresult = srt_zSetOps.rangeByLex(KEY_ZSET_ALL_USERNAMES, Range.range().gte(pattern).lt(searchto));
			for (Iterator iterator = sresult.iterator(); iterator.hasNext(); ) {
				String username = (String) iterator.next();
				System.out.println("key found: " + username);
				User user = (User) rt_hashOps.get(KEY_HASH_ALL_USERS, KEY_PREFIX_USER + username);

				mapResult.put(user.getUsername(), user);
			}

		}

		return mapResult;

	}

	@Override
	public boolean logInUser(String username, String password) {
		User user = getUser(username);
		if (user == null) return false;
		return user.getPassword() == password;
	}

	@Override
	public boolean checkIfUserIsLoggedIn(User user, int ip) {
		// if the ip is in set for all tokens return true
		if (srt_setOps.isMember(KEY_SET_ALL_USERNAMES, String.valueOf(ip)))
			return true;

		return false;
	}

	@Override
	public Map<String, Post> getAllPosts() {
		return rt_hashOps_post.entries(KEY_HASH_ALL_POSTS);
	}

	/**
	 *
	 * Gets you all posts of the wanted users
	 *
	 * @param userList
	 * @return
	 */
	public List<Post> getListOfPostsFromUsers(ArrayList<User> userList) {
		ArrayList<Post> list = new ArrayList<Post>(getAllPosts().values());

		if (userList == null || userList.isEmpty()) return list;

		ArrayList<Post> resultList = new ArrayList<Post>();

		for (Post post : list) {
			for (User user : userList) {
				if (post.getUserId() == user.getId()) {
					resultList.add(post);
				}
			}
		}

		return resultList;
	}

	@Override
	public void writePost(User user, String text) {
		//checkIfUserIsLoggedIn(user, ip);

		String id = String.valueOf(postid.incrementAndGet());

		String key = KEY_PREFIX_POST + "number" + id;
		srt_hashOps.put(key, "id", id);
		srt_hashOps.put(key, "userId", user.getId());
		srt_hashOps.put(key, "text", text);
		DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
		String formattedString = LocalDate.now().format(formatter);
		srt_hashOps.put(key, "date", formattedString);

		// the key for a new user is added to the set for all usernames
		srt_setOps.add(KEY_SET_ALL_POSTS, user.getUsername());


		Post post = new Post();
		post.setId(id);
		post.setUserId(user.getId());
		post.setText(text);
		post.setDate(formattedString);
		// to show how objects can be saved
		rt_hashOps_post.put(KEY_HASH_ALL_POSTS, key, post);


	}

}