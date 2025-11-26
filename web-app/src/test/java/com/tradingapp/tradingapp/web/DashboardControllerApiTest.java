package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.services.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
public class DashboardControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER", "ADMIN"})
    void showDashboard_WithAdminRole_ShouldReturnDashboard() throws Exception {

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("isAdmin"))
                .andExpect(model().attributeExists("isUser"))
                .andExpect(model().attributeExists("username"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void showDashboard_WithUserRole_ShouldReturnDashboard() throws Exception {

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("isAdmin", false))
                .andExpect(model().attribute("isUser", true));
    }

    @Test
    void showDashboard_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isUnauthorized());
    }
}

