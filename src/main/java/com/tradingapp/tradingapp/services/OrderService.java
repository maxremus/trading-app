package com.tradingapp.tradingapp.services;

import com.tradingapp.tradingapp.entities.Customer;
import com.tradingapp.tradingapp.entities.Order;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    List<Order> findAll();
    Order findById(UUID id);
    Order save(Order order);
    void delete(UUID id);
}
