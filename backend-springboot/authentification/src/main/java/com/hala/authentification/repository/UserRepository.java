package com.hala.authentification.repository;

import com.hala.authentification.entities.User;
import com.hala.authentification.enumm.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // ✅ bon import
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findTop5ByOrderByCreatedAtDesc();
    @Query("SELECT u.id FROM User u WHERE u.role = :role")
    List<Integer> findIdsByRole(@Param("role") UserRole role);

    Optional<User> findFirstByRole(UserRole role);

    List<User> findByEmail(String email);

    List<User> findAllByEmail(String email);

    Page<User> findByRoleAndArchived(UserRole role, boolean archived, Pageable pageable);

    long countByRole(UserRole role);

    List<User> findByEnabled(boolean enabled);

    List<User> findByRole(UserRole role); // pas List<Integer>

    long countByRoleAndEnabled(UserRole role, boolean enabled);

    List<User> findByFirstnameContainingIgnoreCaseOrLastnameContainingIgnoreCase(String firstname, String lastname); // ✅ SUPPRIMER @Query

    @Query("SELECT u FROM User u WHERE u.online = true")
    List<User> findOnlineUsers();

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.firstname) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.lastname) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(u.companyName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchUsers(@Param("query") String query);

    List<User> findByFirstnameStartsWithIgnoreCaseOrLastnameStartsWithIgnoreCase(String query, String query1);

    Page<User> findByRole(UserRole role, Pageable pageable);
}
