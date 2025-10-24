package com.tradingapp.tradingapp.services.impl;

import com.tradingapp.tradingapp.entities.Customer;
import com.tradingapp.tradingapp.repositories.CustomerRepository;
import com.tradingapp.tradingapp.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Override
    public Customer findById(UUID id) {
        return customerRepository.findById(id).orElseThrow(null);
    }

    @Override
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public void delete(UUID id) {
        customerRepository.deleteById(id);
    }
}
