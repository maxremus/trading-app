package com.tradingapp.tradingapp.services.impl;

import com.tradingapp.tradingapp.entities.Product;
import com.tradingapp.tradingapp.repositories.ProductRepository;
import com.tradingapp.tradingapp.services.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product findById(UUID id) {
        return productRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Product not found"));
    }

    @Override
    public Product saveProduct(Product product) {
        System.out.println("🟢 В saveProduct(): " + product.getName());
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(UUID id) {
        productRepository.deleteById(id);
    }
}
