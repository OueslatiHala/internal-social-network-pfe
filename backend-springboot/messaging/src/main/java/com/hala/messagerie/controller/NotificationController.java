package com.hala.messagerie.controller;

import com.hala.messagerie.dto.NotificationDTO;
import com.hala.messagerie.entities.Notification;
import com.hala.messagerie.enumm.NotificationType;
import com.hala.messagerie.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import org.springframework.kafka.core.KafkaTemplate;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private KafkaTemplate<String, Notification> kafkaTemplate;
    public NotificationController(KafkaTemplate<String, Notification> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@PathVariable Integer userId) {
        List<Notification> notifications = notificationService.getNotificationsForUser(userId);
        List<NotificationDTO> dtos = notifications.stream().map(n ->
                new NotificationDTO(
                        n.getId(),             // ✅ ici
                        n.getUserId(),
                        n.getSenderId(),
                        n.getPostId(),
                        n.getType(),
                        n.getMessage(),
                        n.getCreatedAt(),
                        n.isRead(),
                        n.getSenderName()
                )

        ).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotificationById(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }


    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/user/{userId}/mark-as-read")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Integer userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/test-notif/{userId}")
    public ResponseEntity<Void> testNotif(@PathVariable Integer userId) {
        Notification notif = new Notification();
        notif.setUserId(userId);
        notif.setType(NotificationType.POST);
        notif.setMessage("Test POST notification");
        notif.setPostId("999"); // string, sans 'L'
        notif.setCreatedAt(LocalDateTime.now());
        notif.setRead(false);

        kafkaTemplate.send("notifications", notif);
        return ResponseEntity.ok().build();
    }

}
