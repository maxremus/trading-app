package com.tradingapp.tradingapp.service;

import com.tradingapp.tradingapp.entities.Customer;
import com.tradingapp.tradingapp.repositories.CustomerRepository;
import com.tradingapp.tradingapp.repositories.OrderRepository;
import com.tradingapp.tradingapp.services.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

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
    void getAllCustomers_ShouldReturnAllCustomers() {

        // Given
        List<Customer> expectedCustomers = List.of(testCustomer);
        when(customerRepository.findAll()).thenReturn(expectedCustomers);

        // When
        List<Customer> actualCustomers = customerService.getAllCustomers();

        // Then
        assertNotNull(actualCustomers);
        assertEquals(1, actualCustomers.size());
        assertEquals("Test Customer", actualCustomers.get(0).getName());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void getCustomerById_WhenCustomerExists_ShouldReturnCustomer() {

        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(testCustomer));

        // When
        Customer result = customerService.getCustomerById(customerId);

        // Then
        assertNotNull(result);
        assertEquals(customerId, result.getId());
        assertEquals("Test Customer", result.getName());
    }

    @Test
    void getCustomerById_WhenCustomerNotExists_ShouldThrowException() {

        // Given
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> customerService.getCustomerById(customerId));
    }

    @Test
    void saveCustomer_ShouldSaveAndReturnCustomer() {

        // Given
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);

        // When
        Customer result = customerService.saveCustomer(testCustomer);

        // Then
        assertNotNull(result);
        assertEquals(customerId, result.getId());
        verify(customerRepository, times(1)).save(testCustomer);
    }

    @Test
    void deleteCustomer_WhenCustomerHasNoOrders_ShouldDeleteCustomer() {

        // Given
        when(orderRepository.existsByCustomer_Id(customerId)).thenReturn(false);
        doNothing().when(customerRepository).deleteById(customerId);

        // When
        customerService.deleteCustomer(customerId);

        // Then
        verify(customerRepository, times(1)).deleteById(customerId);
    }

    @Test
    void deleteCustomer_WhenCustomerHasOrders_ShouldThrowException() {

        // Given
        when(orderRepository.existsByCustomer_Id(customerId)).thenReturn(true);

        // When & Then
        assertThrows(IllegalStateException.class, () -> customerService.deleteCustomer(customerId));
        verify(customerRepository, never()).deleteById(customerId);
    }
}
