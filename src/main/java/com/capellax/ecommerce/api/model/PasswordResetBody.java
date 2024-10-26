package com.capellax.ecommerce.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetBody {

    @NotBlank
    @NotNull
    private String token;

    @NotNull
    @NotBlank
    @Size(min = 6, max = 32)
    private String password;



}
