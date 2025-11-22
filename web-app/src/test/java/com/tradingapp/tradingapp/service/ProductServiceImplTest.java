package com.tradingapp.tradingapp.service;

import com.tradingapp.tradingapp.entities.Product;
import com.tradingapp.tradingapp.repositories.ProductRepository;
import com.tradingapp.tradingapp.services.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        testProduct = Product.builder()
                .id(productId)
                .name("Test Product")
                .price(new BigDecimal("19.99"))
                .quantity(10)
                .category("Electronics")
                .description("Test description")
                .build();
    }

    @Test
    void getAllProducts_ShouldReturnAllProducts() {

        // Given
        List<Product> expectedProducts = List.of(testProduct);
        when(productRepository.findAll()).thenReturn(expectedProducts);

        // When
        List<Product> actualProducts = productService.getAllProducts();

        // Then
        assertNotNull(actualProducts);
        assertEquals(1, actualProducts.size());
        assertEquals("Test Product", actualProducts.get(0).getName());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void findById_WhenProductExists_ShouldReturnProduct() {

        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

        // When
        Product result = productService.findById(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Test Product", result.getName());
    }

    @Test
    void findById_WhenProductNotExists_ShouldThrowException() {

        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> productService.findById(productId));
    }

    @Test
    void saveProduct_ShouldSaveAndReturnProduct() {

        // Given
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // When
        Product result = productService.saveProduct(testProduct);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void deleteProduct_ShouldCallRepositoryDelete() {

        // Given
        doNothing().when(productRepository).deleteById(productId);

        // When
        productService.deleteProduct(productId);

        // Then
        verify(productRepository, times(1)).deleteById(productId);
    }

}
