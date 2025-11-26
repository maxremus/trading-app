package com.tradingapp.tradingapp;

import com.tradingapp.tradingapp.entities.*;
import com.tradingapp.tradingapp.invoice.InvoiceClient;
import com.tradingapp.tradingapp.invoice.dto.InvoiceDTO;
import com.tradingapp.tradingapp.repositories.*;
import com.tradingapp.tradingapp.services.OrderService;
import com.tradingapp.tradingapp.web.dto.OrderDTO;
import com.tradingapp.tradingapp.web.dto.OrderItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private InvoiceClient invoiceClient;

    private Customer testCustomer;
    private Product testProduct;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        orderRepository.deleteAll();
        customerRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user FIRST - this is important!
        testUser = userRepository.save(
                User.builder()
                        .username("testuser")
                        .password(passwordEncoder.encode("password"))
                        .role(UserRole.USER)
                        .email("user@test.com")
                        .active(true)
                        .build()
        );

        // Create test customer
        testCustomer = customerRepository.save(
                Customer.builder()
                        .name("Test Customer")
                        .eik("123456789")
                        .email("test@test.com")
                        .address("Test Address")
                        .build()
        );

        // Create test product
        testProduct = productRepository.save(
                Product.builder()
                        .name("Test Product")
                        .price(BigDecimal.valueOf(10.0))
                        .quantity(100)
                        .category("Test Category")
                        .description("Test Description")
                        .build()
        );
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createOrder_ShouldSaveOrderAndGenerateInvoice() {

        // Arrange
        OrderItemDTO itemDto = OrderItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(2)
                .build();

        OrderDTO orderDTO = OrderDTO.builder()
                .customerId(testCustomer.getId())
                .items(List.of(itemDto))
                .generateInvoice(true)
                .build();

        // Mock the invoice client response
        InvoiceDTO mockInvoiceResponse = InvoiceDTO.builder()
                .id(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .eik(testCustomer.getEik())
                .customerName(testCustomer.getName())
                .totalAmount(BigDecimal.valueOf(20.0))
                .issuedOn(LocalDateTime.now())
                .build();

        when(invoiceClient.createInvoice(any(InvoiceDTO.class)))
                .thenReturn(mockInvoiceResponse);

        // Act
        Order savedOrder = orderService.saveOrder(orderDTO);

        // Assert
        assertNotNull(savedOrder);
        assertNotNull(savedOrder.getId());
        assertEquals(testCustomer.getId(), savedOrder.getCustomer().getId());
        assertEquals(BigDecimal.valueOf(20.0), savedOrder.getTotalPrice());
        assertNotNull(savedOrder.getInvoiceId());
        assertEquals(1, savedOrder.getItems().size());

        // Verify product quantity was updated
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertEquals(98, updatedProduct.getQuantity());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createOrder_WithoutInvoice_ShouldSaveOrder() {
        // Arrange
        OrderItemDTO itemDto = OrderItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(1)
                .build();

        OrderDTO orderDTO = OrderDTO.builder()
                .customerId(testCustomer.getId())
                .items(List.of(itemDto))
                .generateInvoice(false) // No invoice generation
                .build();

        // Act
        Order savedOrder = orderService.saveOrder(orderDTO);

        // Assert
        assertNotNull(savedOrder);
        assertNotNull(savedOrder.getId());
        assertNull(savedOrder.getInvoiceId()); // No invoice ID should be set
        assertEquals(BigDecimal.valueOf(10.0), savedOrder.getTotalPrice());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createOrder_WithInsufficientStock_ShouldThrowException() {

        // Arrange
        OrderItemDTO itemDto = OrderItemDTO.builder()
                .productId(testProduct.getId())
                .quantity(150)
                .build();

        OrderDTO orderDTO = OrderDTO.builder()
                .customerId(testCustomer.getId())
                .items(List.of(itemDto))
                .generateInvoice(false)
                .build();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.saveOrder(orderDTO)
        );

        assertTrue(exception.getMessage().contains("Insufficient availability"));
    }
}

