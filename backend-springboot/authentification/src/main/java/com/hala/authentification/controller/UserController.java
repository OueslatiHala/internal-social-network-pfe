package com.hala.authentification.controller;

import com.hala.authentification.config.JwtService;
import com.hala.authentification.dto.PasswordUpdateRequest;
import com.hala.authentification.dto.UserDTO;
import com.hala.authentification.entities.User;
import com.hala.authentification.enumm.UserRole;
import com.hala.authentification.mappers.UserMapper;
import com.hala.authentification.repository.UserRepository;
import com.hala.authentification.service.UserService;
import com.hala.post.service.PostService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final Path rootLocation = Paths.get("profile-photos");
    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    public UserController(UserService userService, JwtService jwtService, UserMapper userMapper, UserRepository userRepository) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        createDirectory();
    }

    private void createDirectory() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }
    @GetMapping("/recent-users")
    public ResponseEntity<List<UserDTO>> getRecentUsers() {
        List<UserDTO> recentUsers = userService.getRecentUsers(); // ← Crée cette méthode dans UserService
        return ResponseEntity.ok(recentUsers);
    }

    @GetMapping("/ids-by-role")
    public ResponseEntity<List<Integer>> getUserIdsByRole(@RequestParam("role") UserRole role) {
        List<Integer> ids = userRepository.findByRole(role)
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ids);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("employees", userRepository.countByRole(UserRole.EMPLOYE));
        stats.put("partners", userRepository.countByRole(UserRole.PARTENAIRE));
        stats.put("acceptedPartners", userRepository.countByRoleAndEnabled(UserRole.PARTENAIRE, true));
        stats.put("nonAcceptedPartners", userRepository.countByRoleAndEnabled(UserRole.PARTENAIRE, false));
        return ResponseEntity.ok(stats);
    }



    @PostMapping("/create")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        logger.debug("Received request to create user: {}", userDTO);

        try {
            UserDTO createdUser = userService.createUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            logger.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }


    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/find/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("userId") Integer userId) {
        Optional<UserDTO> user = userService.findById(userId);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<UserDTO>> filterUsers(@RequestParam("query") String query) {
        List<UserDTO> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }




    @PutMapping("/update/{userId}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable("userId") Integer userId, @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(userId, userDTO);
        return ResponseEntity.ok(updatedUser);
    }


    @PostMapping("/{userId}/profile-photo")
    public ResponseEntity<Map<String, String>> uploadProfilePhoto(
            @PathVariable Integer userId,
            @RequestParam("profilePhoto") MultipartFile profilePhoto) {

        if (profilePhoto.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded"));
        }

        try {
            String filename = userId + "_" + profilePhoto.getOriginalFilename();
            Path destinationFile = rootLocation.resolve(filename).normalize().toAbsolutePath();

            try (InputStream inputStream = profilePhoto.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            String photoPath = "http://localhost:8070/profile-photos/" + filename;

            // 🔥 ici on décide où mettre l’URL : logo ou profilePicture
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

            if (user.getRole() == UserRole.PARTENAIRE) {
                user.setLogo(photoPath);
            } else {
                user.setProfilePicture(photoPath);
            }
            userRepository.save(user);

            Map<String, String> response = new HashMap<>();
            response.put("url", photoPath);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "File upload failed"));
        }
    }

    @PostMapping("/restore-user")
    public ResponseEntity<String> restoreUser(@RequestParam String email) {
        Optional<User> userOptional = userRepository.findByEmail(email).stream().findFirst();

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with email: " + email);
        }

        User user = userOptional.get();
        if (!Boolean.TRUE.equals(user.getArchived())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User is not archived.");
        }

        user.setArchived(false);
        userRepository.save(user);

        return ResponseEntity.ok("User restored successfully.");
    }



    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") Integer userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateRequest request) {
        try {
            userService.updatePassword(request.getUserId(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok().body(Map.of("message", "Password updated successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Invalid old password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Server error"));
        }
    }



    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserDTO> getUserProfile(@PathVariable("userId") Integer userId) {
        UserDTO userProfile = userService.consulterProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    @GetMapping("/online")
    public ResponseEntity<List<User>> getOnlineUsers() {
        return ResponseEntity.ok(userService.getOnlineUsers());
    }

    @GetMapping("/enabled/{enabled}")
    public ResponseEntity<List<UserDTO>> getUsersByEnabled(@PathVariable boolean enabled) {
        List<UserDTO> users = userService.getUsersByEnabled(enabled);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/partners/totalAccepted")
    public ResponseEntity<Long> getTotalAcceptedPartners() {
        long count = userRepository.countByRoleAndEnabled(UserRole.PARTENAIRE, true);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/partners/totalNonAccepted")
    public ResponseEntity<Long> getTotalNonAcceptedPartners() {
        long count = userRepository.countByRoleAndEnabled(UserRole.PARTENAIRE, false);
        return ResponseEntity.ok(count);
    }
    @GetMapping("/partners")
    public ResponseEntity<Page<UserDTO>> getPartners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false, defaultValue = "false") boolean archived) {
        Page<UserDTO> partners = userService.getAllPartners(page, size, archived);
        return ResponseEntity.ok(partners);
    }
    // Retourne la liste de tous les IDs des users
    @GetMapping("/all-ids")
    public ResponseEntity<List<Integer>> getAllUserIds() {
        List<Integer> ids = userService.getAllUserIds();
        return ResponseEntity.ok(ids);
    }

    @GetMapping("/currentUser")
    public ResponseEntity<UserDTO> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            UserDTO user = userService.getCurrentUser(authHeader.substring(7));
            return ResponseEntity.ok(user);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }





    @GetMapping("/downloadFile/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("profile-photos").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // Détection du content-type (optionnel, mais recommandé)
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }










    @PostMapping("/archive-user")
    public ResponseEntity<String> archiveUser(@RequestParam String email) {
        Optional<User> userOptional = userRepository.findByEmail(email).stream().findFirst();

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with email: " + email);
        }

        User user = userOptional.get();
        if (Boolean.TRUE.equals(user.getArchived())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User is already archived.");
        }

        user.setArchived(true);
        userRepository.save(user);

        return ResponseEntity.ok("User archived successfully.");
    }


}