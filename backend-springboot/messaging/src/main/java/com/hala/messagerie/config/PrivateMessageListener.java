package com.hala.messagerie.config;

import com.hala.messagerie.dto.PrivateMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
public class PrivateMessageListener {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(
            topics = "private-messages-topic",
            groupId = "private-message-group",
            containerFactory = "privateMessageKafkaListenerContainerFactory"
    )
    public void onPrivateMessageReceived(PrivateMessageDTO messageDTO) {
        // ✅ Envoyer au destinataire
        messagingTemplate.convertAndSend("/topic/messages/" + messageDTO.getRecipientId(), messageDTO);

        // ✅ Envoyer aussi au sender (lui-même)
        messagingTemplate.convertAndSend("/topic/messages/" + messageDTO.getSenderId(), messageDTO);
    }
}



