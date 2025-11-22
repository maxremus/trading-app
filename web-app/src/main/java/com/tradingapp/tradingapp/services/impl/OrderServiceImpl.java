package com.tradingapp.tradingapp.services.impl;

import com.tradingapp.tradingapp.entities.*;
import com.tradingapp.tradingapp.invoice.InvoiceClient;
import com.tradingapp.tradingapp.invoice.dto.InvoiceDTO;
import com.tradingapp.tradingapp.repositories.*;
import com.tradingapp.tradingapp.services.OrderService;
import com.tradingapp.tradingapp.web.dto.OrderDTO;
import com.tradingapp.tradingapp.web.dto.OrderItemDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final InvoiceClient invoiceClient;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository, CustomerRepository customerRepository, OrderItemRepository orderItemRepository, UserRepository userRepository, InvoiceClient invoiceClient) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.invoiceClient = invoiceClient;
    }

    @Override
    public List<Order> getAllOrders() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            log.info("The admin '{}' views all orders", authentication.getName());
            return orderRepository.findAllByOrderByCreatedOnDesc();
        }

        String username = authentication.getName();
        log.info("User '{}' reviews his orders", username);
        return orderRepository.findByCreatedBy_UsernameOrderByCreatedOnDesc(username);
    }

    @Override
    public Order findById(UUID id) {

        log.info("Search for an order with ID: {}", id);
        return orderRepository.findById(id).orElseThrow(() -> {
            log.warn("Order with ID {} not found!", id);
            return new RuntimeException("Order not found!");
        });
    }

    @Transactional
    @Override
    public Order saveOrder(OrderDTO orderDTO) {

        log.info("Create a new order for a customer ID: {}", orderDTO.getCustomerId());

        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setCustomer(customer);
        order.setCreatedBy(currentUser);
        order.setCreatedOn(LocalDateTime.now());
        order.setItems(new ArrayList<>());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemDTO itemDto : orderDTO.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getQuantity() < itemDto.getQuantity()) {
                throw new IllegalArgumentException("Insufficient availability for product: " + product.getName());
            }

            product.setQuantity(product.getQuantity() - itemDto.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setTotal(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            orderItem.setOrder(order);

            order.getItems().add(orderItem);
            total = total.add(orderItem.getTotal());
        }

        order.setTotalPrice(total);
        Order savedOrder = orderRepository.save(order);

        //  Ако клиентът е избрал "Издай фактура"
        if (orderDTO.isGenerateInvoice()) {
            try {
                InvoiceDTO invoiceRequest = InvoiceDTO.builder()
                        .orderId(savedOrder.getId())
                        .customerName(savedOrder.getCustomer().getName())
                        .eik(savedOrder.getCustomer().getEik())
                        .totalAmount(savedOrder.getTotalPrice())
                        .build();

                InvoiceDTO invoiceResponse = invoiceClient.createInvoice(invoiceRequest);

                if (invoiceResponse != null && invoiceResponse.getId() != null) {
                    savedOrder.setInvoiceId(invoiceResponse.getId());
                    orderRepository.save(savedOrder);
                    log.info("Invoice created successfully: ID={} for order {}", invoiceResponse.getId(), savedOrder.getId());
                } else {
                    log.warn("invoice-service returned an empty response or ID=null for an order {}", savedOrder.getId());
                }

            } catch (Exception ex) {
                log.error("Error creating invoice: {}", ex.getMessage());
            }
        }

        return savedOrder;
    }

    @Override
    public void deleteOrder(UUID id) {

        log.info("Delete an order with ID: {}", id);
        orderRepository.deleteById(id);
        log.info("Order successfully deleted.");
    }

    @Override
    public Order updateOrder(UUID id, OrderDTO orderDTO) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Customer customer = customerRepository.findById(orderDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        for (OrderItem oldItem : order.getItems()) {
            Product product = oldItem.getProduct();
            product.setQuantity(product.getQuantity() + oldItem.getQuantity());
            productRepository.save(product);
        }

        order.getItems().clear();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemDTO itemDto : orderDTO.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getQuantity() < itemDto.getQuantity()) {
                throw new IllegalArgumentException("Insufficient availability for product: " + product.getName());
            }

            product.setQuantity(product.getQuantity() - itemDto.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setTotal(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));

            order.getItems().add(orderItem);
            total = total.add(orderItem.getTotal());
        }

        order.setCustomer(customer);
        order.setTotalPrice(total);
        order.setCreatedOn(LocalDateTime.now());

        return orderRepository.save(order);
    }

    @Override
    public OrderDTO getOrderAsDto(UUID id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCustomerId(order.getCustomer().getId());

        List<OrderItemDTO> items = new ArrayList<>();
        order.getItems().forEach(i -> {
            OrderItemDTO itemDto = new OrderItemDTO();
            itemDto.setProductId(i.getProduct().getId());
            itemDto.setQuantity(i.getQuantity());
            items.add(itemDto);
        });

        dto.setItems(items);

        return dto;
    }
}
