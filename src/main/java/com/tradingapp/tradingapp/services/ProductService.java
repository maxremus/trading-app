package com.tradingapp.tradingapp.services;

import com.tradingapp.tradingapp.entities.Product;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    List<Product> getAllProducts();
    Product findById(UUID id);
    Product saveProduct(Product product);
    void deleteProduct(UUID id);
}
