package org.example.invoiceservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.invoiceservice.dto.InvoiceDTO;
import org.example.invoiceservice.entity.Invoice;
import org.example.invoiceservice.service.InvoicePdfService;
import org.example.invoiceservice.service.InvoiceService;
import org.example.invoiceservice.util.InvoiceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
public class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceService invoiceService;

    @MockBean
    private InvoiceMapper invoiceMapper;

    @MockBean
    private InvoicePdfService invoicePdfService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getInvoiceById_WithValidId_ShouldReturnInvoice() throws Exception {

        // Given
        UUID invoiceId = UUID.randomUUID();
        Invoice invoice = createTestInvoice(invoiceId);
        InvoiceDTO invoiceDTO = createTestInvoiceDTO(invoiceId);

        when(invoiceService.getInvoiceById(invoiceId)).thenReturn(invoice);
        when(invoiceMapper.toDto(invoice)).thenReturn(invoiceDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/invoices/{id}", invoiceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invoiceId.toString()))
                .andExpect(jsonPath("$.customerName").value("Test Customer"));
    }

    @Test
    void createInvoice_WithValidData_ShouldReturnCreatedInvoice() throws Exception {

        // Given
        InvoiceDTO requestDTO = createTestInvoiceDTO(null);
        Invoice invoice = createTestInvoice(UUID.randomUUID());
        InvoiceDTO responseDTO = createTestInvoiceDTO(invoice.getId());

        when(invoiceMapper.toEntity(requestDTO)).thenReturn(invoice);
        when(invoiceService.createInvoice(invoice)).thenReturn(invoice);
        when(invoiceMapper.toDto(invoice)).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }


    @Test
    void getAllInvoices_ShouldReturnInvoiceList() throws Exception {

        // Given
        List<Invoice> invoices = List.of(createTestInvoice(UUID.randomUUID()));
        List<InvoiceDTO> invoiceDTOs = List.of(createTestInvoiceDTO(UUID.randomUUID()));

        when(invoiceService.getAllInvoices()).thenReturn(invoices);
        when(invoiceMapper.toDto(any(Invoice.class))).thenReturn(invoiceDTOs.get(0));

        // When & Then
        mockMvc.perform(get("/api/v1/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deleteInvoice_WithValidId_ShouldReturnNoContent() throws Exception {

        // Given
        UUID invoiceId = UUID.randomUUID();
        doNothing().when(invoiceService).deleteInvoice(invoiceId);

        // When & Then
        mockMvc.perform(delete("/api/v1/invoices/{id}", invoiceId))
                .andExpect(status().isNoContent());
    }

    private Invoice createTestInvoice(UUID id) {
        return Invoice.builder()
                .id(id)
                .orderId(UUID.randomUUID())
                .eik("123456789")
                .customerName("Test Customer")
                .totalAmount(new BigDecimal("100.00"))
                .issuedOn(LocalDateTime.now())
                .build();
    }

    private InvoiceDTO createTestInvoiceDTO(UUID id) {
        return InvoiceDTO.builder()
                .id(id)
                .orderId(UUID.randomUUID())
                .eik("123456789")
                .customerName("Test Customer")
                .totalAmount(new BigDecimal("100.00"))
                .issuedOn(LocalDateTime.now())
                .build();
    }

    @Test
    void downloadInvoicePdf_WithNonExistingId_ShouldThrowException() throws Exception {
        // Given
        UUID nonExistingId = UUID.randomUUID();
        when(invoicePdfService.generateInvoicePdf(nonExistingId))
                .thenThrow(new RuntimeException("Invoice not found."));

        // When & Then
        mockMvc.perform(get("/api/v1/invoices/{id}/pdf", nonExistingId))
                .andExpect(status().isInternalServerError());
    }
}

