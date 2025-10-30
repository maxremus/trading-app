package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.services.CustomerService;
import com.tradingapp.tradingapp.services.OrderService;
import com.tradingapp.tradingapp.services.ProductService;
import com.tradingapp.tradingapp.web.dto.OrderDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

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
        ModelAndView mv = new ModelAndView("orders");
        mv.addObject("orders", orderService.getAllOrders());
        return mv;
    }

    @GetMapping("/add")
    public ModelAndView showAddForm() {
        ModelAndView mv = new ModelAndView("add-order");
        mv.addObject("order", new OrderDTO());
        mv.addObject("products", productService.getAllProducts());
        mv.addObject("customers", customerService.getAllCustomers());
        return mv;
    }

    @PostMapping("/add")
    public ModelAndView addOrder(@Valid @ModelAttribute("order") OrderDTO orderDTO,
                                 BindingResult result) {

        if (result.hasErrors()) {
            ModelAndView mv = new ModelAndView("add-order");
            mv.addObject("products", productService.getAllProducts());
            mv.addObject("customers", customerService.getAllCustomers());
            return mv;
        }

        try {
            orderService.saveOrder(orderDTO);
        } catch (IllegalArgumentException e) {
            ModelAndView mv = new ModelAndView("add-order");
            mv.addObject("error", e.getMessage());
            mv.addObject("products", productService.getAllProducts());
            mv.addObject("customers", customerService.getAllCustomers());
            return mv;
        }

        return new ModelAndView("redirect:/orders");
    }
}
