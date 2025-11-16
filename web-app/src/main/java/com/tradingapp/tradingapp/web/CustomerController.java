package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.entities.Customer;
import com.tradingapp.tradingapp.repositories.CustomerRepository;
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
    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerController(CustomerService customerService, CustomerRepository customerRepository) {
        this.customerService = customerService;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/edit/{id}")
    public ModelAndView showEditForm(@PathVariable UUID id) {

        ModelAndView modelAndView = new ModelAndView("edit-customer");
        Customer customer = customerService.getCustomerById(id);
        modelAndView.addObject("customer", customer);

        return modelAndView;
    }

    @PostMapping("/edit/{id}")
    public ModelAndView updateCustomer(@PathVariable UUID id,
                                       @Valid @ModelAttribute("customer") Customer customer,
                                       BindingResult result) {

        if (result.hasErrors()) {
            return new ModelAndView("edit-customer");
        }

        Customer existing = customerService.getCustomerById(id);
        existing.setName(customer.getName());
        existing.setEik(customer.getEik());
        existing.setEmail(customer.getEmail());
        existing.setAddress(customer.getAddress());
        customerService.saveCustomer(existing);

        return new ModelAndView("redirect:/customers");
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

        ModelAndView modelAndView = new ModelAndView("add-customer");

        // Проверка за невалидни полета (валидация от @Size)
        if (result.hasErrors()) {
            modelAndView.addObject("error", "Please fill in all fields correctly.");
            return modelAndView;
        }

        // Проверка дали вече има клиент с този ЕИК
        if (customerRepository.existsByEik(customer.getEik())) {
            result.rejectValue("eik", "error.customer", "A customer with this name already exists. ЕИК!");
            modelAndView.addObject("error", "A customer with this name already exists. ЕИК!");
            return modelAndView;
        }

        // Проверка за дублиран имейл (по желание)
        if (customerRepository.existsByEmail(customer.getEmail())) {
            result.rejectValue("email", "error.customer", "Email is already registered!");
            modelAndView.addObject("error", "Email is already registered!");
            return modelAndView;
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
