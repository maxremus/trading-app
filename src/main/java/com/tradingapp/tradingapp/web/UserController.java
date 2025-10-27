package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.entities.User;
import com.tradingapp.tradingapp.entities.UserRole;
import com.tradingapp.tradingapp.repositories.UserRepository;
import com.tradingapp.tradingapp.web.dto.LoginDto;
import com.tradingapp.tradingapp.web.dto.UserRegisterDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @GetMapping("/login")
    public ModelAndView showLoginPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("loginDto", new LoginDto());
        modelAndView.setViewName("login");

        return modelAndView;
    }


    @GetMapping("/register")
    public ModelAndView showRegisterPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", new UserRegisterDto());
        modelAndView.setViewName("register");

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerUser(@Valid @ModelAttribute("user") UserRegisterDto dto,
                                     BindingResult result) {
        ModelAndView modelAndView = new ModelAndView("register");

        if (result.hasErrors()) {
            modelAndView.addObject("error", "Моля, попълнете всички полета коректно.");
            return modelAndView;
        }

        //  Проверка за празни пароли
        if (dto.getPassword() == null || dto.getConfirmPassword() == null ||
                !dto.getPassword().equals(dto.getConfirmPassword())) {
            modelAndView.addObject("error", "Паролите не съвпадат!");
            return modelAndView;
        }

        //  Проверка за съществуващо потребителско име
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            modelAndView.addObject("error", "Потребителското име вече съществува!");
            return modelAndView;
        }

        //  Създаваме нов потребител
        User user = User.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(UserRole.USER)
                .build();

        userRepository.save(user);

        return new ModelAndView("redirect:/login");
    }
}
