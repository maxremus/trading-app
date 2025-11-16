package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.invoice.InvoiceClient;
import com.tradingapp.tradingapp.invoice.dto.InvoiceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/invoices")
public class InvoiceViewController {

    private final InvoiceClient invoiceClient;

    @Autowired
    public InvoiceViewController(InvoiceClient invoiceClient) {
        this.invoiceClient = invoiceClient;
    }

    @GetMapping
    public ModelAndView showInvoices() {

        List<InvoiceDTO> invoices = invoiceClient.getAllInvoices();
        ModelAndView modelAndView = new ModelAndView("invoices");
        modelAndView.addObject("invoices", invoices);

        return modelAndView;
    }

    //  Детайли за конкретна фактура
    @GetMapping("/{id}")
    public ModelAndView showInvoiceDetails(@PathVariable UUID id) {

        InvoiceDTO invoice = invoiceClient.getInvoiceById(id);
        if (invoice == null) {
            ModelAndView errorView = new ModelAndView("error");
            errorView.addObject("message", "Invoice not found!");
            return errorView;
        }

        ModelAndView modelAndView = new ModelAndView("invoice-details");
        modelAndView.addObject("invoice", invoice);

        return modelAndView;
    }

    // Изтриване на фактура
    @PostMapping("/delete/{id}")
    public String deleteInvoice(@PathVariable UUID id) {

        invoiceClient.deleteInvoice(id);

        return "redirect:/invoices";
    }
}
