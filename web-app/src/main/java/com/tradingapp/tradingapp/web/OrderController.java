package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.entities.Order;
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

import java.time.LocalDateTime;
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

        orderService.saveOrder(orderDTO);
        return new ModelAndView("redirect:/orders?success");
    }

    @GetMapping("/edit/{id}")
    public ModelAndView showEditForm(@PathVariable UUID id) {
        ModelAndView mv = new ModelAndView("edit-order");

        OrderDTO dto = orderService.getOrderAsDto(id);

        mv.addObject("order", dto);
        mv.addObject("products", productService.getAllProducts());
        mv.addObject("customers", customerService.getAllCustomers());

        return mv;
    }

    @PostMapping("/edit/{id}")
    public ModelAndView updateOrder(@PathVariable UUID id,
                                    @Valid @ModelAttribute("order") OrderDTO orderDTO,
                                    BindingResult result) {

        if (result.hasErrors()) {
            ModelAndView mv = new ModelAndView("edit-order");
            mv.addObject("products", productService.getAllProducts());
            mv.addObject("customers", customerService.getAllCustomers());
            return mv;
        }

        orderService.updateOrder(id, orderDTO);
        return new ModelAndView("redirect:/orders?updated");
    }

    @GetMapping("/delete/{id}")
    public ModelAndView deleteOrder(@PathVariable UUID id) {
        orderService.deleteOrder(id);
        return new ModelAndView("redirect:/orders?deleted");
    }
}
