package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.entities.User;
import com.tradingapp.tradingapp.entities.UserRole;
import com.tradingapp.tradingapp.repositories.UserRepository;
import com.tradingapp.tradingapp.services.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private CustomerService customerService;

    @Test
    @WithMockUser
    void showLoginPage_ShouldReturnDefaultSecurityLoginPage() throws Exception {
        // When & Then - Spring Security предоставя default login page
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Please sign in")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("username")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("password")));
    }

    @Test
    @WithMockUser
    void showRegisterPage_ShouldReturnRegisterPage() throws Exception {
        // When & Then - register page работи правилно
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser
    void registerUser_WithValidData_ShouldRedirectToLogin() throws Exception {
        // Given
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // When & Then
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser
    void registerUser_WithExistingUsername_ShouldReturnError() throws Exception {
        // Given
        User existingUser = User.builder().username("existinguser").build();
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        // When & Then
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "existinguser")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Username already exists!"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser
    void registerUser_WithPasswordMismatch_ShouldReturnError() throws Exception {
        // When & Then
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("password", "password123")
                        .param("confirmPassword", "differentpassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", "Passwords do not match!"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser
    void registerUser_WithInvalidData_ShouldReturnError() throws Exception {
        // When & Then - празно потребителско име
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "") // невалидно
                        .param("password", "pass") // твърде кратко
                        .param("confirmPassword", "pass"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser
    void registerUser_ShouldEncodePassword() throws Exception {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("testpass")).thenReturn("encodedTestPass");

        // When
        mockMvc.perform(post("/register")
                .with(csrf())
                .param("username", "testuser")
                .param("password", "testpass")
                .param("confirmPassword", "testpass"));

        // Then
        verify(passwordEncoder, times(1)).encode("testpass");
        verify(userRepository, times(1)).save(argThat(user ->
                user.getPassword().equals("encodedTestPass") &&
                        user.getRole() == UserRole.USER &&
                        user.isActive()
        ));
    }
}

