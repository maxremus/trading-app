package com.tradingapp.tradingapp.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private UUID id;

    @NotBlank(message = "Името на продукта е задължително")
    private String name;

    @NotNull(message = "Цената е задължителна")
    @Min(value = 0, message = "Цената не може да бъде отрицателна")
    private BigDecimal price;

    @Min(value = 0, message = "Количеството не може да бъде отрицателно")
    private int quantity;

    private String category;
}
