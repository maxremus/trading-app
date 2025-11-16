package com.tradingapp.tradingapp.services.impl;

import com.tradingapp.tradingapp.entities.Customer;
import com.tradingapp.tradingapp.repositories.CustomerRepository;
import com.tradingapp.tradingapp.repositories.OrderRepository;
import com.tradingapp.tradingapp.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public List<Customer> getAllCustomers() {

        log.info("Retrieving all customers from the database");
        return customerRepository.findAll();
    }

    @Override
    public Customer getCustomerById(UUID id) {

        log.info(" Search for a customer with ID: {}", id);
        return customerRepository.findById(id).orElseThrow(() -> {
            log.warn(" Customer with ID {} not found!", id);
            return new RuntimeException("Customer not found!");
        });
    }

    @Override
    public Customer saveCustomer(Customer customer) {

        log.info("Add a customer: {}", customer.getName());
        Customer saved = customerRepository.save(customer);
        log.info("The client '{}' has been successfully registered with ID: {}", saved.getName(), saved.getId());
        return saved;
    }

    @Override
    public void deleteCustomer(UUID id) {

        if (orderRepository.existsByCustomer_Id(id)) {
            throw new IllegalStateException("The customer has orders and cannot be deleted.");
        }

        customerRepository.deleteById(id);
    }
}
