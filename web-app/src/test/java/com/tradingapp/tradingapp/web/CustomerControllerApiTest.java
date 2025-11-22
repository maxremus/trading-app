package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.entities.Customer;
import com.tradingapp.tradingapp.repositories.CustomerRepository;
import com.tradingapp.tradingapp.services.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
public class CustomerControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CustomerRepository customerRepository;

    private Customer testCustomer;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        testCustomer = Customer.builder()
                .id(customerId)
                .name("Test Customer")
                .eik("123456789")
                .email("test@example.com")
                .address("Test Address")
                .build();
    }

    @Test
    @WithMockUser
    void listCustomers_ShouldReturnCustomersPage() throws Exception {

        // Given
        when(customerService.getAllCustomers()).thenReturn(List.of(testCustomer));

        // When & Then
        MockHttpServletRequestBuilder request = get("/customers");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("customers"))
                .andExpect(model().attributeExists("customers"))
                .andExpect(model().attribute("activePage", "customers"));
    }

    @Test
    @WithMockUser
    void showAddForm_ShouldReturnAddCustomerPage() throws Exception {

        // When & Then
        MockHttpServletRequestBuilder request = get("/customers/add");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-customer"))
                .andExpect(model().attributeExists("customer"));
    }

    @Test
    @WithMockUser
    void addCustomer_WithValidData_ShouldRedirectToCustomers() throws Exception {

        // Given
        when(customerRepository.existsByEik(any())).thenReturn(false);
        when(customerRepository.existsByEmail(any())).thenReturn(false);
        when(customerService.saveCustomer(any(Customer.class))).thenReturn(testCustomer);

        // When & Then
        MockHttpServletRequestBuilder request = post("/customers/add")
                .with(csrf())
                .param("name", "New Customer")
                .param("eik", "987654321")
                .param("email", "new@example.com")
                .param("address", "New Address");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));
    }

    @Test
    @WithMockUser
    void addCustomer_WithDuplicateEik_ShouldReturnError() throws Exception {

        // Given
        when(customerRepository.existsByEik(any())).thenReturn(true);

        // When & Then
        MockHttpServletRequestBuilder request = post("/customers/add")
                .with(csrf())
                .param("name", "New Customer")
                .param("eik", "123456789")
                .param("email", "new@example.com")
                .param("address", "New Address");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-customer"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    @WithMockUser
    void showEditForm_ShouldReturnEditCustomerPage() throws Exception {

        // Given
        when(customerService.getCustomerById(customerId)).thenReturn(testCustomer);

        // When & Then
        MockHttpServletRequestBuilder request = get("/customers/edit/{id}", customerId);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-customer"))
                .andExpect(model().attributeExists("customer"));
    }

    @Test
    @WithMockUser
    void deleteCustomer_ShouldRedirectToCustomers() throws Exception {

        // Given
        doNothing().when(customerService).deleteCustomer(customerId);

        // When & Then
        MockHttpServletRequestBuilder request = get("/customers/delete/{id}", customerId)
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));
    }
}

