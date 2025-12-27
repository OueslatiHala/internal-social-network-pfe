package com.hala.messagerie.service;

import com.hala.messagerie.Iservice.IServicePrivateMessage;
import com.hala.messagerie.client.AuthClient;
import com.hala.messagerie.client.UserDTO;
import com.hala.messagerie.dto.NotificationDTO;
import com.hala.messagerie.dto.PrivateMessageDTO;
import com.hala.messagerie.dto.UserConversationDTO;
import com.hala.messagerie.entities.PrivateMessage;
import com.hala.messagerie.enumm.NotificationType;
import com.hala.messagerie.mappers.PrivateMessageMapper;
import com.hala.messagerie.repository.PrivateMessageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PrivateMessageService implements IServicePrivateMessage {
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    @Qualifier("deleteMessageKafkaTemplate")
    private KafkaTemplate<String, Long> deleteMessageKafkaTemplate;

    private final PrivateMessageRepository privateMessageRepository;
    private final PrivateMessageMapper privateMessageMapper;
    private final AuthClient authClient;
    private final KafkaTemplate<String, NotificationDTO> notificationKafkaTemplate;
    private final KafkaTemplate<String, PrivateMessageDTO> kafkaTemplate;
    @Autowired
    public PrivateMessageService(
            SimpMessagingTemplate messagingTemplate,
            PrivateMessageRepository privateMessageRepository,
            PrivateMessageMapper privateMessageMapper,
            AuthClient authClient, KafkaTemplate<String, NotificationDTO> notificationKafkaTemplate,
            @Qualifier("privateMessageKafkaTemplate") KafkaTemplate<String, PrivateMessageDTO> kafkaTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
        this.privateMessageRepository = privateMessageRepository;
        this.privateMessageMapper = privateMessageMapper;
        this.authClient = authClient;
        this.notificationKafkaTemplate = notificationKafkaTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }
    public Long countUnreadMessages(Integer userId) {
        return privateMessageRepository.countByRecipientIdAndConsulterFalse(userId);
    }
    @Transactional
    public void markAllMessagesAsRead(Integer userId) {
        List<PrivateMessage> messages = privateMessageRepository.findByRecipientIdAndConsulterFalse(userId);
        for (PrivateMessage m : messages) {
            m.setConsulter(true);
        }
        privateMessageRepository.saveAll(messages);
    }

    @Transactional  // ✅ OBLIGATOIRE pour update/delete
    public void deleteConversation(Integer userId, Integer otherUserId) {
        privateMessageRepository.deleteConversationBetweenUsers(userId, otherUserId);
    }
    public Map<Integer, Long> getUnreadCountsMap(Integer userId) {
        // Pour chaque user ayant une conversation avec userId,
        // compte le nombre de messages non lus de LUI vers userId
        List<Integer> otherUserIds = privateMessageRepository.findConversationUserIds(userId);
        Map<Integer, Long> map = new HashMap<>();
        for (Integer otherId : otherUserIds) {
            Long count = privateMessageRepository.countBySenderIdAndRecipientIdAndConsulterFalse(otherId, userId);
            map.put(otherId, count);
        }
        return map;
    }

    public List<UserConversationDTO> getUserConversations(Integer userId) {
        List<Integer> otherUserIds = privateMessageRepository.findConversationUserIds(userId);
        List<UserConversationDTO> conversations = new ArrayList<>();

        for (Integer otherId : otherUserIds) {
            List<PrivateMessage> messages = privateMessageRepository.findConversation(userId, otherId);

            // Supprimer cette ligne qui ne sert à rien ici :
            // UserDTO sender = authClient.getUserById(userId);

            List<PrivateMessageDTO> messageDTOs = messages.stream()
                    .map(msg -> {
                        UserDTO actualSender = authClient.getUserById(msg.getSenderId());
                        return PrivateMessageDTO.fromEntity(msg, actualSender);
                    })
                    .collect(Collectors.toList());

            conversations.add(new UserConversationDTO(otherId, messageDTOs, false));
        }

        return conversations;
    }

    @Override
    public PrivateMessageDTO sendMessage(Integer userId, PrivateMessageDTO dto) {
        // 🔒 Empêcher les messages à soi-même
        if (userId.equals(dto.getRecipientId())) {
            throw new IllegalArgumentException("You cannot send a message to yourself.");
        }

        UserDTO sender = authClient.getUserById(userId);
        if (sender == null) throw new EntityNotFoundException("Sender not found: " + userId);

        UserDTO recipient = authClient.getUserById(dto.getRecipientId());
        if (recipient == null) throw new EntityNotFoundException("Recipient not found: " + dto.getRecipientId());

        PrivateMessage entity = privateMessageMapper.privateMessageDtoToPrivateMessage(dto);
        entity.setSenderId(userId);
        entity.setRecipientId(dto.getRecipientId());
        entity.setDateMsg(new Date());
        entity.setConsulter(false);

        PrivateMessage saved = privateMessageRepository.save(entity);

        PrivateMessageDTO enrichedDTO = PrivateMessageDTO.fromEntity(saved, sender);

        // ✅ Envoi Kafka/WebSocket
        kafkaTemplate.send("private-messages-topic", enrichedDTO);

        // ✅ Notification WebSocket au destinataire
        messagingTemplate.convertAndSend("/topic/messages/" + dto.getRecipientId(), enrichedDTO);

        return enrichedDTO;
    }



    @Override
    public PrivateMessageDTO receiveMessage(Integer messageId) {
        PrivateMessage privateMessage = privateMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Private message not found"));
        privateMessage.setConsulter(true);
        PrivateMessage updated = privateMessageRepository.save(privateMessage);
        return privateMessageMapper.privateMessageToPrivateMessageDto(updated);
    }

    @Transactional
    public void markConversationAsRead(Integer userId, Integer otherUserId) {
        privateMessageRepository.markConversationAsRead(userId, otherUserId);
    }


    @Transactional
    public void deleteMessage(Integer id) {
        PrivateMessage message = privateMessageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        privateMessageRepository.deleteById(id);

        // Envoie l'ID du message supprimé aux deux utilisateurs
        messagingTemplate.convertAndSend("/topic/delete-message/" + message.getSenderId(), id);
        messagingTemplate.convertAndSend("/topic/delete-message/" + message.getRecipientId(), id);
    }


    @Override
    public List<PrivateMessageDTO> findAllMessagesByUser(Integer userId) {
        List<PrivateMessage> messages = privateMessageRepository.findAllMessagesForUser(userId); // méthode à créer

        return messages.stream()
                .map(msg -> {
                    UserDTO sender = authClient.getUserById(msg.getSenderId());
                    return PrivateMessageDTO.fromEntity(msg, sender); // enrichi
                })
                .collect(Collectors.toList());
    }


    @Override
    public PrivateMessageDTO findMessageById(Integer messageId) {
        PrivateMessage privateMessage = privateMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Private message not found with id: " + messageId));
        return privateMessageMapper.privateMessageToPrivateMessageDto(privateMessage);
    }
}
