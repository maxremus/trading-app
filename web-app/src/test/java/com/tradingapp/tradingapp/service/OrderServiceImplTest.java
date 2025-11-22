package com.tradingapp.tradingapp.service;

import com.tradingapp.tradingapp.entities.*;
import com.tradingapp.tradingapp.invoice.InvoiceClient;
import com.tradingapp.tradingapp.invoice.dto.InvoiceDTO;
import com.tradingapp.tradingapp.repositories.*;
import com.tradingapp.tradingapp.services.impl.OrderServiceImpl;
import com.tradingapp.tradingapp.web.dto.OrderDTO;
import com.tradingapp.tradingapp.web.dto.OrderItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;


    @Mock
    private UserRepository userRepository;

    @Mock
    private InvoiceClient invoiceClient;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Customer testCustomer;
    private Product testProduct;
    private User testUser;
    private OrderDTO testOrderDTO;
    private UUID customerId;
    private UUID productId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();
        userId = UUID.randomUUID();

        // Setup test customer
        testCustomer = Customer.builder()
                .id(customerId)
                .name("Test Customer")
                .eik("123456789")
                .email("customer@test.com")
                .address("Test Address")
                .build();

        // Setup test product
        testProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .price(new BigDecimal("25.50"))
                .quantity(100)
                .category("Test Category")
                .description("Test Description")
                .build();

        // Setup test user
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .password("password")
                .role(UserRole.USER)
                .email("user@test.com")
                .active(true)
                .build();

        // Setup test order DTO
        OrderItemDTO itemDTO = OrderItemDTO.builder()
                .productId(productId)
                .quantity(5)
                .build();

        testOrderDTO = OrderDTO.builder()
                .customerId(customerId)
                .items(List.of(itemDTO))
                .generateInvoice(false)
                .build();

        // Setup security context - NO STUBBINGS HERE!
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void saveOrder_WhenValidData_ShouldCreateOrderSuccessfully() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Order savedOrder = Order.builder()
                .id(UUID.randomUUID())
                .customer(testCustomer)
                .createdBy(testUser)
                .totalPrice(new BigDecimal("127.50"))
                .createdOn(LocalDateTime.now())
                .items(new java.util.ArrayList<>())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // When
        Order result = orderService.saveOrder(testOrderDTO);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testCustomer, result.getCustomer());
        assertEquals(testUser, result.getCreatedBy());

        verify(productRepository, times(1)).save(any(Product.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void saveOrder_WhenCustomerNotFound_ShouldThrowException() {
        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> orderService.saveOrder(testOrderDTO));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void saveOrder_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> orderService.saveOrder(testOrderDTO));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void saveOrder_WhenProductNotFound_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> orderService.saveOrder(testOrderDTO));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void saveOrder_WhenInsufficientProductQuantity_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        testProduct.setQuantity(3); // Only 3 available
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.saveOrder(testOrderDTO));
        assertTrue(exception.getMessage().contains("Insufficient availability"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void saveOrder_WithInvoiceGeneration_ShouldCreateInvoice() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        testOrderDTO.setGenerateInvoice(true);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Order savedOrder = Order.builder()
                .id(UUID.randomUUID())
                .customer(testCustomer)
                .createdBy(testUser)
                .totalPrice(new BigDecimal("127.50"))
                .createdOn(LocalDateTime.now())
                .items(new java.util.ArrayList<>())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        InvoiceDTO invoiceResponse = InvoiceDTO.builder()
                .id(UUID.randomUUID())
                .orderId(savedOrder.getId())
                .customerName("Test Customer")
                .eik("123456789")
                .totalAmount(new BigDecimal("127.50"))
                .issuedOn(LocalDateTime.now())
                .build();

        when(invoiceClient.createInvoice(any(InvoiceDTO.class))).thenReturn(invoiceResponse);

        // When
        Order result = orderService.saveOrder(testOrderDTO);

        // Then
        assertNotNull(result);
        verify(invoiceClient, times(1)).createInvoice(any(InvoiceDTO.class));
    }

    @Test
    void getAllOrders_ForAdminUser_ShouldReturnAllOrders() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        Order testOrder = Order.builder()
                .id(UUID.randomUUID())
                .customer(testCustomer)
                .createdBy(testUser)
                .totalPrice(new BigDecimal("100.00"))
                .createdOn(LocalDateTime.now())
                .build();

        when(orderRepository.findAllByOrderByCreatedOnDesc()).thenReturn(List.of(testOrder));

        // When
        List<Order> result = orderService.getAllOrders();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAllByOrderByCreatedOnDesc();
        verify(orderRepository, never()).findByCreatedBy_UsernameOrderByCreatedOnDesc(anyString());
    }

    @Test
    void getAllOrders_ForRegularUser_ShouldReturnUserOrders() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        Order testOrder = Order.builder()
                .id(UUID.randomUUID())
                .customer(testCustomer)
                .createdBy(testUser)
                .totalPrice(new BigDecimal("100.00"))
                .createdOn(LocalDateTime.now())
                .build();

        when(orderRepository.findByCreatedBy_UsernameOrderByCreatedOnDesc("testuser")).thenReturn(List.of(testOrder));

        // When
        List<Order> result = orderService.getAllOrders();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findByCreatedBy_UsernameOrderByCreatedOnDesc("testuser");
        verify(orderRepository, never()).findAllByOrderByCreatedOnDesc();
    }

    @Test
    void findById_WhenOrderExists_ShouldReturnOrder() {
        // Given
        UUID orderId = UUID.randomUUID();
        Order testOrder = Order.builder()
                .id(orderId)
                .customer(testCustomer)
                .createdBy(testUser)
                .totalPrice(new BigDecimal("100.00"))
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When
        Order result = orderService.findById(orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void findById_WhenOrderNotExists_ShouldThrowException() {
        // Given
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> orderService.findById(orderId));
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void deleteOrder_ShouldCallRepositoryDelete() {
        // Given
        UUID orderId = UUID.randomUUID();
        doNothing().when(orderRepository).deleteById(orderId);

        // When
        orderService.deleteOrder(orderId);

        // Then
        verify(orderRepository, times(1)).deleteById(orderId);
    }

    @Test
    void updateOrder_WhenValidData_ShouldUpdateOrderSuccessfully() {
        // Given
        UUID orderId = UUID.randomUUID();

        // Existing order with items
        Order existingOrder = Order.builder()
                .id(orderId)
                .customer(testCustomer)
                .createdBy(testUser)
                .totalPrice(new BigDecimal("100.00"))
                .items(new java.util.ArrayList<>())
                .build();

        OrderItem existingItem = OrderItem.builder()
                .id(UUID.randomUUID())
                .order(existingOrder)
                .product(testProduct)
                .quantity(2)
                .price(new BigDecimal("25.50"))
                .total(new BigDecimal("51.00"))
                .build();

        existingOrder.getItems().add(existingItem);

        // Updated order DTO
        OrderItemDTO updatedItemDTO = OrderItemDTO.builder()
                .productId(productId)
                .quantity(10)
                .build();

        OrderDTO updatedOrderDTO = OrderDTO.builder()
                .customerId(customerId)
                .items(List.of(updatedItemDTO))
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

        // When
        Order result = orderService.updateOrder(orderId, updatedOrderDTO);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(testCustomer, result.getCustomer());

        verify(productRepository, times(2)).save(any(Product.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void getOrderAsDto_WhenOrderExists_ShouldReturnOrderDTO() {
        // Given
        UUID orderId = UUID.randomUUID();

        Order testOrder = Order.builder()
                .id(orderId)
                .customer(testCustomer)
                .createdBy(testUser)
                .totalPrice(new BigDecimal("100.00"))
                .items(new java.util.ArrayList<>())
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(UUID.randomUUID())
                .order(testOrder)
                .product(testProduct)
                .quantity(3)
                .price(new BigDecimal("25.50"))
                .total(new BigDecimal("76.50"))
                .build();

        testOrder.getItems().add(orderItem);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // When
        OrderDTO result = orderService.getOrderAsDto(orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(customerId, result.getCustomerId());
        assertEquals(1, result.getItems().size());
        assertEquals(productId, result.getItems().get(0).getProductId());
        assertEquals(3, result.getItems().get(0).getQuantity());
    }

    @Test
    void getOrderAsDto_WhenOrderNotExists_ShouldThrowException() {
        // Given
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> orderService.getOrderAsDto(orderId));
    }

    @Test
    void saveOrder_WhenInvoiceServiceFails_ShouldStillSaveOrder() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");

        testOrderDTO.setGenerateInvoice(true);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Order savedOrder = Order.builder()
                .id(UUID.randomUUID())
                .customer(testCustomer)
                .createdBy(testUser)
                .totalPrice(new BigDecimal("127.50"))
                .createdOn(LocalDateTime.now())
                .items(new java.util.ArrayList<>())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(invoiceClient.createInvoice(any(InvoiceDTO.class))).thenThrow(new RuntimeException("Invoice service down"));

        // When
        Order result = orderService.saveOrder(testOrderDTO);

        // Then
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(invoiceClient, times(1)).createInvoice(any(InvoiceDTO.class));
    }

    @Test
    void updateOrder_WhenInsufficientQuantity_ShouldThrowException() {
        // Given
        UUID orderId = UUID.randomUUID();

        // Existing order
        Order existingOrder = Order.builder()
                .id(orderId)
                .customer(testCustomer)
                .createdBy(testUser)
                .totalPrice(new BigDecimal("100.00"))
                .items(new java.util.ArrayList<>())
                .build();

        OrderItem existingItem = OrderItem.builder()
                .id(UUID.randomUUID())
                .order(existingOrder)
                .product(testProduct)
                .quantity(2)
                .price(new BigDecimal("25.50"))
                .total(new BigDecimal("51.00"))
                .build();

        existingOrder.getItems().add(existingItem);

        // Updated order DTO with insufficient quantity
        OrderItemDTO updatedItemDTO = OrderItemDTO.builder()
                .productId(productId)
                .quantity(200) // More than available (100)
                .build();

        OrderDTO updatedOrderDTO = OrderDTO.builder()
                .customerId(customerId)
                .items(List.of(updatedItemDTO))
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> orderService.updateOrder(orderId, updatedOrderDTO));
    }
}


