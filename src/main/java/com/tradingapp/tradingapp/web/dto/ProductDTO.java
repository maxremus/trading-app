package com.tradingapp.tradingapp.web.dto;


import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductDTO {

    private UUID id;
    private String name;
    private BigDecimal price;
    private int quantity;
    private String category;
    private String description;

}
