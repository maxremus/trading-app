package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.entities.Customer;
import com.tradingapp.tradingapp.services.CustomerService;
import com.tradingapp.tradingapp.web.dto.CustomerDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ModelAndView listCustomers() {

        ModelAndView modelAndView = new ModelAndView("customers");
        modelAndView.addObject("customers", customerService.getAllCustomers());
        modelAndView.addObject("activePage", "customers");
        return modelAndView;
    }

    @GetMapping("/add")
    public ModelAndView showAddForm() {

        ModelAndView modelAndView= new ModelAndView("add-customer");
        modelAndView.addObject("customer", new CustomerDTO());

        return modelAndView;
    }

    @PostMapping("/add")
    public ModelAndView addCustomer(@Valid @ModelAttribute("customer") Customer customer,
                                    BindingResult result) {

        if (result.hasErrors()) {
            return new ModelAndView("add-customer");
        }
        customerService.saveCustomer(customer);
        return new ModelAndView("redirect:/customers");
    }

    @GetMapping("/delete/{id}")
    public ModelAndView deleteCustomer(@PathVariable UUID id) {

        customerService.deleteCustomer(id);
        return new ModelAndView("redirect:/customers");
    }
}
