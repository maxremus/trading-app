package com.tradingapp.tradingapp.services.impl;

import com.tradingapp.tradingapp.entities.Customer;
import com.tradingapp.tradingapp.entities.Order;
import com.tradingapp.tradingapp.entities.OrderItem;
import com.tradingapp.tradingapp.entities.Product;
import com.tradingapp.tradingapp.repositories.CustomerRepository;
import com.tradingapp.tradingapp.repositories.OrderItemRepository;
import com.tradingapp.tradingapp.repositories.OrderRepository;
import com.tradingapp.tradingapp.repositories.ProductRepository;
import com.tradingapp.tradingapp.services.OrderService;
import com.tradingapp.tradingapp.web.dto.OrderDTO;
import com.tradingapp.tradingapp.web.dto.OrderItemDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository, CustomerRepository customerRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order findById(UUID id) {
        return orderRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Order not found"));
    }

    @Transactional
    @Override
    public Order saveOrder(OrderDTO orderDTO) {
        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Клиентът не е намерен"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setCreatedOn(LocalDateTime.now());
        order.setItems(new ArrayList<>());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemDTO itemDto : orderDTO.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Продуктът не е намерен"));

            if (product.getQuantity() < itemDto.getQuantity()) {
                throw new IllegalArgumentException("Недостатъчна наличност за продукт: " + product.getName());
            }

            // актуализираме наличността
            product.setQuantity(product.getQuantity() - itemDto.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setTotal(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            orderItem.setOrder(order); // задаваме връзка

            order.getItems().add(orderItem);
            total = total.add(orderItem.getTotal());
        }

        order.setTotalPrice(total);

        return orderRepository.save(order);
    }


    @Override
    public void deleteOrder(UUID id) {
        orderRepository.deleteById(id);
    }
}
