package com.hala.authentification.controller;
import com.hala.authentification.auth.AuthenticationRequest;
import com.hala.authentification.auth.AuthenticationResponse;
import com.hala.authentification.auth.AuthenticationService;
import com.hala.authentification.auth.RegisterRequest;
import com.hala.authentification.entities.User;
import com.hala.authentification.enumm.UserRole;
import com.hala.authentification.repository.UserRepository;
import com.hala.authentification.service.StorageService;
import com.hala.authentification.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {
    private final StorageService storageService;
    private final AuthenticationService authService;
    private final UserService userService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request) {
        try {
            AuthenticationResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    AuthenticationResponse.builder()
                            .message(e.getMessage())
                            .accessToken(null)
                            .refreshToken(null)
                            .build()
            );
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthenticationResponse.builder()
                            .message("Failed to send email")
                            .accessToken(null)
                            .refreshToken(null)
                            .build()
            );
        } catch (Exception e) {
            log.error("Registration error", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    AuthenticationResponse.builder()
                            .message("Registration failed due to an unexpected error")
                            .accessToken(null)
                            .refreshToken(null)
                            .build()
            );
        }
    }
    @GetMapping("/users/ids-by-role")
    public ResponseEntity<List<Integer>> getUserIdsByRole(@RequestParam UserRole role) {
        List<Integer> ids = userRepository.findIdsByRole(role);
        return ResponseEntity.ok(ids);
    }


    @GetMapping("/downloadFile/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        if (file.exists() && file.isReadable()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/all-ids")
    public List<Integer> getAllUserIds() {
        return userService.getAllUserIds(); // À implémenter dans le service
    }


    @PostMapping("/create-employee")
    public ResponseEntity<AuthenticationResponse> createEmployee(
            @RequestBody RegisterRequest request) {
        try {
            AuthenticationResponse response = authService.createEmployee(request, UserRole.EMPLOYE);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    AuthenticationResponse.builder()
                            .message(e.getMessage())
                            .build()
            );
        } catch (MessagingException e) {
            log.error("Failed to send email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    AuthenticationResponse.builder()
                            .message("Failed to send email")
                            .build()
            );
        } catch (Exception e) {
            log.error("Error creating employee", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    AuthenticationResponse.builder()
                            .message("Failed to create employee due to an unexpected error")
                            .build()
            );
        }
    }



    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        try {
            AuthenticationResponse response = authService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    AuthenticationResponse.builder()
                            .message("Authentication failed")
                            .build()
            );
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam Integer userId) {
        authService.logout(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authService.refreshToken(request, response);
    }


    @PostMapping("/validate-user")
    public ResponseEntity<String> validateUser(@RequestParam String userEmail) {
        try {
            authService.validateUser(userEmail);
            return ResponseEntity.ok("User validated successfully and email sent");
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body("Failed to send email");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body("User not found");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @GetMapping("/pending-users")
    public ResponseEntity<List<User>> getPendingUsers() {
        List<User> pendingUsers = authService.getPendingUsers();
        return ResponseEntity.ok(pendingUsers);
    }
    @GetMapping("/all-partners")
    public ResponseEntity<Map<String, Object>> getAllPartners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<User> pageUsers = authService.getAllPartners(page, size);
        Map<String, Object> response = new HashMap<>();
        response.put("users", pageUsers.getContent());
        response.put("totalUsers", pageUsers.getTotalElements());
        response.put("totalPages", pageUsers.getTotalPages());
        response.put("currentPage", pageUsers.getNumber());

        return ResponseEntity.ok(response);
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam("email") String email) throws MessagingException {
        authService.forgotPassword(email);
        return ResponseEntity.ok("Un email de réinitialisation a été envoyé à " + email);
    }
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam("token") String token,
            @RequestParam("newPassword") String newPassword) {

        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Mot de passe réinitialisé avec succès.");
    }

}