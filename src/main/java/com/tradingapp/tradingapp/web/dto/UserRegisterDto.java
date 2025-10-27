package com.tradingapp.tradingapp.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
public class UserRegisterDto {

    @NotNull(message = "The username cannot be empty")
    @Size(min = 3, max = 20, message = "The username must be between 3 and 20 characters")
    private String username;

    @NotNull(message = "The password cannot be empty")
    private String password;

    @NotNull(message = "The confirmPassword cannot be empty")
    private String confirmPassword;
}
