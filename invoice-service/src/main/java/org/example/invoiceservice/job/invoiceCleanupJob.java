package org.example.invoiceservice.job;

import org.example.invoiceservice.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class invoiceCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(invoiceCleanupJob.class);
    private final InvoiceRepository repo;

    @Autowired
    public invoiceCleanupJob(InvoiceRepository repo) {
        this.repo = repo;
    }

    // Изпълнява се всеки ден в 03:00
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanOldInvoices() {

        log.info("Checking for old invoices...");

        repo.findAll().stream()
                .filter(invoice -> invoice.getIssuedOn()
                        .isBefore(LocalDateTime.now().minus(365, ChronoUnit.DAYS)))
                .forEach(oldInvoice -> {
                    repo.delete(oldInvoice);
                    log.info("Deleted old invoice: {}", oldInvoice.getId());
                });

        log.info("Invoice cleanup completed at {}", LocalDateTime.now());

    }

}
