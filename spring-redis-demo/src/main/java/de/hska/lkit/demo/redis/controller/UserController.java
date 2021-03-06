package de.hska.lkit.demo.redis.controller;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;

import de.hska.lkit.demo.redis.Receiver;
import de.hska.lkit.demo.redis.repo.impl.UserRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.hska.lkit.demo.redis.model.*;
import de.hska.lkit.demo.redis.repo.UserRepository;

/**
 * @author knad0001
 *
 */
@Controller
public class UserController {

	private final UserRepository userRepository;

	@Autowired
	public UserController(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}

	// Redis Things
    @Autowired
    RedisMessageListenerContainer container;
	@Autowired
	StringRedisTemplate template;
	@Autowired
    MessageListenerAdapter listenerAdapter;



	/**
	 * list all users
	 * 
	 * @param model
	 * 
	 * @return
	 */
	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public String getAllUsers(@ModelAttribute Greeting greeting, Model model, @ModelAttribute Post post) throws UnknownHostException{
		boolean logInSuccess = userRepository.checkIfUserIsLoggedIn(InetAddress.getLocalHost().getHostAddress());
		if(!logInSuccess)
			return "logInUser";
		Map<String, User> retrievedUsers = userRepository.getAllUsers();
		model.addAttribute("users", retrievedUsers);
		model.addAttribute("posts", userRepository.getAllPosts());
		model.addAttribute("tokens", userRepository.getAllTokens());
		return "users";
	}


	/**
	 *
	 * get all posts and users to homepage
	 *
	 *
	 * */
	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String getAllUsersHome(@ModelAttribute Greeting greeting, Model model, @ModelAttribute Post post) throws UnknownHostException{
		boolean logInSuccess = userRepository.checkIfUserIsLoggedIn(InetAddress.getLocalHost().getHostAddress());
		if(!logInSuccess)
			return "logInUser";
		Map<String, User> retrievedUsers = userRepository.getAllUsers();
		model.addAttribute("users", retrievedUsers);
		model.addAttribute("posts", userRepository.getAllPosts());
		model.addAttribute("followers", userRepository.getFollowedUsersForCurrentUser(InetAddress.getLocalHost().getHostAddress()) );
		model.addAttribute("personalPosts", userRepository.getListOfPostsFromUsers(userRepository.getFollowedUsersForCurrentUser(InetAddress.getLocalHost().getHostAddress())));
		return "home";
	}



	@RequestMapping(value = "/home", method = RequestMethod.POST)
	public String searchUsers(@ModelAttribute Greeting greeting, Model model, @ModelAttribute Post post) throws UnknownHostException{

		boolean logInSuccess = userRepository.checkIfUserIsLoggedIn(InetAddress.getLocalHost().getHostAddress());
		if(!logInSuccess)
			return "logInUser";

		Map<String, User> foundUsers = userRepository.findUsersWith(post.getText());

		model.addAttribute("users", foundUsers);
		return "users";
	}




	/**
	 * get information for user with username
	 * 
	 * @param username
	 *            username to find
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/user/{username}", method = RequestMethod.GET)
	public String getOneUsers(@ModelAttribute Greeting greeting, @PathVariable("username") String username, Model model) throws UnknownHostException{
		boolean logInSuccess = userRepository.checkIfUserIsLoggedIn(InetAddress.getLocalHost().getHostAddress());
		if(!logInSuccess)
			return "logInUser";
		User found = userRepository.getUser(username);

		model.addAttribute("userFound", found);
		return "oneUser";
	}




	/**
	 * redirect to page to add a new user
	 * 
	 * @return
	 */
	@RequestMapping(value = "/adduser", method = RequestMethod.GET)
	public String addUser(@ModelAttribute User user) {
		return "newUser";
	}

	/**
	 * add a new user, adds a list of all users to model
	 * 
	 * @param user
	 *            User object filled in form
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/adduser", method = RequestMethod.POST)
	public String saveUser(@ModelAttribute User user, Model model) throws UnknownHostException{

		boolean success = userRepository.saveUser(user);
		if(!success) return "logInUser";
		userRepository.logInUser(user.getUsername(), user.getPassword(), InetAddress.getLocalHost().getHostAddress());
		model.addAttribute("message", "User successfully added");

		Map<String, User> retrievedUsers = userRepository.getAllUsers();

		model.addAttribute("users", retrievedUsers);
		return "users";
	}

	/**
	 * redirect to page to add a new post
	 *
	 * @return
	 */
	@RequestMapping(value = "/addPost", method = RequestMethod.GET)
	public String addPost(@ModelAttribute Greeting greeting, @ModelAttribute Post post, Model model) throws UnknownHostException{
		boolean logInSuccess = userRepository.checkIfUserIsLoggedIn(InetAddress.getLocalHost().getHostAddress());
		if(!logInSuccess)
			return "logInUser";

		Token token = ((UserRepositoryImpl) userRepository).getToken(InetAddress.getLocalHost().getHostAddress());
		DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
		String formattedString = LocalDate.now().format(formatter);

        model.addAttribute("username", token.getUsername());
        model.addAttribute("date", formattedString);
		return "newPost";
	}

	@RequestMapping(value = "/logInUser", method = RequestMethod.GET)
	public String logInUser(@ModelAttribute Greeting greeting) throws UnknownHostException{
		userRepository.logOutUser(InetAddress.getLocalHost().getHostAddress());
		return "logInUser";
	}

	@RequestMapping(value = "/logInUser", method = RequestMethod.POST)
	public String logInUser(@ModelAttribute Greeting greeting, Model model, @ModelAttribute Post post) throws UnknownHostException {
		boolean logInSuccess = userRepository.logInUser(greeting.getUsername(), greeting.getPassword(), InetAddress.getLocalHost().getHostAddress());

		if(logInSuccess)
		{
			Map<String, User> retrievedUsers = userRepository.getAllUsers();
			model.addAttribute("users", retrievedUsers);
			model.addAttribute("posts", userRepository.getAllPosts());
			model.addAttribute("followers", userRepository.getFollowedUsersForCurrentUser(InetAddress.getLocalHost().getHostAddress()));
			return "home";
		}
		else {
			return "logInUser";
		}
	}

	/**
	 * add a new post, adds a list of all posts to model
	 *
	 * @param post
	 *            Post object filled in form
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/addPost", method = RequestMethod.POST)
	public String writePost(@ModelAttribute Greeting greeting, @ModelAttribute Post post, Model model) throws UnknownHostException{
		boolean logInSuccess = userRepository.checkIfUserIsLoggedIn(InetAddress.getLocalHost().getHostAddress());
		if(!logInSuccess)
			return "logInUser";

		userRepository.writePost(post, InetAddress.getLocalHost().getHostAddress());
		model.addAttribute("message", "Post successfully added");

		Map<String, Post> retrievedPost = userRepository.getAllPosts();

		model.addAttribute("users", userRepository.getAllUsers());
		model.addAttribute("posts", retrievedPost);

		/*
		Redis Pub/Sub Messaging
		 */
        String message = post.getText();
        template.convertAndSend("newPostIsComingMethod", message);



		return "users";
	}
	
	/**
	 * search usernames containing the sequence of characters
	 * 
	 * @param user
	 *            User object filled in form
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/searchuser/{pattern}", method = RequestMethod.GET)
	public String searchUser(@ModelAttribute Greeting greeting, @PathVariable("pattern") String pattern, @ModelAttribute User user, Model model) throws UnknownHostException{
		boolean logInSuccess = userRepository.checkIfUserIsLoggedIn(InetAddress.getLocalHost().getHostAddress());
		if(!logInSuccess)
			return "logInUser";

		Map<String, User> retrievedUsers = userRepository.findUsersWith(pattern);
		model.addAttribute("users", retrievedUsers);
		return "users";
	}

	@RequestMapping(value = "/users", method = RequestMethod.POST)
	public String follow(@ModelAttribute Greeting greeting, @ModelAttribute Post post, Model model) throws UnknownHostException {

		userRepository.follow(post.getText(), InetAddress.getLocalHost().getHostAddress());
		model.addAttribute("message", "User successfully followed");

		Map<String, User> retrievedUsers = userRepository.getAllUsers();

		model.addAttribute("users", retrievedUsers);
		model.addAttribute("relation", userRepository.getAllRelations());
		return "users";
	}
	
	

}
