package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.entities.User;
import com.tradingapp.tradingapp.repositories.UserRepository;
import com.tradingapp.tradingapp.web.dto.ChangePasswordDto;
import com.tradingapp.tradingapp.web.dto.UpdateProfileDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("")
    public ModelAndView showAccount(Authentication auth) {

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UpdateProfileDto profileDto = new UpdateProfileDto();
        profileDto.setUsername(user.getUsername());
        profileDto.setEmail(user.getEmail());

        ModelAndView mav = new ModelAndView("account");
        mav.addObject("profile", profileDto);
        mav.addObject("passwordDto", new ChangePasswordDto());
        mav.addObject("user", user);

        return mav;
    }

    @PostMapping("/update")
    public ModelAndView updateProfile(
            @Valid @ModelAttribute("profile") UpdateProfileDto dto,
            BindingResult result,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ModelAndView mav = new ModelAndView("account");
        mav.addObject("passwordDto", new ChangePasswordDto());
        mav.addObject("user", user);

        // Полета невалидни
        if (result.hasErrors()) {
            mav.addObject("error", "Моля, попълнете всички полета коректно.");
            return mav;
        }

        // Email вече зает
        if (userRepository.existsByEmailAndIdNot(dto.getEmail(), user.getId())) {
            mav.addObject("error", "Този имейл вече се използва!");
            return mav;
        }

        // Обновяване
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Профилът е обновен успешно!");

        return new ModelAndView("redirect:/account");
    }

    @PostMapping("/change-password")
    public ModelAndView changePassword(
            @Valid @ModelAttribute("passwordDto") ChangePasswordDto dto,
            BindingResult result,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ModelAndView mav = new ModelAndView("account");

        UpdateProfileDto profile = new UpdateProfileDto();
        profile.setEmail(user.getEmail());
        profile.setUsername(user.getUsername());

        mav.addObject("profile", profile);
        mav.addObject("user", user);

        // Проверка за грешки
        if (result.hasErrors()) {
            mav.addObject("passError", "Попълнете всички полета коректно.");
            return mav;
        }

        // Проверка на текуща парола
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            mav.addObject("passError", "Грешна текуща парола.");
            return mav;
        }

        // Съвпадение на новите пароли
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            mav.addObject("passError", "Новите пароли не съвпадат.");
            return mav;
        }

        // Запис на нова парола
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Паролата е сменена успешно!");

        return new ModelAndView("redirect:/account");
    }
}
