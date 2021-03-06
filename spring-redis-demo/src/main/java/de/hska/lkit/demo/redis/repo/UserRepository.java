package de.hska.lkit.demo.redis.repo;

import java.util.*;

import de.hska.lkit.demo.redis.model.*;

public interface UserRepository {
	
	/**
	 * save user to repository
	 * 
	 * @param user
	 */
	public boolean saveUser(User user);
	
	
	/**
	 * returns a list of all users
	 * 
	 * @return
	 */
	public Map<String, User> getAllUsers();
	
	
	/**
	 * find the user with username
	 * 
	 * @param username
	 * @return
	 */
	public User getUser(String username);


	/**
	 * 
	 * find all users with characters in username
	 * 
	 * @param characters
	 * @return
	 */
	public Map<String, User> findUsersWith(String characters);


	/**
	 *
	 * logs in User
	 *
	 * @return boolean, if the login was correct
	 */
	public boolean logInUser(String username, String password, String ip);


	/**
	 *
	 * Logs out user
	 *
	 * @param ip
	 */
	public void logOutUser(String ip);


	/**
	 *
	 * checks, if the user is logged in
	 *
	 *
	 * @param ip
	 * @return true, if logged in
	 */
	public boolean checkIfUserIsLoggedIn(String ip);

	/**
	 *
	 * 	For the timelines we want all posts
	 *
	 * @return all posts in the db
	 */
	public Map<String, Post> getAllPosts();

	/**
	 *
	 * Write a post on your blog.
	 *
	 */
	public void writePost(Post post, String ip);

	/**
	 *
	 * Let you get all Tokens
	 *
	 * @return all Tokens
	 */
	public Map<String, Token> getAllTokens();

	public void follow(String username, String ip);

	public void stopFollowing(String username, String ip);

	public Map<String, Follower_Relation> getAllRelations();

	public List<String> getFollowedUsersForCurrentUser(String ip);

	public Map<String, Post> getListOfPostsFromUsers(List<String> userList);
}
