package com.tradingapp.tradingapp.invoice;

import com.tradingapp.tradingapp.invoice.dto.InvoiceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "invoice-service", url = "http://localhost:8081/api/v1/invoices")
public interface InvoiceClient {

    @GetMapping("/{id}")
    InvoiceDTO getInvoiceById(@PathVariable("id") UUID id);

    @GetMapping
    List<InvoiceDTO> getAllInvoices();

    @PostMapping
    InvoiceDTO createInvoice(@RequestBody InvoiceDTO invoice);

    @DeleteMapping("/{id}")
    void deleteInvoice(@PathVariable("id") UUID id);

}
