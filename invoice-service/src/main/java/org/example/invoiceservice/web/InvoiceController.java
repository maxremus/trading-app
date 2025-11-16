package org.example.invoiceservice.web;

import jakarta.validation.Valid;
import org.example.invoiceservice.dto.InvoiceDTO;
import org.example.invoiceservice.entity.Invoice;
import org.example.invoiceservice.service.InvoicePdfService;
import org.example.invoiceservice.service.InvoiceService;
import org.example.invoiceservice.util.InvoiceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceMapper mapper;
    private final InvoicePdfService invoicePdfService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService, InvoiceMapper mapper, InvoicePdfService invoicePdfService) {
        this.invoiceService = invoiceService;
        this.mapper = mapper;
        this.invoicePdfService = invoicePdfService;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable UUID id) {

        byte[] pdf = invoicePdfService.generateInvoicePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> getInvoiceById(@PathVariable UUID id) {

        Invoice invoice = invoiceService.getInvoiceById(id);
        InvoiceDTO dto = mapper.toDto(invoice);
        return ResponseEntity.ok(dto);
    }


    @PostMapping
    public ResponseEntity<InvoiceDTO> createInvoice(@Valid @RequestBody InvoiceDTO dto) {

        Invoice invoice = mapper.toEntity(dto);
        Invoice savedInvoice = invoiceService.createInvoice(invoice);
        InvoiceDTO responseDto = mapper.toDto(savedInvoice);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices() {

        List<InvoiceDTO> list = invoiceService.getAllInvoices().stream()
                .map(mapper::toDto)
                .toList();

        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable UUID id) {

        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
}
