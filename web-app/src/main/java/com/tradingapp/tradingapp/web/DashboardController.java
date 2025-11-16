package com.tradingapp.tradingapp.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public ModelAndView showDashboard(Authentication authentication) {

        ModelAndView modelAndView = new ModelAndView("dashboard");

        boolean isAdmin = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        boolean isUser = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_USER"));

        modelAndView.addObject("isAdmin", isAdmin);
        modelAndView.addObject("isUser", isUser);
        modelAndView.addObject("username", authentication.getName());

        return modelAndView;
    }
}
