package com.tradingapp.tradingapp.services;

import com.tradingapp.tradingapp.entities.Customer;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    List<Customer> getAllCustomers();

    Customer getCustomerById(UUID id);

    Customer saveCustomer(Customer customer);

    void deleteCustomer(UUID id);
}
