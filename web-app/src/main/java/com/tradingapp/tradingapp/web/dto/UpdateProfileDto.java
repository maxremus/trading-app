package com.tradingapp.tradingapp.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UpdateProfileDto {

    @NotBlank(message = "Името не може да е празно.")
    private String username;

    @Email(message = "Невалиден имейл адрес.")
    @NotBlank(message = "Имейлът е задължителен.")
    private String email;
}
