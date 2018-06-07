package de.hska.lkit.demo.redis.repo.impl;

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

import de.hska.lkit.demo.redis.model.*;
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

	private static final String KEY_SET_ALL_FOLLOWERS = "all:followers";

	private static final String KEY_ZSET_ALL_USERNAMES = "all:usernames:sorted";

	private static final String KEY_HASH_ALL_USERS = "all:user";

	private static final String KEY_HASH_ALL_POSTS = "all:post";

	private static final String KEY_HASH_ALL_TOKEN = "all:token";

	private static final String KEY_HASH_ALL_FOLLOWERS = "all:follower";

	private static final String KEY_PREFIX_USER = "user:";

	private static final String KEY_PREFIX_POST = "post:";

	private static final String KEY_PREFIX_TOKEN = "token:";

	private static final String KEY_PREFIX_FOLLOWER = "follower:";

	/**
	 * to generate unique ids for user
	 */
	private RedisAtomicLong userid;

	private RedisAtomicLong postid;

	private RedisAtomicLong tokenid;
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

	/**
	 * hash operations for redisTemplate
	 */
	@Resource(name = "redisTemplate")
	private HashOperations<String, String, Token> rt_hashOps_token;

	/**
	 * hash operations for redisTemplate
	 */
	@Resource(name = "redisTemplate")
	private HashOperations<String, String, Follower_Relation> rt_hashOps_follower_relation;


	/*
	 *
	 */
	@Autowired
	public UserRepositoryImpl(RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
		this.redisTemplate = redisTemplate;
		this.stringRedisTemplate = stringRedisTemplate;
		this.userid = new RedisAtomicLong("userid", stringRedisTemplate.getConnectionFactory());
		this.postid = new RedisAtomicLong("postid", stringRedisTemplate.getConnectionFactory());
		this.tokenid = new RedisAtomicLong("tokenid", stringRedisTemplate.getConnectionFactory());
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
	public boolean saveUser(User user) {
		// generate a unique id
		String id = String.valueOf(userid.incrementAndGet());

		user.setId(id);

		// to show how objects can be saved
		// be careful, if username already exists it's not added another time
		String key = KEY_PREFIX_USER + user.getUsername();
		if(srt_setOps.isMember(KEY_SET_ALL_USERNAMES, user.getUsername())) {
			return false;
		}
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

		return true;
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
			String key = KEY_PREFIX_USER + username;
			user.setId(srt_hashOps.get(key, "id"));
			user.setFirstname(srt_hashOps.get(key, "firstName"));
			user.setLastname(srt_hashOps.get(key, "lastName"));
			user.setUsername(srt_hashOps.get(key, "username"));
			user.setPassword(srt_hashOps.get(key, "password"));
		} else
			user = null;
		return user;
	}

	public Token getToken(String ip) {
		Token token = new Token();

		// if ip is in set for all tokens,
		if (srt_setOps.isMember(KEY_SET_ALL_TOKENS, ip)) {

			// get the user data out of the hash object with key "'token:' + ip"
			String key = KEY_PREFIX_TOKEN + ip;
			token.setId(srt_hashOps.get(key, "id"));
			token.setIp(srt_hashOps.get(key, "ip"));
			token.setToDate(srt_hashOps.get(key, "toDate"));
			token.setUsername(srt_hashOps.get(key, "username"));
		} else
			token = null;
		return token;
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
	public boolean logInUser(String username, String password, String ip) {
		User user = getUser(username);
		if (user == null) return false;
		if (user.getPassword().equals(password)) {
			addToken(ip, user.getId());
			return true;
		} else {return false;}
	}

	@Override
	public void logOutUser(String ip) {
			if (srt_setOps.isMember(KEY_SET_ALL_TOKENS, ip)) {
				String key = KEY_PREFIX_TOKEN + ip;
				srt_hashOps.put(key, "toDate", "-1");
				//srt_hashOps.put(key, "useragent", )
		}
	}

	@Override
	public boolean checkIfUserIsLoggedIn(String ip) {
		if (srt_setOps.isMember(KEY_SET_ALL_TOKENS, ip)) {
			DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
			String formattedString = LocalDate.now().format(formatter);
			return getToken(ip) != null && Integer.parseInt(getToken(ip).getToDate()) >= Integer.parseInt(formattedString);
		}
		return false;
	}

	@Override
	public Map<String, Post> getAllPosts() {
			Map<String, Post> unsortMap = rt_hashOps_post.entries(KEY_HASH_ALL_POSTS);

			// 1. Convert Map to List of Map
			List<Map.Entry<String, Post>> list =
					new LinkedList<Map.Entry<String, Post>>(unsortMap.entrySet());

			// 2. Sort list with Collections.sort(), provide a custom Comparator
			//    Try switch the o1 o2 position for a different order
			Collections.sort(list, new Comparator<Map.Entry<String, Post>>() {
				public int compare(Map.Entry<String, Post> o1,
								   Map.Entry<String, Post> o2) {
					return Integer.parseInt(o2.getValue().getDate()) - Integer.parseInt(o1.getValue().getDate());
				}
			});

			// 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
			Map<String, Post> sortedMap = new LinkedHashMap<String, Post>();
			for (Map.Entry<String, Post> entry : list) {
				sortedMap.put(entry.getKey(), entry.getValue());
			}
		return sortedMap;
	}

	/**
	 *
	 * Gets you all posts of the wanted users.
	 * If you call it without usernames, you get the full list.
	 *
	 * @param userList
	 * @return
	 */
	public List<Post> getListOfPostsFromUsers(List<String> userList) {
		ArrayList<Post> list = new ArrayList<Post>(getAllPosts().values());

		if (userList == null || userList.isEmpty()) return list;

		ArrayList<Post> resultList = new ArrayList<Post>();

		for (Post post : list) {
			for (String username : userList) {
				if (post.getUsername().equals(username)) {
					resultList.add(post);
				}
			}
		}

		return resultList;
	}

	@Override
	public void writePost(Post post, String ip) {
		String id = String.valueOf(postid.incrementAndGet());

		String key = KEY_PREFIX_POST + "number" + id;
		srt_hashOps.put(key, "id", id);
		srt_hashOps.put(key, "username", getToken(ip).getUsername());
		srt_hashOps.put(key, "text", post.getText());
		DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
		String formattedString = LocalDate.now().format(formatter);
		srt_hashOps.put(key, "date", formattedString);

		// the key for a new user is added to the set for all usernames
		srt_setOps.add(KEY_SET_ALL_POSTS, key);

		Post postCopy = new Post();
		postCopy.setId(id);
		postCopy.setUsername(getToken(ip).getUsername());
		postCopy.setText(post.getText());
		postCopy.setDate(formattedString);
		// to show how objects can be saved
		rt_hashOps_post.put(KEY_HASH_ALL_POSTS, key, postCopy);

	}

	public void addToken(String ip, String username) {
		String id = String.valueOf(tokenid.incrementAndGet());

		String key = KEY_PREFIX_TOKEN + ip;
		srt_hashOps.put(key, "ip", ip);
		srt_hashOps.put(key, "id", id);
		srt_hashOps.put(key, "username", username);
		DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
		String formattedString = LocalDate.now().plusWeeks(1).format(formatter);
		srt_hashOps.put(key, "toDate", formattedString);

		// the key for a new token is added to the set for all tokens
		srt_setOps.add(KEY_SET_ALL_TOKENS, ip);

		Token token = new Token();
		token.setId(id);
		token.setIp(ip);
		token.setUsername(username);
		token.setToDate(formattedString);
		rt_hashOps_token.put(KEY_HASH_ALL_TOKEN, key, token);


	}

	public void follow(String username, String ip) {
		System.out.println(username + " ist der Nutzer");
		String key = KEY_PREFIX_FOLLOWER + getToken(ip).getUsername();

		ArrayList<String> usernamesList;

		// if username is in set for all usernames,
		if (srt_setOps.isMember(KEY_SET_ALL_FOLLOWERS,  getToken(ip).getUsername())) {
			String[] list = srt_hashOps.get(key, "usernamesFollowed").split(" ");
			usernamesList = new ArrayList<String>(Arrays.asList(list));
		} else {
			usernamesList = new ArrayList<String>();
		}

		if(!usernamesList.contains(username)) {
			usernamesList.add(username);
		}

		String value = "";
		for (String s: usernamesList) {
			value += s + " ";
		}

		if (value != null && value.length() > 0 && String.valueOf(value.charAt(value.length() - 1)) == " ") {
			value = value.substring(0, value.length() - 1);
		}

		srt_hashOps.put(key, "usernamesFollowed", value);

		// the key for a new user is added to the set for all usernames
		srt_setOps.add(KEY_SET_ALL_FOLLOWERS,  getToken(ip).getUsername());


		Follower_Relation relation = new Follower_Relation();
		relation.setUsernameFollower( getToken(ip).getUsername());
		relation.setUsernamesFollowed(usernamesList);
		rt_hashOps_follower_relation.put(KEY_HASH_ALL_FOLLOWERS, key, relation);
	}

	@Override
	public Map<String, Token> getAllTokens() {
		return rt_hashOps_token.entries(KEY_HASH_ALL_TOKEN);
	}

	@Override
	public Map<String, Follower_Relation> getAllRelations() {
		return rt_hashOps_follower_relation.entries(KEY_HASH_ALL_FOLLOWERS);
	}

	@Override
	public List<String> getFollowedUsersForCurrentUser(String ip) {
		System.out.println("getting users...");
		String key = KEY_PREFIX_FOLLOWER +  getToken(ip).getUsername();

		List<String> usernamesList;

		// if username is in set for all follower Relations, get his followed users names
		if (srt_setOps.isMember(KEY_SET_ALL_FOLLOWERS,  getToken(ip).getUsername())) {
			String[] list = srt_hashOps.get(key, "usernamesFollowed").split(" ");
			for (String s: list) {
				System.out.println(s + " Nutzer");
			}
			return Arrays.asList(list);
		} else {
			return null;
		}
	}

}