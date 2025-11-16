package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.entities.User;
import com.tradingapp.tradingapp.entities.UserRole;
import com.tradingapp.tradingapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;

    @Autowired
    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ModelAndView listUsers() {
        ModelAndView mav = new ModelAndView("admin-users");
        mav.addObject("users", userRepository.findAll());
        return mav;
    }

    @PostMapping("/role/{id}")
    public ModelAndView changeRole(
            @PathVariable String id,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == UserRole.ADMIN) {
            user.setRole(UserRole.USER);
            redirectAttributes.addFlashAttribute("success", "Ролята е сменена на USER");
        } else {
            user.setRole(UserRole.ADMIN);
            redirectAttributes.addFlashAttribute("success", "Ролята е сменена на ADMIN");
        }

        userRepository.save(user);
        return new ModelAndView("redirect:/admin/users");
    }

    @PostMapping("/toggle/{id}")
    public ModelAndView toggleActive(
            @PathVariable String id,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(!user.isActive());
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success",
                user.isActive()
                        ? "Потребителят е активиран."
                        : "Потребителят е блокиран.");

        return new ModelAndView("redirect:/admin/users");
    }

}
