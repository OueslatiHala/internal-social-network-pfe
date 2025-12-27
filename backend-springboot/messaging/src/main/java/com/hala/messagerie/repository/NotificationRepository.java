package com.hala.messagerie.repository;

import com.hala.messagerie.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<Notification> findByUserIdAndReadFalse(Integer userId);
    void deleteById(Long id);
}
