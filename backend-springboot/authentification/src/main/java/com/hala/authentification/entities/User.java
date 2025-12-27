package com.hala.authentification.entities;

import com.hala.authentification.enumm.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_test")
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private boolean online;
    private String firstname;
    private String lastname;
    private String profilePicture;
    private String companyName;
    private String phoneNumber;
    private String logo;
    private String email;
    private String password;
    private boolean accountLocked;
    private boolean enabled;
    private boolean isDeleted = false;
    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


    @Column(nullable = false, columnDefinition = "boolean default false")
    private Boolean archived = false;



    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }




    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Override
    public Collection<SimpleGrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getAuthority()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String getFullName() {
        return firstname + " " + lastname;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
}