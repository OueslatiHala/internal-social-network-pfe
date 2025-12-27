package com.hala.authentification.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import com.hala.authentification.config.JwtService;
import com.hala.authentification.email.EmailDetails;
import com.hala.authentification.email.EmailService;
import com.hala.authentification.email.RandomPassword;
import com.hala.authentification.entities.User;
import com.hala.authentification.enumm.UserRole;
import com.hala.authentification.mappers.UserMapper;
import com.hala.authentification.repository.UserRepository;
import com.hala.authentification.token.Token;
import com.hala.authentification.token.TokenRepository;
import com.hala.authentification.token.TokenType;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final RandomPassword randomPassword;
    private final AuthenticationProvider authenticationProvider; // Injected here

    private final UserMapper userMapper;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);


    public AuthenticationResponse register(RegisterRequest request) throws MessagingException {
        UserRole userRole = UserRole.PARTENAIRE;

        logger.info("Attempting to register user with email: {}", request.getEmail());

        if (UserRole.ADMIN.equals(userRole)) {
            throw new IllegalStateException("Admins cannot register themselves.");
        }

        if (userRole == UserRole.PARTENAIRE && adminAlreadyRegistered()) {
            logger.warn("Admin already registered, registration pending approval.");
        }


        User user = createUserFromRequest(request, userRole);
        String generatedPassword = randomPassword.generateRandomPassword(10);
        System.out.println("🔐 Mot de passe en clair pour " + user.getEmail() + " : " + generatedPassword);

        user.setPassword(passwordEncoder.encode(generatedPassword));


        User savedUser = repository.save(user);


        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        if (userRole == UserRole.PARTENAIRE) {
            sendAccessPermissionEmail("oueslati.hala4@gmail.com", user.getEmail(), userRole);
        }

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .userId(savedUser.getId())
                .build();
    }





    private boolean adminAlreadyRegistered() {
        boolean isAdminRegistered = repository.findFirstByRole(UserRole.ADMIN).isPresent();

        logger.info("Admin already registered: {}", isAdminRegistered);
        return isAdminRegistered;
    }

    private User createUserFromRequest(RegisterRequest request, UserRole userRole) {
        if (userRole == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }
        String logo = request.getLogo();
        if (logo != null && !logo.startsWith("/profile-photos/")) {
            logo = "/profile-photos/" + logo;
        }

        String rawPassword = request.getPassword();
        String encodedPassword;

        // Si le password n’est pas fourni ou vide → générer un random
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            String generatedPassword = randomPassword.generateRandomPassword(10);
            encodedPassword = passwordEncoder.encode(generatedPassword);
            logger.info("Generated random password for user: {}", generatedPassword);

            // Optionnel : envoyer cet info par mail ici si nécessaire !
        } else {
            encodedPassword = passwordEncoder.encode(rawPassword);
        }

        return User
                .builder()
                .companyName(request.getCompanyName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .logo(logo)
                .accountLocked(false)
                .role(userRole)
                .password(encodedPassword)
                .archived(false)  // ← 🔥 ajoute ça explicitement !
                .build();


    }




    public AuthenticationResponse createEmployee(RegisterRequest request, UserRole userRole) throws MessagingException {
        if (userRole != UserRole.EMPLOYE) {
            throw new IllegalStateException("Only employees can be created with this method.");
        }

        User user = createUserFromRequest(request, userRole);
        String generatedPassword = randomPassword.generateRandomPassword(10);
        user.setPassword(passwordEncoder.encode(generatedPassword));

        repository.save(user);

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }


    public void validateUser(String userEmail) throws MessagingException {
        List<User> users = repository.findAllByEmail(userEmail);

        // Log intermediate values
        System.out.println("Number of users found with email " + userEmail + ": " + users.size());

        if (users.size() != 1) {
            throw new IllegalStateException("Expected one user, but found " + users.size());
        }

        User user = users.get(0);

        // If the user is not yet activated
        if (!user.isEnabled()) {
            String generatedPassword = randomPassword.generateRandomPassword(10);
            user.setPassword(passwordEncoder.encode(generatedPassword));
            user.setEnabled(true);
            repository.save(user);

            System.out.println("Sending access email to " + user.getEmail());
            sendAccessEmail(user.getEmail(), generatedPassword);
            System.out.println("Access email sent successfully");
        }
    }



    private User getUserByEmail(String email) {
        List<User> users = repository.findByEmail(email);
        if (users.size() != 1) {
            throw new UsernameNotFoundException("Utilisateur non trouvé ou résultats multiples trouvés pour l'email : " + email);
        }
        return users.get(0);
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = getUserByEmail(request.getEmail());
        if (user == null) {
            throw new IllegalArgumentException("User not found with email: " + request.getEmail());
        }

        System.out.println("User found: " + user.getEmail());

        if (user.getRole() == null) {
            throw new IllegalStateException("User role is null");
        }

        // Stockez le mot de passe haché
        System.out.println("Stored password (hashed): " + user.getPassword());
        // Mot de passe fourni par l'utilisateur
        System.out.println("Request password: " + request.getPassword());

        // Comparez le mot de passe fourni avec le mot de passe haché stocké
        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        System.out.println("Password matches: " + matches);
        if (!matches) {
            throw new IllegalArgumentException("Password mismatch for user: " + user.getEmail());
        }

        user.setOnline(true); // Mettre l'utilisateur en ligne
        repository.save(user); // Sauvegarder les modifications

        Map<String, Object> claims = new HashMap<>();
        claims.put("fullName", user.getFullName());
        String jwtToken = jwtService.generateToken(claims, user);
        String refreshToken = jwtService.generateRefreshToken(user);
        System.out.println("JWT Token: " + jwtToken);
        System.out.println("Refresh Token: " + refreshToken);

        String message = "Authentication successful";

        AuthenticationResponse response = AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .companyName(user.getCompanyName())
                .role(user.getRole().name())
                .profilePicture(user.getProfilePicture())
                .logo(user.getLogo())
                .user(userMapper.userToUserDto(user)) // ✅ indispensable côté Angular
                .build();


        if (isAdministrator(request)) {
            return response;
        } else {
            if (user.getRole() == UserRole.PARTENAIRE || user.getRole() == UserRole.EMPLOYE) {
                user.setEnabled(true);
                repository.save(user);
            }

            revokeAllUserTokens(user);
            saveUserToken(user, jwtToken);

            return response;
        }
    }

    public void forgotPassword(String email) throws MessagingException {
        List<User> users = repository.findByEmail(email);
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("No user found with this email: " + email);
        }

        User user = users.get(0);
        String resetToken = jwtService.generateToken(user);

        String resetLink = "http://localhost:4200/reset-password?token=" + resetToken;

        EmailDetails details = new EmailDetails();
        details.setRecipient(email);
        details.setSubject("Password Reset Request");
        details.setMsgBody(
                "Hello,\n\n" +
                        "We received a request to reset your password.\n" +
                        "Click the link below to create a new password:\n\n" +
                        resetLink + "\n\n" +
                        "If you did not request a password reset, please ignore this email.\n\n" +
                        "Best regards,\nThe Support Team."
        );

        emailService.sendSimpleMail(details);
    }

    public void resetPassword(String token, String newPassword) {
        String userEmail = jwtService.extractUsername(token);
        if (userEmail == null) {
            throw new IllegalArgumentException("Token invalide");
        }

        List<User> users = repository.findByEmail(userEmail);
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("Utilisateur introuvable");
        }

        User user = users.get(0);
        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);
    }




    public void logout(Integer userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        user.setOnline(false); // Set user's online status to false
        repository.save(user);
    }


    private boolean isAdministrator(AuthenticationRequest request) {
        User user = getUserByEmail(request.getEmail());
        return user.getRole() == UserRole.ADMIN;
    }


    private void saveUserToken(User user, String jwtToken) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty()) return;

        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader("Authorization");
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        // Extraire le token de rafraîchissement du header Authorization
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            // ⚠️ Correction ici : findByEmail retourne une List<User> !
            List<User> users = repository.findByEmail(userEmail);
            if (users.size() == 1) {
                User user = users.get(0);
                if (jwtService.isTokenValid(refreshToken, user)) {
                    // Générer un nouveau token d'accès
                    String accessToken = jwtService.generateToken(new HashMap<>(), user);
                    // Construire la réponse d'authentification
                    AuthenticationResponse authResponse = AuthenticationResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .userId(user.getId())
                            .firstname(user.getFirstname()) // ⚠️ vérifier que c'est bien getFirstname()
                            .lastname(user.getLastname())   // idem
                            .companyName(user.getCompanyName())
                            .role(user.getRole().name())   // ⚠️ String attendu, donc .name()
                            .profilePicture(user.getProfilePicture())
                            .logo(user.getLogo())
                            .build();
                    // Envoyer la réponse JSON
                    new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
                }
            }
        }
    }



    private void sendAccessEmail(String userEmail, String password) throws MessagingException {
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient(userEmail);
        emailDetails.setSubject("New User Access Information");
        emailDetails.setMsgBody("Hello,\n\nA new user has been registered with the following credentials:\n\n"
                + "Email: " + userEmail + "\n"
                + "Password: " + password + "\n\n"
                + "Please use these credentials to log in.\n\n"
                + "Thank you.");

        emailService.sendSimpleMail(emailDetails);
    }

    private void sendAccessPermissionEmail(String adminEmail, String userEmail, UserRole userRole) throws MessagingException {
        EmailDetails details = new EmailDetails();
        details.setRecipient(adminEmail);
        details.setSubject("Access Request: " + userRole.name());
        details.setMsgBody("The user with the email address " + userEmail + " has requested access as a " + userRole.name() + ".\n\n"
                + "Please review and approve or deny this request accordingly.\n\n"
                + "Best regards,\nThe Authentication Team");

        emailService.sendSimpleMail(details);
    }

    public List<User> getPendingUsers() {
        List<User> pendingUsers = repository.findByEnabled(false);
        List<User> acceptedUsers = repository.findByEnabled(true);
        pendingUsers.addAll(acceptedUsers); // Inclure les utilisateurs acceptés
        return pendingUsers;
    }

    public Page<User> getAllPartners(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByRole(UserRole.PARTENAIRE, pageable);
    }

}
