package com.example.weatherapp.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDto {
    @Size(min = 3)
    private String firstName;
    @Size(min = 3)
    private String lastName;
    private String email;
    @Size(min = 8)
    private String password;
}
