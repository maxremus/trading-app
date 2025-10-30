package com.tradingapp.tradingapp.web.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private UUID customerId;
    private List<OrderItemDTO> items;
}
