package com.smart.controllers;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entites.Contact;
import com.smart.entites.User;
import com.smart.helpers.Message;


@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private ContactRepository contactRepository;
	@Autowired
	private UserRepository userRepository;

	// method for comman data

	@ModelAttribute
	public void getUserName(Model model, Principal principal) {
		String userName = principal.getName();

		User user = userRepository.getUserByUserName(userName);
		model.addAttribute("user", user);
	}

	// handler for user dashboard
	@RequestMapping("/index")
	public String userDashboard(Model model, Principal principal) {
		
		model.addAttribute("title","Dashboard - user");
		return "normal/user_dashboard";
	}

	// handler form Add Form Page
	@GetMapping("/add-contact")
	public String userAddForm(Model model) {
		model.addAttribute("title", "Add - contact");
		model.addAttribute("contact",new Contact());
		return "normal/add-contact-form";
	}
	
	// handler for save contact
	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute Contact contact,BindingResult result,@RequestParam("profileImage") MultipartFile file,Principal principal,Model model,HttpSession session)
	{
	
		
		try {
			if(result.hasErrors())
			{
				System.out.println("ERROR.."+result.toString());
				return "normal/add-contact-form";
			}
		
			System.out.println("DATA "+contact);
			
			String userName = principal.getName();
			
			if(file.isEmpty())
			{
				// file is empty
					contact.setImage("contact.png");
					
			}else {
				
				//file the file to folder and  update the name to contact
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				System.out.println("Image is Uploded !!");
			}
			
			User user = userRepository.getUserByUserName(userName);
			
			contact.setUser(user);
			
			user.getContacts().add(contact);
			
			userRepository.save(user);
			
			
			System.out.println("Added successfully !!");
			session.setAttribute("message", new Message("Successfully Added !!","alert-success"));
			return "normal/add-contact-form";
			
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !!","alert-danger"));
			return "normal/add-contact-form";
		}
		
			
		
	}

	//handler for view contact
	//per page=5[n]
	//current page=page
	@GetMapping("/view-contacts/{page}")
	public String viewContact(@PathVariable("page") Integer page ,Model model,Principal principal)
	{
		String name = principal.getName();
		
		User user = userRepository.getUserByUserName(name);
		 
		Pageable pageable = PageRequest.of(page, 8);
		Page<Contact> contacts = contactRepository.findContactByUser(user.getId(),pageable);
		model.addAttribute("contacts" ,contacts);	
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPage",contacts.getTotalPages());
		
		model.addAttribute("title","view Contact");
		
		return "normal/view-contacts";
	}
	
	//handler for show specific contact
	@GetMapping("/{userId}/contact")
	public String showSpecificContact(@PathVariable("userId") int userId,Model model,Principal principal)
	{
		Optional<Contact> contactOptional = contactRepository.findById(userId);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		
		User user = userRepository.getUserByUserName(userName);
		//check..
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact",contact);
			model.addAttribute("title" ,contact.getContact_name());
		}
		return "normal/contact";
	}
	
	
	
	
	
	//handler for delete Specific contact
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") int cId,Model model,Principal principal,HttpSession session)
	{
		Optional<Contact> contactOptional = contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String username = principal.getName();
		User user = userRepository.getUserByUserName(username);
		
		
		//check...
		if(user.getId()==contact.getUser().getId())
		{
			contactRepository.delete(contact);
		}
		
		
		session.setAttribute("message", new Message("Contact Deleted !!","alert-success"));
		
		return "redirect:/user/view-contacts/0";
	}
	
	//handler for update contact form
	@GetMapping("/update/{cId}")
	public String updateContact(@PathVariable("cId") int cId,Model model,Principal principal)
	{
		
		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		
		Optional<Contact> contactOptional = contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact",contact);
		}
			
		
		return "normal/update-form";
	}
	
	//handler for processing update contact form
	@PostMapping("/process-update")
	public String processUpadte(@Valid @ModelAttribute Contact contact,BindingResult result,@RequestParam("profileImage") MultipartFile file,Model model,Principal principal,HttpSession session)
	{
		try {
			
			Contact oldContactDetail = contactRepository.findById(contact.getC_id()).get();
			
			if(!file.isEmpty())
			{
				//file work
				//rewrite
				//delete old photo
				
				//update new photo
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
			}else {
				
				
				//file is empty
				contact.setImage(oldContactDetail.getImage());
				
				System.out.println("Image is Uploded !!");
			}
			
			User user = userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			contactRepository.save(contact);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return "redirect:/user/"+contact.getC_id()+"/contact";
	}
	
	//handler for user Profile
	@GetMapping("/profile")
	public String userProfile(Model model) 
	{
		model.addAttribute("title","Your - Profile");
		return "normal/profile";
	}
	
	//handler for opent Setting
	@GetMapping("/setting")
	public String openSetting(Model model)
	{
		model.addAttribute("title" ,"Setting");
		
		return "normal/setting";
	}
	
	//handler for change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session)
	{
		
			User user = userRepository.getUserByUserName(principal.getName());
			
			System.out.println(user.getPassword());
			
			if(bCryptPasswordEncoder.matches(oldPassword, user.getPassword()))
			{
				//change Password
				
				user.setPassword(bCryptPasswordEncoder.encode(newPassword));
				userRepository.save(user);
				session.setAttribute("message", new Message("Password Successfully Changed !!","alert-success"));
				
			}else
			{
				//error
				
				session.setAttribute("message", new Message("Wrong Old Password !!","alert-danger"));
				return "normal/setting";
			}
			
		
			return "normal/setting";
		
		}
	
}

