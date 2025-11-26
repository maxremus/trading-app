package org.example.invoiceservice;

import org.example.invoiceservice.entity.Invoice;
import org.example.invoiceservice.repository.InvoiceRepository;
import org.example.invoiceservice.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
public class InvoiceServiceIntegrationTest {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private Invoice testInvoice;

    @BeforeEach
    void setUp() {

        // Clean up before each test
        invoiceRepository.deleteAll();

        // Create test invoice
        testInvoice = invoiceRepository.save(
                Invoice.builder()
                        .orderId(UUID.randomUUID())
                        .eik("123456789")
                        .customerName("Test Customer")
                        .totalAmount(BigDecimal.valueOf(100.0))
                        .issuedOn(LocalDateTime.now())
                        .build()
        );
    }

    @Test
    void getInvoiceById_ShouldReturnInvoice() {

        // Act
        Invoice foundInvoice = invoiceService.getInvoiceById(testInvoice.getId());

        // Assert
        assertNotNull(foundInvoice);
        assertEquals(testInvoice.getId(), foundInvoice.getId());
        assertEquals("Test Customer", foundInvoice.getCustomerName());
        assertEquals(BigDecimal.valueOf(100.0), foundInvoice.getTotalAmount());
    }

    @Test
    void createInvoice_ShouldSaveInvoice() {

        // Arrange
        Invoice newInvoice = Invoice.builder()
                .orderId(UUID.randomUUID())
                .eik("987654321")
                .customerName("New Customer")
                .totalAmount(BigDecimal.valueOf(50.0))
                .issuedOn(LocalDateTime.now())
                .build();

        // Act
        Invoice savedInvoice = invoiceService.createInvoice(newInvoice);

        // Assert
        assertNotNull(savedInvoice);
        assertNotNull(savedInvoice.getId());
        assertEquals("New Customer", savedInvoice.getCustomerName());
        assertEquals(BigDecimal.valueOf(50.0), savedInvoice.getTotalAmount());
    }

    @Test
    void getAllInvoices_ShouldReturnAllInvoices() {

        // Act
        List<Invoice> invoices = invoiceService.getAllInvoices();

        // Assert
        assertNotNull(invoices);
        assertEquals(1, invoices.size());
        assertEquals(testInvoice.getId(), invoices.get(0).getId());
    }

    @Test
    void deleteInvoice_ShouldRemoveInvoice() {

        // Act
        invoiceService.deleteInvoice(testInvoice.getId());

        // Assert
        assertFalse(invoiceRepository.existsById(testInvoice.getId()));
    }
}

