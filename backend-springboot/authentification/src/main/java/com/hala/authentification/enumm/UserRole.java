package com.hala.authentification.enumm;

import java.util.Arrays;
import java.util.List;

public enum UserRole {
    ADMIN("ROLE_ADMIN"),
    EMPLOYE("ROLE_EMPLOYE"),
    PARTENAIRE("ROLE_PARTENAIRE");

    private final String authority;

    UserRole(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }

    // This method returns a list of all authorities/roles
    public static List<UserRole> getAuthorities() {
        return Arrays.asList(UserRole.values());
    }
}
