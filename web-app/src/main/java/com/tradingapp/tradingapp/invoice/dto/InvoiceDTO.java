package com.tradingapp.tradingapp.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDTO {

    private UUID id;
    private UUID orderId;
    private String eik;
    private String customerName;
    private BigDecimal totalAmount;
    private LocalDateTime issuedOn;
}
