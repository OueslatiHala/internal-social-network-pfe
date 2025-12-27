package com.hala.messagerie.service;
import lombok.extern.slf4j.Slf4j;

import com.hala.messagerie.dto.NotificationDTO;
import com.hala.messagerie.entities.Notification;
import com.hala.messagerie.enumm.NotificationType;
import com.hala.messagerie.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
@Slf4j

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Pour créer (et sauvegarder) une notification
    public Notification createNotification(Integer userId, Integer senderId, String postId, NotificationType type, String message, String senderName) {
        Notification notif = Notification.builder()
                .userId(userId)
                .senderId(senderId)
                .postId(postId)
                .type(type)
                .message(message)
                .senderName(senderName)
                .createdAt(LocalDateTime.now())
                .read(false)
                .build();
        return notificationRepository.save(notif);
    }
    @Transactional
    public void deleteNotificationById(Long id) {
        try {
            Optional<Notification> optional = notificationRepository.findById(id);
            if (optional.isPresent()) {
                notificationRepository.delete(optional.get());
            } else {
                log.warn("⚠️ Notification with id {} not found", id);
            }
        } catch (Exception e) {
            log.error("❌ Failed to delete notification with id {}: {}", id, e.getMessage());
        }
    }



    public List<Notification> getNotificationsForUser(Integer userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long notificationId) {
        Notification notif = notificationRepository.findById(notificationId).orElseThrow();
        notif.setRead(true);
        notificationRepository.save(notif);
    }
    public void markAllAsRead(Integer userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndReadFalse(userId);
        for (Notification notif : notifications) {
            notif.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }

}
