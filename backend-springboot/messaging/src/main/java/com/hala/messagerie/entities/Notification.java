package com.hala.messagerie.entities;

import com.hala.messagerie.enumm.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer userId; // destinataire
    private Integer senderId;
    private String postId;
    private String senderName;

    @Enumerated(EnumType.STRING)
    private NotificationType type;
    private String message;
    private LocalDateTime createdAt;
    private boolean read;
}
