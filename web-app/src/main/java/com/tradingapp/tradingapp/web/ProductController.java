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

    @GetMapping("/edit/{id}")
    public ModelAndView showEditForm(@PathVariable UUID id) {

        ModelAndView modelAndView = new ModelAndView("edit-product");
        Product product = productService.findById(id);
        modelAndView.addObject("product", product);

        return modelAndView;
    }

    @PostMapping("/edit/{id}")
    public ModelAndView updateProduct(@PathVariable UUID id,
                                      @Valid @ModelAttribute("product") ProductDTO productDTO,
                                      BindingResult result) {

        if (result.hasErrors()) {
            return new ModelAndView("edit-product");
        }

        Product existing = productService.findById(id);
        existing.setName(productDTO.getName());
        existing.setCategory(productDTO.getCategory());
        existing.setPrice(productDTO.getPrice());
        existing.setQuantity(productDTO.getQuantity());
        existing.setDescription(productDTO.getDescription());

        productService.saveProduct(existing);

        return new ModelAndView("redirect:/products");
    }


    @GetMapping
    public ModelAndView listAllProducts() {

        ModelAndView modelAndView = new ModelAndView("products");
        modelAndView.addObject("products", productService.getAllProducts());
        modelAndView.addObject("activePage", "products");

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
            return new ModelAndView("add-product");
        }

        Product product = Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .quantity(productDTO.getQuantity())
                .category(productDTO.getCategory())
                .description(productDTO.getDescription())
                .build();

        productService.saveProduct(product);

        return new ModelAndView("redirect:/products");
    }

    @GetMapping("/delete/{id}")
    public ModelAndView deleteProduct(@PathVariable("id") UUID id) {

        productService.deleteProduct(id);

        return new ModelAndView("redirect:/products");
    }
}

