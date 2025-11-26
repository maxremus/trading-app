package org.example.invoiceservice.service;

import org.example.invoiceservice.entity.Invoice;
import org.example.invoiceservice.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        invoiceService = new InvoiceService(invoiceRepository);
    }

    @Test
    void getInvoiceById_WithExistingId_ShouldReturnInvoice() {

        // Given
        UUID invoiceId = UUID.randomUUID();
        Invoice expectedInvoice = createTestInvoice(invoiceId);

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(expectedInvoice));

        // When
        Invoice result = invoiceService.getInvoiceById(invoiceId);

        // Then
        assertNotNull(result);
        assertEquals(invoiceId, result.getId());
        verify(invoiceRepository, times(1)).findById(invoiceId);
    }

    @Test
    void getInvoiceById_WithNonExistingId_ShouldThrowException() {

        // Given
        UUID nonExistingId = UUID.randomUUID();
        when(invoiceRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> invoiceService.getInvoiceById(nonExistingId));

        assertEquals("Invoice not found: " + nonExistingId, exception.getMessage());
    }

    @Test
    void createInvoice_WithValidInvoice_ShouldSaveAndReturn() {

        // Given
        Invoice invoiceToSave = createTestInvoice(null);
        Invoice savedInvoice = createTestInvoice(UUID.randomUUID());

        when(invoiceRepository.save(invoiceToSave)).thenReturn(savedInvoice);

        // When
        Invoice result = invoiceService.createInvoice(invoiceToSave);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        verify(invoiceRepository, times(1)).save(invoiceToSave);
    }

    @Test
    void getAllInvoices_ShouldReturnAllInvoices() {

        // Given
        List<Invoice> expectedInvoices = List.of(
                createTestInvoice(UUID.randomUUID()),
                createTestInvoice(UUID.randomUUID())
        );

        when(invoiceRepository.findAll()).thenReturn(expectedInvoices);

        // When
        List<Invoice> result = invoiceService.getAllInvoices();

        // Then
        assertEquals(2, result.size());
        verify(invoiceRepository, times(1)).findAll();
    }

    @Test
    void deleteInvoice_WithValidId_ShouldDelete() {

        // Given
        UUID invoiceId = UUID.randomUUID();
        doNothing().when(invoiceRepository).deleteById(invoiceId);

        // When
        invoiceService.deleteInvoice(invoiceId);

        // Then
        verify(invoiceRepository, times(1)).deleteById(invoiceId);
    }

    private Invoice createTestInvoice(UUID id) {
        return Invoice.builder()
                .id(id)
                .orderId(UUID.randomUUID())
                .eik("123456789")
                .customerName("Test Customer")
                .totalAmount(new BigDecimal("99.99"))
                .issuedOn(LocalDateTime.now())
                .build();
    }
}

