package com.tradingapp.tradingapp.web.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
public class OrderDTO {

    private UUID id;
    private UUID customerId;
    private String eik;
    private List<OrderItemDTO> items;
    private boolean generateInvoice;
}
