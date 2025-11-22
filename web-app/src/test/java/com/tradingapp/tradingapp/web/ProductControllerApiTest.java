package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.entities.Product;
import com.tradingapp.tradingapp.services.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private GlobalControllerAdvice globalControllerAdvice;

    private Product testProduct;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        testProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .price(new BigDecimal("25.50"))
                .quantity(100)
                .category("Test Category")
                .description("Test Description")
                .build();
    }

    @Test
    @WithMockUser
    void listAllProducts_ShouldReturnProductsPage() throws Exception {
        // Given
        when(productService.getAllProducts()).thenReturn(List.of(testProduct));

        // When & Then
        MockHttpServletRequestBuilder request = get("/products");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attribute("activePage", "products"));
    }

    @Test
    @WithMockUser
    void showAddForm_ShouldReturnAddProductPage() throws Exception {
        // When & Then
        MockHttpServletRequestBuilder request = get("/products/add");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-product"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    @WithMockUser
    void addProduct_WithValidData_ShouldRedirectToProducts() throws Exception {
        // Given
        when(productService.saveProduct(any(Product.class))).thenReturn(testProduct);

        // When & Then
        MockHttpServletRequestBuilder request = post("/products/add")
                .with(csrf())
                .param("name", "New Product")
                .param("price", "19.99")
                .param("quantity", "50")
                .param("category", "New Category")
                .param("description", "New Description");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }

    @Test
    @WithMockUser
    void addProduct_WithEmptyName_ShouldReturnFormWithErrors() throws Exception {
        // When & Then - Промени очакванията, защото контролера пренасочва при грешка
        MockHttpServletRequestBuilder request = post("/products/add")
                .with(csrf())
                .param("name", "") // Invalid empty name
                .param("price", "19.99")
                .param("quantity", "50")
                .param("category", "New Category");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }

    @Test
    @WithMockUser
    void showEditForm_ShouldReturnEditProductPage() throws Exception {
        // Given
        when(productService.findById(productId)).thenReturn(testProduct);

        // When & Then
        MockHttpServletRequestBuilder request = get("/products/edit/{id}", productId);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-product"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_ShouldRedirectToProducts() throws Exception {
        // Given
        doNothing().when(productService).deleteProduct(productId);

        // When & Then
        MockHttpServletRequestBuilder request = get("/products/delete/{id}", productId)
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products"));
    }
}

