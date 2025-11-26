package org.example.invoiceservice.service;

import org.example.invoiceservice.entity.Invoice;
import org.example.invoiceservice.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvoicePdfServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    private InvoicePdfService pdfService;

    @Test
    void generateInvoicePdf_WithValidId_ShouldReturnPdfBytes() {

        // Given
        pdfService = new InvoicePdfService(invoiceRepository);
        UUID invoiceId = UUID.randomUUID();

        Invoice invoice = Invoice.builder()
                .id(invoiceId)
                .orderId(UUID.randomUUID())
                .eik("123456789")
                .customerName("Тест Клиент")
                .totalAmount(new BigDecimal("150.75"))
                .issuedOn(LocalDateTime.now())
                .build();

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(invoice));

        // When
        byte[] result = pdfService.generateInvoicePdf(invoiceId);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
        verify(invoiceRepository, times(1)).findById(invoiceId);
    }

    @Test
    void generateInvoicePdf_WithNonExistingId_ShouldThrowException() {

        // Given
        pdfService = new InvoicePdfService(invoiceRepository);
        UUID nonExistingId = UUID.randomUUID();
        when(invoiceRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> pdfService.generateInvoicePdf(nonExistingId));

        assertEquals("Invoice not found.", exception.getMessage());
    }
}

