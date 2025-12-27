package com.hala.post.client;
import com.hala.post.enumm.UserRole;
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
    private String companyName; // ✅ si c'est un partenaire
    private String logo; //


    public void setProfilePhoto(String ProfilePhoto) {
        this.profilePicture = ProfilePhoto;
    }

    public String getUserName() {
        return firstname;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }



} 