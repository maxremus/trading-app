package com.tradingapp.tradingapp.services.impl;

import com.tradingapp.tradingapp.entities.Product;
import com.tradingapp.tradingapp.error.ProductNotFoundException;
import com.tradingapp.tradingapp.repositories.ProductRepository;
import com.tradingapp.tradingapp.services.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Cacheable("products")
    public List<Product> getAllProducts() {

        log.info("Retrieve all products from the database");
        return productRepository.findAll();
    }

    @Override
    public Product findById(UUID id) {

        log.info("Search for a product with ID: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(" Product with ID {} not found!", id);
                    return new RuntimeException("Product not found!");
                });
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public Product saveProduct(Product product) {

        log.info("Save product: {}", product.getName());
        return productRepository.save(product);
    }

    @Override
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(UUID id) {

        log.info("Delete a product with ID: {}", id);
        productRepository.deleteById(id);
    }
}
