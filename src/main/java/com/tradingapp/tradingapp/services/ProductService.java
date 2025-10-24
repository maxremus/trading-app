package com.tradingapp.tradingapp.services;

import com.tradingapp.tradingapp.entities.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    List<Product> findAll();
    Product findById(UUID id);
    Product save(Product product);
    void delete(UUID id);
}
