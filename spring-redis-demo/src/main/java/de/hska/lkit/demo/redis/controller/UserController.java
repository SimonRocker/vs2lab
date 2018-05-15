package de.hska.lkit.demo.redis.controller;

import java.util.Map;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
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

	/**
	 * list all users
	 * 
	 * @param model
	 * 
	 * @return
	 */
	@RequestMapping(value = "/users", method = RequestMethod.GET)
	public String getAllUsers(Model model) throws UnknownHostException{
		/*boolean logInSuccess = userRepository.checkIfUserIsLoggedIn(InetAddress.getLocalHost().getHostAddress());
		if(!logInSuccess)
			return "logInUser";*/
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
	public String getAllUsersHome(Model model) throws UnknownHostException{
		boolean logInSuccess = userRepository.checkIfUserIsLoggedIn(InetAddress.getLocalHost().getHostAddress());
		if(!logInSuccess)
			return "logInUser";
		Map<String, User> retrievedUsers = userRepository.getAllUsers();
		model.addAttribute("users", retrievedUsers);
		model.addAttribute("posts", userRepository.getAllPosts());

		return "home";
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
	public String getOneUsers(@PathVariable("username") String username, Model model) throws UnknownHostException{
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

		userRepository.saveUser(user);
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
	public String addPost(@ModelAttribute Post post, Greeting greeting) throws UnknownHostException{
		boolean logInSuccess = userRepository.checkIfUserIsLoggedIn(InetAddress.getLocalHost().getHostAddress());
		if(!logInSuccess)
			return "logInUser";
		return "newPost";
	}



	@RequestMapping(value = "/logInUser", method = RequestMethod.GET)
	public String logInUser(@ModelAttribute Greeting greeting) {
		return "logInUser";
	}


	@RequestMapping(value = "/logInUser", method = RequestMethod.POST)
	public String logInUser(@ModelAttribute Greeting greeting, Model model) throws UnknownHostException {
		String testString = greeting.getUsername();
		boolean logInSuccess = userRepository.logInUser(greeting.getUsername(), greeting.getPassword(), InetAddress.getLocalHost().getHostAddress());
		if(logInSuccess)
			return "home";
		else
			return "logInUser";
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
	public String writePost(@ModelAttribute Post post, Model model) throws UnknownHostException{
		boolean logInSuccess = userRepository.checkIfUserIsLoggedIn(InetAddress.getLocalHost().getHostAddress());
		if(!logInSuccess)
			return "logInUser";

		userRepository.writePost(post);
		model.addAttribute("message", "Post successfully added");

		Map<String, Post> retrievedPost = userRepository.getAllPosts();

		model.addAttribute("users", userRepository.getAllUsers());
		model.addAttribute("posts", retrievedPost);
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
	public String searchUser(@PathVariable("pattern") String pattern, @ModelAttribute User user, Model model) throws UnknownHostException{
		boolean logInSuccess = userRepository.checkIfUserIsLoggedIn(InetAddress.getLocalHost().getHostAddress());
		if(!logInSuccess)
			return "logInUser";

		Map<String, User> retrievedUsers = userRepository.findUsersWith(pattern);
		model.addAttribute("users", retrievedUsers);
		return "users";
	}

	public void follow(String username){
		userRepository.follow(username);
	}

	@RequestMapping(value = "/users", method = RequestMethod.POST)
	public String follow(@ModelAttribute String username, Model model) {

		userRepository.follow(username);
		model.addAttribute("message", "User successfully followed");

		Map<String, User> retrievedUsers = userRepository.getAllUsers();

		model.addAttribute("users", retrievedUsers);
		model.addAttribute("relation", userRepository.getAllRelations());
		return "users";
	}
	
	

}
