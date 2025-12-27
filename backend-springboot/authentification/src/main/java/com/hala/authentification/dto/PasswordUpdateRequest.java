package com.hala.authentification.dto;

import lombok.Data;

@Data
public class PasswordUpdateRequest {
    private Integer userId;
    private String oldPassword;
    private String newPassword;
}
