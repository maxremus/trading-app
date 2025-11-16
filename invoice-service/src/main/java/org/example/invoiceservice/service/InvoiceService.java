package org.example.invoiceservice.service;

import jakarta.transaction.Transactional;
import org.example.invoiceservice.entity.Invoice;
import org.example.invoiceservice.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);
    private final InvoiceRepository repo;

    @Autowired
    public InvoiceService(InvoiceRepository repo) {
        this.repo = repo;
    }

    public Invoice getInvoiceById(UUID id) {

        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + id));
    }

    @CacheEvict(value = "invoices", allEntries = true)
    public Invoice createInvoice(Invoice invoice) {

        log.info("Creating new invoice for customer '{}'", invoice.getCustomerName());
        return repo.save(invoice);
    }

    @Cacheable("invoices")
    public List<Invoice> getAllInvoices() {

        log.info("Fetching all invoices");
        return repo.findAll();
    }

    @CacheEvict(value = "invoices", allEntries = true)
    public void deleteInvoice(UUID id) {

        log.info("Deleting invoice with ID {}", id);
        repo.deleteById(id);
    }
}
