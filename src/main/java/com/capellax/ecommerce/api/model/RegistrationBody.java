package com.capellax.ecommerce.api.model;

import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class RegistrationBody {


    private String username;

    @Email
    private String email;


    private String password;


    private String firstName;


    private String lastName;

}
