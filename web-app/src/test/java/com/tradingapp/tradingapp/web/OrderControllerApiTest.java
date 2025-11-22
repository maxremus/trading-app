package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.entities.Customer;
import com.tradingapp.tradingapp.entities.Order;
import com.tradingapp.tradingapp.entities.Product;
import com.tradingapp.tradingapp.services.CustomerService;
import com.tradingapp.tradingapp.services.OrderService;
import com.tradingapp.tradingapp.services.ProductService;
import com.tradingapp.tradingapp.web.dto.OrderDTO;
import com.tradingapp.tradingapp.web.dto.OrderItemDTO;
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

@WebMvcTest(OrderController.class)
class OrderControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private ProductService productService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private GlobalControllerAdvice globalControllerAdvice;

    private Order testOrder;
    private Customer testCustomer;
    private Product testProduct;
    private UUID orderId;
    private UUID customerId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();

        testCustomer = Customer.builder()
                .id(customerId)
                .name("Test Customer")
                .eik("123456789")
                .email("test@example.com")
                .build();

        testProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .price(new BigDecimal("25.50"))
                .quantity(100)
                .category("Test Category")
                .build();

        testOrder = Order.builder()
                .id(orderId)
                .customer(testCustomer)
                .totalPrice(new BigDecimal("127.50"))
                .build();
    }

    @Test
    @WithMockUser
    void listOrders_ShouldReturnOrdersPage() throws Exception {
        // Given
        when(orderService.getAllOrders()).thenReturn(List.of(testOrder));

        // When & Then
        MockHttpServletRequestBuilder request = get("/orders");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    @WithMockUser
    void showAddForm_ShouldReturnAddOrderPage() throws Exception {
        // Given
        when(productService.getAllProducts()).thenReturn(List.of(testProduct));
        when(customerService.getAllCustomers()).thenReturn(List.of(testCustomer));

        // When & Then
        MockHttpServletRequestBuilder request = get("/orders/add");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-order"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("customers"));
    }

    @Test
    @WithMockUser
    void addOrder_WithValidData_ShouldRedirectToOrders() throws Exception {
        // Given
        when(orderService.saveOrder(any(OrderDTO.class))).thenReturn(testOrder);

        // When & Then
        MockHttpServletRequestBuilder request = post("/orders/add")
                .with(csrf())
                .param("customerId", customerId.toString())
                .param("items[0].productId", productId.toString())
                .param("items[0].quantity", "5")
                .param("generateInvoice", "false");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders?success"));
    }

    @Test
    @WithMockUser
    void addOrder_WithEmptyCustomerId_ShouldRedirectToOrders() throws Exception {
        // Given
        when(orderService.saveOrder(any(OrderDTO.class))).thenReturn(testOrder);

        // When & Then - Приемаме пренасочване
        MockHttpServletRequestBuilder request = post("/orders/add")
                .with(csrf())
                .param("customerId", "")
                .param("items[0].productId", productId.toString())
                .param("items[0].quantity", "5");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders?success"));
    }

    @Test
    @WithMockUser
    void addOrder_WithInvalidQuantity_ShouldRedirectToOrders() throws Exception {
        // Given
        when(orderService.saveOrder(any(OrderDTO.class))).thenReturn(testOrder);

        // When & Then - Приемаме пренасочване
        MockHttpServletRequestBuilder request = post("/orders/add")
                .with(csrf())
                .param("customerId", customerId.toString())
                .param("items[0].productId", productId.toString())
                .param("items[0].quantity", "0");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders?success"));
    }

    @Test
    @WithMockUser
    void showEditForm_ShouldReturnEditOrderPage() throws Exception {
        // Given
        OrderDTO orderDTO = OrderDTO.builder()
                .id(orderId)
                .customerId(customerId)
                .eik("123456789")
                .items(List.of(OrderItemDTO.builder()
                        .productId(productId)
                        .quantity(5)
                        .build()))
                .generateInvoice(false)
                .build();

        when(orderService.getOrderAsDto(orderId)).thenReturn(orderDTO);
        when(productService.getAllProducts()).thenReturn(List.of(testProduct));
        when(customerService.getAllCustomers()).thenReturn(List.of(testCustomer));

        // When & Then
        MockHttpServletRequestBuilder request = get("/orders/edit/{id}", orderId);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-order"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("customers"));
    }

    @Test
    @WithMockUser
    void updateOrder_WithValidData_ShouldRedirectToOrders() throws Exception {
        // Given
        when(orderService.updateOrder(any(UUID.class), any(OrderDTO.class))).thenReturn(testOrder);

        // When & Then
        MockHttpServletRequestBuilder request = post("/orders/edit/{id}", orderId)
                .with(csrf())
                .param("customerId", customerId.toString())
                .param("items[0].productId", productId.toString())
                .param("items[0].quantity", "10");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders?updated"));
    }

    @Test
    @WithMockUser
    void deleteOrder_ShouldRedirectToOrders() throws Exception {
        // Given
        doNothing().when(orderService).deleteOrder(orderId);

        // When & Then
        MockHttpServletRequestBuilder request = get("/orders/delete/{id}", orderId)
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders?deleted"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteOrder_AsAdmin_ShouldSucceed() throws Exception {
        // Given
        doNothing().when(orderService).deleteOrder(orderId);

        // When & Then
        MockHttpServletRequestBuilder request = get("/orders/delete/{id}", orderId)
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders?deleted"));
    }

    public GlobalControllerAdvice getGlobalControllerAdvice() {
        return globalControllerAdvice;
    }

    public void setGlobalControllerAdvice(GlobalControllerAdvice globalControllerAdvice) {
        this.globalControllerAdvice = globalControllerAdvice;
    }
}

