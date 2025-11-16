package com.tradingapp.tradingapp.web.dto;

import lombok.*;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {

    private String name;
    private String eik;
    private String email;
    private String address;
}
