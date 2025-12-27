package com.hala.messagerie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConversationDTO {
    private Integer otherUserId;
    private List<PrivateMessageDTO> messages;
    private boolean hasUnreadMessages;
}
