package com.tradingapp.tradingapp.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChangePasswordDto {

    @NotBlank(message = "Моля, въведете текущата парола.")
    private String oldPassword;

    @NotBlank(message = "Моля, въведете нова парола.")
    private String newPassword;

    @NotBlank(message = "Моля, повторете новата парола.")
    private String confirmPassword;
}
