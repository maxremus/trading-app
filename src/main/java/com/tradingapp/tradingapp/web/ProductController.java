package com.tradingapp.tradingapp.web;

import com.tradingapp.tradingapp.entities.Product;
import com.tradingapp.tradingapp.services.ProductService;
import com.tradingapp.tradingapp.web.dto.ProductDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @GetMapping
    public ModelAndView listAllProducts() {

        ModelAndView modelAndView = new ModelAndView("products");
        modelAndView.addObject("products", productService.findAll());
        return modelAndView;

    }

    @GetMapping("/add")
    public ModelAndView showAddForm() {

        ModelAndView modelAndView = new ModelAndView("add-product");
        modelAndView.addObject("product", new ProductDTO());
        return modelAndView;
    }

    @PostMapping("/add")
    public ModelAndView addProduct(@Valid @ModelAttribute("product") ProductDTO productDTO,
                                   BindingResult bindingResult) {

        ModelAndView modelAndView = new ModelAndView("add-product");

        if (bindingResult.hasErrors()) {
            return modelAndView;
        }

        Product product = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .quantity(productDTO.getQuantity())
                .category(productDTO.getCategory())
                .build();

        productService.save(product);
        modelAndView.setViewName("redirect:/products");
        return modelAndView;
    }

    @GetMapping("/delete/{id}")
    public ModelAndView deleteProduct(@PathVariable("id") UUID id) {

        productService.delete(id);
        return new ModelAndView("redirect:/products");
    }
}

