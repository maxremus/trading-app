package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.entities.User;
import com.tradingapp.tradingapp.entities.UserRole;
import com.tradingapp.tradingapp.repositories.UserRepository;
import com.tradingapp.tradingapp.web.dto.LoginDto;
import com.tradingapp.tradingapp.web.dto.UserRegisterDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public ModelAndView showLoginPage(@ModelAttribute("successMessage") String successMessage) {

        log.info(" Open the login page.");

        ModelAndView modelAndView = new ModelAndView("login");
        modelAndView.addObject("loginDto", new LoginDto());

        if (successMessage != null && !successMessage.isEmpty()) {
            modelAndView.addObject("successMessage", successMessage);
        }

        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView showRegisterPage() {

        log.info(" Open the registration page.");

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", new UserRegisterDto());
        modelAndView.setViewName("register");

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerUser(
            @Valid @ModelAttribute("user") UserRegisterDto dto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        ModelAndView modelAndView = new ModelAndView("register");

        // validation errors
        if (result.hasErrors()) {
            log.warn("Registration error: invalid fields for '{}'.", dto.getUsername());
            modelAndView.addObject("error", "Please fill in all fields correctly.");
            return modelAndView;
        }

        // password mismatch
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            log.warn("Password mismatch during registration for '{}'.", dto.getUsername());
            modelAndView.addObject("error", "Passwords do not match!");
            return modelAndView;
        }

        // username exists
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            log.warn("Existing username used: '{}'.", dto.getUsername());
            modelAndView.addObject("error", "Username already exists!");
            return modelAndView;
        }

        // save new user
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(UserRole.USER)
                .active(true)
                .build();

        userRepository.save(user);

        log.info("Successful registration of new user '{}'.", user.getUsername());

        // add success message and redirect
        redirectAttributes.addFlashAttribute("successMessage",
                "Registration successful! You can now log in.");

        return new ModelAndView("redirect:/login");
    }
}
