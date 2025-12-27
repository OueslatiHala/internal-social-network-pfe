package com.hala.post.dto;

import com.hala.post.enumm.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class NotificationDTO {
    @NotNull
    private Integer userId;        // Destinataire

    private Integer senderId;      // Expéditeur

    private String postId;         // ID du post concerné

    @NotNull
    private NotificationType type; // LIKE, COMMENT, etc.

    @NotNull
    private String message;

    private LocalDateTime createdAt;

    private boolean read;

    private String senderName;


    public NotificationDTO(Integer userId, Integer senderId, String postId, NotificationType type,
                           String message, LocalDateTime createdAt, boolean read, String senderName) {
        this.userId = userId;
        this.senderId = senderId;
        this.postId = postId;
        this.type = type;
        this.message = message;
        this.createdAt = createdAt;
        this.read = read;
        this.senderName = senderName;
    }

}
