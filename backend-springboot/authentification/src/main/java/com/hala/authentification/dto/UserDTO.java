package com.hala.authentification.dto;

import com.hala.authentification.enumm.UserRole;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private UserRole role;
    private String profilePicture;
    private String phoneNumber;
    private String companyName;
    private Boolean archived;   // 🔥 ajouté pour l’archivage
    private Boolean enabled;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    private String logo;

// 🔥 ajouté pour le statut accepté/non accepté
}
