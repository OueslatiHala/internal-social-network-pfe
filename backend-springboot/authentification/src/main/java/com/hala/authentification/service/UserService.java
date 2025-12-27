package com.hala.authentification.service;

import com.hala.authentification.config.JwtService;
import com.hala.authentification.dto.UserDTO;
import com.hala.authentification.email.EmailDetails;
import com.hala.authentification.email.EmailService;
import com.hala.authentification.email.RandomPassword;
import com.hala.authentification.entities.User;
import com.hala.authentification.enumm.UserRole;
import com.hala.authentification.mappers.UserMapper;
import com.hala.authentification.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NonUniqueResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final EmailService emailService;
    private final RandomPassword randomPassword;
    private final PasswordEncoder passwordEncoder;
    private final Path rootLocation = Paths.get("profile-photos");
    private final Path profilePhotoStorageLocation = Paths.get("profile_photos").toAbsolutePath().normalize();

    public UserService(UserRepository userRepository, JwtService jwtService, UserMapper userMapper, EmailService emailService, RandomPassword randomPassword, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.emailService = emailService;
        this.randomPassword = randomPassword;
        this.passwordEncoder = passwordEncoder;
        createDirectory();
    }
    public List<Integer> getUserIdsByRole(UserRole role) {
        return userRepository.findIdsByRole(role);
    }

    private void createDirectory() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public List<UserDTO> searchUsers(String query) {
        List<User> users = userRepository.searchUsers(query);
        return users.stream()
                .map(userMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDTO> findUsersByFirstNameOrLastName(String query) {
        List<User> users = userRepository.findByFirstnameStartsWithIgnoreCaseOrLastnameStartsWithIgnoreCase(query, query);
        return users.stream()
                .map(userMapper::userToUserDto)
                .collect(Collectors.toList());
    }
    public List<UserDTO> getRecentUsers() {
        List<User> recentUsers = userRepository.findTop5ByOrderByCreatedAtDesc();
        return recentUsers.stream()
                .map(userMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return userMapper.userToUserDto(user);
    }

    public Optional<UserDTO> findById(Integer id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(userMapper::userToUserDto);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::userToUserDto)
                .collect(Collectors.toList());
    }


    public Optional<UserDTO> findByEmail(String email) {
        List<User> users = userRepository.findByEmail(email);
        if (users.size() != 1) {
            throw new NonUniqueResultException("Query did not return a unique result: " + users.size() + " results were returned");
        }
        return Optional.of(userMapper.userToUserDto(users.get(0)));
    }

    public UserDTO createUser(UserDTO userDTO) {
        logger.debug("Creating user with DTO: {}", userDTO);

        List<User> users = userRepository.findByEmail(userDTO.getEmail());
        if (!users.isEmpty()) {
            logger.error("Email already exists: {}", userDTO.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        User user = userMapper.userDtoToUser(userDTO);
        user.setCreatedAt(LocalDateTime.now());

        logger.debug("Mapped User entity: {}", user);

        if (user.getRole() == null) {
            user.setRole(UserRole.EMPLOYE);
        }
        user.setArchived(false); // ✅ Empêche archived d'être null


        String generatedPassword = randomPassword.generateRandomPassword(10);
        user.setPassword(passwordEncoder.encode(generatedPassword));
        logger.debug("User entity before save: {}", user);

        User savedUser = userRepository.save(user);
        logger.debug("User entity after save: {}", savedUser);

        // Send email with the generated password
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setRecipient(savedUser.getEmail());
        emailDetails.setSubject("Welcome to Our Platform");
        emailDetails.setMsgBody("Welcome " +" user.username Your account has been created. Your password is: " + generatedPassword);
        emailService.sendSimpleMail(emailDetails);
        EmailDetails adminNotification = new EmailDetails();
        adminNotification.setRecipient("oueslati.hala4@gmail.com"); // ✅ Mets ici les vrais emails séparés par des virgules
        adminNotification.setSubject("New Employee Registered");
        adminNotification.setMsgBody("A new partner has registered:\n" +
                "Name: " + savedUser.getFirstname() + " " + savedUser.getLastname() + "\n" +
                "Email: " + savedUser.getEmail() + "\n" +
                "Company: " + savedUser.getCompanyName());
        emailService.sendSimpleMail(adminNotification);


        return userMapper.userToUserDto(savedUser);
    }

    public void updatePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        System.out.println("🧪 Ancien mot de passe reçu = " + oldPassword);
        System.out.println("🧪 Password encodé en DB = " + user.getPassword());

        boolean isEmpty = user.getPassword() == null || user.getPassword().isBlank();
        boolean matches = oldPassword != null && passwordEncoder.matches(oldPassword, user.getPassword());

        if (!isEmpty && !matches) {
            throw new IllegalArgumentException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }



    public Page<UserDTO> getAllPartners(int page, int size, boolean archived) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.findByRoleAndArchived(UserRole.PARTENAIRE, archived, pageable);
        return userPage.map(userMapper::userToUserDto);
    }







    public void archiveUser(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email).stream().findFirst();
        if (userOptional.isEmpty()) {
            throw new EntityNotFoundException("User not found with email: " + email);
        }

        User user = userOptional.get();
        if (Boolean.TRUE.equals(user.getArchived())) {
            throw new IllegalStateException("User is already archived.");
        }

        user.setArchived(true);
        userRepository.save(user);
    }


    public UserDTO updateUser(Integer userId, UserDTO userDTO) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        existingUser.setFirstname(userDTO.getFirstname());
        existingUser.setLastname(userDTO.getLastname());
        existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        existingUser.setRole(userDTO.getRole());
        existingUser.setProfilePicture(userDTO.getProfilePicture());
        existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        existingUser.setCompanyName(userDTO.getCompanyName());

        User updatedUser = userRepository.save(existingUser);
        return userMapper.userToUserDto(updatedUser);
    }

    public void updateProfilePhoto(Integer userId, String profilePhotoPath) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setProfilePicture(profilePhotoPath);
        userRepository.save(user);
    }

    public List<Integer> getAllUserIds() {
        return userRepository.findAll().stream()
                .map(User::getId)
                .collect(Collectors.toList());
    }



    public String saveProfilePhoto(Integer userId, MultipartFile file) {
        String fileName = userId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        try {
            Path targetLocation = profilePhotoStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            String profilePhotoUrl = "/profile_photos/" + fileName;

            // Mise à jour de l'utilisateur avec le nouveau URL de la photo de profil
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
            user.setProfilePicture(profilePhotoUrl);
            userRepository.save(user);

            return profilePhotoUrl;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store profile photo " + file.getOriginalFilename() + ". Please try again!", ex);
        }
    }
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        userRepository.delete(user); // Suppression réelle de l'utilisateur
    }


    public UserDTO consulterProfile(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            return userMapper.userToUserDto(userOptional.get());
        } else {
            throw new EntityNotFoundException("User not found with ID: " + userId);
        }
    }

    public List<UserDTO> getUsersByEnabled(boolean enabled) {
        List<User> users = userRepository.findByEnabled(enabled);
        return users.stream()
                .map(userMapper::userToUserDto)
                .collect(Collectors.toList());
    }

    public UserDTO getCurrentUser(String token) {
        String email = jwtService.extractUsername(token);
        List<User> users = userRepository.findByEmail(email);
        if (users.size() != 1) {
            throw new UsernameNotFoundException("Utilisateur non trouvé ou résultats multiples trouvés");
        }
        User user = users.get(0);
        return userMapper.userToUserDto(user);
    }


    public List<User> getOnlineUsers() {
        return userRepository.findOnlineUsers();
    }

    public List<UserDTO> getOnlineUsersExcludingCurrentUser(Integer currentUserId) {
        List<User> onlineUsers = userRepository.findOnlineUsers();

        return onlineUsers.stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .map(userMapper::userToUserDto)
                .collect(Collectors.toList());
    }
}
