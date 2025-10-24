package com.tradingapp.tradingapp.services.impl;

import com.tradingapp.tradingapp.entities.Product;
import com.tradingapp.tradingapp.repositories.ProductRepository;
import com.tradingapp.tradingapp.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product findById(UUID id) {
        return productRepository.findById(id).orElseThrow(null);
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void delete(UUID id) {
        productRepository.deleteById(id);
    }
}
