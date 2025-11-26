package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.invoice.InvoiceClient;
import com.tradingapp.tradingapp.invoice.dto.InvoiceDTO;
import com.tradingapp.tradingapp.services.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceViewController.class)
public class InvoiceViewControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceClient invoiceClient;

    @MockBean
    private CustomerService customerService;

    private InvoiceDTO testInvoice;
    private UUID invoiceId;

    @BeforeEach
    void setUp() {
        invoiceId = UUID.randomUUID();
        testInvoice = InvoiceDTO.builder()
                .id(invoiceId)
                .orderId(UUID.randomUUID())
                .customerName("Test Customer")
                .eik("123456789")
                .totalAmount(new BigDecimal("100.00"))
                .issuedOn(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser
    void showInvoices_ShouldReturnInvoicesPage() throws Exception {
        when(invoiceClient.getAllInvoices()).thenReturn(List.of(testInvoice));

        mockMvc.perform(get("/invoices"))
                .andExpect(status().isOk())
                .andExpect(view().name("invoices"))
                .andExpect(model().attributeExists("invoices"));
    }

    @Test
    @WithMockUser
    void showInvoiceDetails_WithValidInvoice_ShouldReturnDetailsPage() throws Exception {
        when(invoiceClient.getInvoiceById(invoiceId)).thenReturn(testInvoice);

        mockMvc.perform(get("/invoices/{id}", invoiceId))
                .andExpect(status().isOk())
                .andExpect(view().name("invoice-details"))
                .andExpect(model().attributeExists("invoice"));
    }

    @Test
    @WithMockUser
    void deleteInvoice_ShouldRedirectToInvoices() throws Exception {
        doNothing().when(invoiceClient).deleteInvoice(invoiceId);

        mockMvc.perform(post("/invoices/delete/{id}", invoiceId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/invoices"));

        verify(invoiceClient, times(1)).deleteInvoice(invoiceId);
    }
}

