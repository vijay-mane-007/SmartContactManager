package com.smart.controllers;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entites.User;
import com.smart.helpers.Message;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	
	// hander for home page
	@RequestMapping("/")
	public String homePage() {

		return "home";
	}

	//handler for About Page
	@RequestMapping("/about")
	public String userAbout()
	{
		return "about";
	}
	
	// handler for signup page user
	@RequestMapping(value = "/signup")
	public String signupUser(Model model) {
		
		
		model.addAttribute("user",new User());
		return "signup";
	}

	// handler for Registering user
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String regiterUser(@Valid @ModelAttribute("user") User user,BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			 HttpSession session) {
		try {
			
			 
			if (!agreement) {
				System.out.println("you have not agreed the terms and conditions");
				throw new Exception("you have not agreed the terms and conditions");
			}

			if(result1.hasErrors())
			{
				System.out.println("ERROR "+result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
				
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImage_url("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println("Agreement "+agreement);
			
			User result = userRepository.save(user);

			model.addAttribute("user",result);

			
			session.setAttribute("message", new Message("Successfully Registered !! ", "alert-success"));
			return "signup";

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong !! " + e.getMessage(), "alert-danger"));
			return "signup";
		}
	}
		//handler for custom login 
		@GetMapping("/signin") 
		public String customLogin(Model model)
		{
			model.addAttribute("title","login page");
			return "login";
		}
		
	
}
