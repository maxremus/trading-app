package com.tradingapp.tradingapp.services;

import com.tradingapp.tradingapp.entities.Order;
import com.tradingapp.tradingapp.web.dto.OrderDTO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    List<Order> getAllOrders();

    Order findById(UUID id);

    Order saveOrder(@Valid OrderDTO order);

    void deleteOrder(UUID id);

    Order updateOrder(UUID id, OrderDTO orderDTO);
}
