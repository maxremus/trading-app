package com.tradingapp.tradingapp.web.dto;

import lombok.*;

import java.util.UUID;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {

    private UUID productId;
    private int quantity;
}
