package org.example.invoiceservice.util;

import org.example.invoiceservice.dto.InvoiceDTO;
import org.example.invoiceservice.entity.Invoice;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class InvoiceMapperTest {

    private final InvoiceMapper mapper = new InvoiceMapper();

    @Test
    void toEntity_WithValidDto_ShouldReturnEntity() {

        // Given
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        InvoiceDTO dto = InvoiceDTO.builder()
                .id(id)
                .orderId(orderId)
                .eik("123456789")
                .customerName("Test Customer")
                .totalAmount(new BigDecimal("100.00"))
                .issuedOn(now)
                .build();

        // When
        Invoice entity = mapper.toEntity(dto);

        // Then
        assertNotNull(entity);
        assertEquals(id, entity.getId());
        assertEquals(orderId, entity.getOrderId());
        assertEquals("123456789", entity.getEik());
        assertEquals("Test Customer", entity.getCustomerName());
        assertEquals(new BigDecimal("100.00"), entity.getTotalAmount());
        assertEquals(now, entity.getIssuedOn());
    }

    @Test
    void toEntity_WithNullDto_ShouldReturnNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntity_WithoutIssuedOn_ShouldSetCurrentTime() {

        // Given
        InvoiceDTO dto = InvoiceDTO.builder()
                .orderId(UUID.randomUUID())
                .eik("123456789")
                .customerName("Test Customer")
                .totalAmount(new BigDecimal("100.00"))
                .issuedOn(null)
                .build();

        // When
        Invoice entity = mapper.toEntity(dto);

        // Then
        assertNotNull(entity.getIssuedOn());
    }

    @Test
    void toDto_WithValidEntity_ShouldReturnDto() {

        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Invoice entity = Invoice.builder()
                .id(id)
                .orderId(UUID.randomUUID())
                .eik("123456789")
                .customerName("Test Customer")
                .totalAmount(new BigDecimal("150.50"))
                .issuedOn(now)
                .build();

        // When
        InvoiceDTO dto = mapper.toDto(entity);

        // Then
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals("123456789", dto.getEik());
        assertEquals("Test Customer", dto.getCustomerName());
        assertEquals(new BigDecimal("150.50"), dto.getTotalAmount());
        assertEquals(now, dto.getIssuedOn());
    }

    @Test
    void toDto_WithNullEntity_ShouldReturnNull() {
        assertNull(mapper.toDto(null));
    }
}

