package org.example.invoiceservice.util;

import org.example.invoiceservice.dto.InvoiceDTO;
import org.example.invoiceservice.entity.Invoice;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class InvoiceMapper {

    public Invoice toEntity(InvoiceDTO dto) {

        if (dto == null) return null;
        return Invoice.builder()
                .id(dto.getId())
                .orderId(dto.getOrderId())
                .eik(dto.getEik())
                .customerName(dto.getCustomerName())
                .totalAmount(dto.getTotalAmount())
                .issuedOn(dto.getIssuedOn() != null ? dto.getIssuedOn() : LocalDateTime.now())
                .build();
    }

    public InvoiceDTO toDto(Invoice entity) {

        if (entity == null) return null;
        return InvoiceDTO.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .eik(entity.getEik())
                .customerName(entity.getCustomerName())
                .totalAmount(entity.getTotalAmount())
                .issuedOn(entity.getIssuedOn())
                .build();
    }
}
