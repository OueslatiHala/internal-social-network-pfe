package com.hala.authentification.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotEmpty(message = "Company name is mandatory")
    private String companyName;

    @Email(message = "Email is not well formatted")
    @NotEmpty(message = "Email is mandatory")
    private String email;

    @NotEmpty(message = "Phone number is mandatory")
    private String phoneNumber;

    @NotEmpty(message = "Logo is mandatory")
    private String logo;

    @Schema(description = "Optional password; leave blank for random generation", example = "")
    private String password;

}

