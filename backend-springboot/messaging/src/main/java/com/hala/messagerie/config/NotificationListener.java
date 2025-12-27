package com.hala.messagerie.config;
import org.springframework.messaging.simp.SimpMessagingTemplate;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hala.messagerie.dto.NotificationDTO;
import com.hala.messagerie.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;

@Service
public class NotificationListener {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private WebSocketSessionManager sessionManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(
            topics = "notifications-topic",
            groupId = "notification-group",
            containerFactory = "notificationKafkaListenerContainerFactory"
    )
    public void listen(NotificationDTO notificationDTO) {
        // 1. Sauvegarder la notification
        notificationService.createNotification(
                notificationDTO.getUserId(),
                notificationDTO.getSenderId(),
                notificationDTO.getPostId(),
                notificationDTO.getType(),
                notificationDTO.getMessage(),
                notificationDTO.getSenderName()
        );

        // 2. Notif en temps réel via STOMP
        Long userId = notificationDTO.getUserId().longValue();

        if (sessionManager.isUserConnected(userId)) {
            System.out.println("📤 Sending STOMP notification to /topic/notifications/" + userId);
            simpMessagingTemplate.convertAndSend("/topic/notifications/" + userId, notificationDTO);
        }

    }
}