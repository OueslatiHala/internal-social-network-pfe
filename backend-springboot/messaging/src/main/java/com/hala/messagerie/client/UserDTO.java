package com.hala.messagerie.client;
import com.hala.messagerie.enumm.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private String logo;
    public String getUsername() {
        return firstname;
    }
    private String companyName;
}
