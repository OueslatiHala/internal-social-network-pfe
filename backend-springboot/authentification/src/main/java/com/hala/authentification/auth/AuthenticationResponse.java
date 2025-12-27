package com.hala.authentification.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hala.authentification.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    private String message;
    private Integer userId;
    private String firstname;
    private String lastname;
    private String companyName;
    private String role;
    private String profilePicture;
    private String logo;
    private UserDTO user;  // ou simplement User si tu préfères

    public static AuthenticationResponse errorResponse(String message) {
        return AuthenticationResponse.builder()
                .message(message)
                .build();
    }
}

