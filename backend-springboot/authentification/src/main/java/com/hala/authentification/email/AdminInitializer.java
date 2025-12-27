package com.hala.authentification.email;


import com.hala.authentification.entities.User;
import com.hala.authentification.enumm.UserRole;
import com.hala.authentification.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner addAdminAtStartup() {
        return args -> {
            String adminEmail = "oueslati.hala4@gmail.com";
            String adminPassword = "oueslatihala123";

            boolean adminExists = !userRepository.findByEmail(adminEmail).isEmpty();

            if (!adminExists) {
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(UserRole.ADMIN);  // adapte selon ton enum/entité
                admin.setFirstname("Admin");
                admin.setLastname("User");
                admin.setEnabled(true);  // si tu as un champ enabled ou active

                userRepository.save(admin);

                System.out.println("✅ Admin account created: " + adminEmail);
            } else {
                System.out.println("ℹ Admin account already exists: " + adminEmail);
            }
        };
    }
}

