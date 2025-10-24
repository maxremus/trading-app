package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.entities.Order;
import com.tradingapp.tradingapp.entities.Product;
import com.tradingapp.tradingapp.services.CustomerService;
import com.tradingapp.tradingapp.services.OrderService;
import com.tradingapp.tradingapp.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final ProductService productService;
    private final CustomerService customerService;

    @Autowired
    public OrderController(OrderService orderService, ProductService productService, CustomerService customerService) {
        this.orderService = orderService;
        this.productService = productService;
        this.customerService = customerService;
    }


    @GetMapping
    public ModelAndView listOrders() {

        ModelAndView modelAndView = new ModelAndView("orders");
        modelAndView.addObject("orders", orderService.findAll());
        return modelAndView;
    }

    @GetMapping("/add")
    public ModelAndView showAddForm() {

        ModelAndView modelAndView = new ModelAndView("add-order");
        modelAndView.addObject("order", new Order());
        modelAndView.addObject("products", productService.findAll());
        modelAndView.addObject("customers", customerService.findAll());
        return modelAndView;
    }

    @PostMapping("/add")
    public ModelAndView addOrder(@Valid @ModelAttribute("order") Order order,
                                 BindingResult result) {
        if (result.hasErrors()) {
            return new ModelAndView("add-order");
        }

        // Пресмятаме цена = product.price * quantity
        Product product = productService.findById(order.getProduct().getId());
        if (product != null) {
            order.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
        }

        orderService.save(order);
        return new ModelAndView("redirect:/orders");
    }

    @GetMapping("/delete/{id}")
    public ModelAndView deleteOrder(@PathVariable UUID id) {

        orderService.delete(id);
        return new ModelAndView("redirect:/orders");
    }
}
