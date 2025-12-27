package com.hala.authentification.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordValidationService {

    private final PasswordEncoder passwordEncoder;

    public PasswordValidationService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Valide le mot de passe en clair contre le mot de passe haché stocké.
     *
     * @param rawPassword     Mot de passe en clair fourni par l'utilisateur
     * @param encodedPassword Mot de passe haché stocké dans la base de données
     * @return true si le mot de passe correspond, false sinon
     */
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public static void main(String[] args) {
        PasswordValidationService service = new PasswordValidationService();

        // Mot de passe haché stocké
        String storedHashedPassword = "$2a$10$RJ5UzFPvKsC4m4DIBgpatuNRu.bMaNJHJ9QDuoT4fYzS1.i2xR99e";

        // Mot de passe fourni par l'utilisateur
        String requestPassword = "oueslatihala123";

        // Validation du mot de passe
        boolean matches = service.validatePassword(requestPassword, storedHashedPassword);

        System.out.println("Password matches: " + matches);
    }
}