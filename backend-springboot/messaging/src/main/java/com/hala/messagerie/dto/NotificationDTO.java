package com.hala.messagerie.dto;

import com.hala.messagerie.enumm.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    @NotNull
    private Integer userId;
    private Integer senderId;
    private String postId;
    private String senderName;

    @NotNull
    private NotificationType type;
    @NotNull
    private String message;
    private LocalDateTime createdAt;
    private boolean read;

    public NotificationDTO(Long id, Integer userId, Integer senderId, String postIdOrMessageId,
                           NotificationType type, String message,
                           LocalDateTime createdAt, boolean read, String senderName) {
        this.id = id;
        this.userId = userId;
        this.senderId = senderId;
        this.postId = postIdOrMessageId;
        this.type = type;
        this.message = message;
        this.createdAt = createdAt;
        this.read = read;
        this.senderName = senderName;
    }



}
