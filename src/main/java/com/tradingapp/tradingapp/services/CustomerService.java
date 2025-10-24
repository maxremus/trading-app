package com.tradingapp.tradingapp.services;

import com.tradingapp.tradingapp.entities.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    List<Customer> findAll();
    Customer findById(UUID id);
    Customer save(Customer customer);
    void delete(UUID id);
}
