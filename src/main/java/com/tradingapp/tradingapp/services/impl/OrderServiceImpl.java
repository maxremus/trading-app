package com.tradingapp.tradingapp.services.impl;

import com.tradingapp.tradingapp.entities.Order;
import com.tradingapp.tradingapp.repositories.OrderRepository;
import com.tradingapp.tradingapp.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order findById(UUID id) {
        return orderRepository.findById(id).orElseThrow(null);
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public void delete(UUID id) {
        orderRepository.deleteById(id);
    }
}
