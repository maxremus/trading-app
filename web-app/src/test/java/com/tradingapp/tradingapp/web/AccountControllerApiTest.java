package com.tradingapp.tradingapp.web;


import com.tradingapp.tradingapp.entities.User;
import com.tradingapp.tradingapp.repositories.UserRepository;
import com.tradingapp.tradingapp.services.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
public class AccountControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private CustomerService customerService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .password("encodedPassword")
                .email("test@example.com")
                .build();
    }

    @Test
    @WithMockUser(username = "testuser")
    void showAccount_ShouldReturnAccountPage() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/account"))
                .andExpect(status().isOk())
                .andExpect(view().name("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().attributeExists("passwordDto"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateProfile_WithValidData_ShouldRedirect() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot("new@example.com", userId)).thenReturn(false);

        mockMvc.perform(post("/account/update")
                        .with(csrf())
                        .param("username", "newusername")
                        .param("email", "new@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account"))
                .andExpect(flash().attributeExists("success"));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateProfile_WithDuplicateEmail_ShouldReturnError() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmailAndIdNot("existing@example.com", userId)).thenReturn(true);

        mockMvc.perform(post("/account/update")
                        .with(csrf())
                        .param("username", "testuser")
                        .param("email", "existing@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("account"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void changePassword_WithValidData_ShouldRedirect() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");

        mockMvc.perform(post("/account/change-password")
                        .with(csrf())
                        .param("oldPassword", "oldPassword")
                        .param("newPassword", "newPassword")
                        .param("confirmPassword", "newPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account"))
                .andExpect(flash().attributeExists("success"));

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "testuser")
    void changePassword_WithWrongCurrentPassword_ShouldReturnError() throws Exception {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        mockMvc.perform(post("/account/change-password")
                        .with(csrf())
                        .param("oldPassword", "wrongPassword")
                        .param("newPassword", "newPassword")
                        .param("confirmPassword", "newPassword"))
                .andExpect(status().isOk())
                .andExpect(view().name("account"))
                .andExpect(model().attributeExists("passError"));
    }
}

