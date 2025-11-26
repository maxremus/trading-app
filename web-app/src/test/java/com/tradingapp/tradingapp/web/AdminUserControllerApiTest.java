package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.entities.User;
import com.tradingapp.tradingapp.entities.UserRole;
import com.tradingapp.tradingapp.repositories.UserRepository;
import com.tradingapp.tradingapp.services.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
public class AdminUserControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CustomerService customerService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.USER)
                .active(true)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeRole_FromUserToAdmin_ShouldChangeRoleAndRedirect() throws Exception {
        // Given - потребител с USER роля
        testUser.setRole(UserRole.USER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When & Then - промяна от USER към ADMIN
        mockMvc.perform(post("/admin/users/role/{id}", userId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(flash().attribute("success", "Ролята е сменена на ADMIN"));

        // Verify - проверка дали ролята е променена на ADMIN
        verify(userRepository, times(1)).save(argThat(user ->
                user.getRole() == UserRole.ADMIN
        ));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeRole_FromAdminToUser_ShouldChangeRoleAndRedirect() throws Exception {
        // Given - потребител с ADMIN роля
        testUser.setRole(UserRole.ADMIN);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When & Then - промяна от ADMIN към USER
        mockMvc.perform(post("/admin/users/role/{id}", userId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attributeExists("success"))
                .andExpect(flash().attribute("success", "Ролята е сменена на USER"));

        // Verify - проверка дали ролята е променена на USER
        verify(userRepository, times(1)).save(argThat(user ->
                user.getRole() == UserRole.USER
        ));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeRole_WithInvalidUserId_ShouldThrowException() throws Exception {
        // Given - несъществуващ потребител
        UUID invalidUserId = UUID.randomUUID();
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        // When & Then - очакваме грешка
        mockMvc.perform(post("/admin/users/role/{id}", invalidUserId)
                        .with(csrf()))
                .andExpect(status().isOk()) // Грешката се обработва от GlobalControllerAdvice
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("statusCode", 500));

        // Verify - проверка дали не е извикан save
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void changeRole_WithoutAuthentication_ShouldBeUnauthorized() throws Exception {
        // When & Then - неаутентикиран потребител
        mockMvc.perform(post("/admin/users/role/{id}", userId)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        // Verify - проверка дали не са извикани repository методи
        verify(userRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeRole_WithMalformedUUID_ShouldHandleException() throws Exception {
        // Given - невалиден UUID формат
        String malformedId = "invalid-uuid";

        // When & Then - очакваме грешка при парсване на UUID
        mockMvc.perform(post("/admin/users/role/{id}", malformedId)
                        .with(csrf()))
                .andExpect(status().isOk()) // Грешката се обработва от GlobalControllerAdvice
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));

        // Verify - проверка дали не е извикан findById
        verify(userRepository, never()).findById(any(UUID.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeRole_ShouldCallRepositorySave() throws Exception {
        // Given
        testUser.setRole(UserRole.USER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        mockMvc.perform(post("/admin/users/role/{id}", userId)
                .with(csrf()));

        // Then - проверка дали save е извикан точно веднъж
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void changeRole_WithSameUser_ShouldWorkCorrectly() throws Exception {
        // Given - ADMIN потребител променя собствената си роля
        testUser.setRole(UserRole.ADMIN);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/admin/users/role/{id}", userId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attribute("success", "Ролята е сменена на USER"));

        // Verify - ролята е променена на USER
        verify(userRepository, times(1)).save(argThat(user ->
                user.getRole() == UserRole.USER
        ));
    }
}

