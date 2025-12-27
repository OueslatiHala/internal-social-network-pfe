package com.hala.messagerie.controller;

import com.hala.messagerie.dto.PrivateMessageDTO;
import com.hala.messagerie.dto.UserConversationDTO;
import com.hala.messagerie.service.PrivateMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/private-messages")
public class PrivateMessageController {

    private final PrivateMessageService privateMessageService;

    @PostMapping("/send")
    public ResponseEntity<PrivateMessageDTO> sendPrivateMessage(@RequestParam Integer userId, @RequestBody PrivateMessageDTO privateMessageDTO) {
        PrivateMessageDTO sentMessage = privateMessageService.sendMessage(userId, privateMessageDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(sentMessage);
    }
    @GetMapping("/user/{userId}/unread-counts-map")
    public ResponseEntity<Map<Integer, Long>> getUnreadCountsMap(@PathVariable Integer userId) {
        Map<Integer, Long> unreadMap = privateMessageService.getUnreadCountsMap(userId);
        return ResponseEntity.ok(unreadMap);
    }
    @PostMapping("/user/{userId}/mark-all-as-read")
    public ResponseEntity<Void> markAllMessagesAsRead(@PathVariable Integer userId) {
        privateMessageService.markAllMessagesAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @Autowired
    public PrivateMessageController(PrivateMessageService privateMessageService) {
        this.privateMessageService = privateMessageService;
    }
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadMessageCount(@PathVariable Integer userId) {
        Long count = privateMessageService.countUnreadMessages(userId);
        return ResponseEntity.ok(count);
    }
    @GetMapping("/user/{userId}/conversations")
    public ResponseEntity<List<UserConversationDTO>> getConversations(@PathVariable Integer userId) {
        List<UserConversationDTO> conversations = privateMessageService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }
    @GetMapping("/receive/{messageId}")
    public ResponseEntity<PrivateMessageDTO> receivePrivateMessage(@PathVariable Integer messageId) {
        PrivateMessageDTO receivedMessage = privateMessageService.receiveMessage(messageId);
        return ResponseEntity.ok(receivedMessage);
    }
    @PostMapping("/mark-as-read")
    public ResponseEntity<Void> markAsRead(@RequestParam Integer userId, @RequestParam Integer otherUserId) {
        privateMessageService.markConversationAsRead(userId, otherUserId);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/delete/{messageId}")
    public ResponseEntity<Void> deletePrivateMessage(@PathVariable Integer messageId) {
        privateMessageService.deleteMessage(messageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PrivateMessageDTO>> getMessagesByUserId(@PathVariable Integer userId) {
        List<PrivateMessageDTO> messages = privateMessageService.findAllMessagesByUser(userId);
        return ResponseEntity.ok(messages);
    }
    @PostMapping("/conversation/delete")
    public ResponseEntity<Void> deleteConversation(
            @RequestParam Integer userId,
            @RequestParam Integer otherUserId) {
        privateMessageService.deleteConversation(userId, otherUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<PrivateMessageDTO> getMessageById(@PathVariable Integer messageId) {
        PrivateMessageDTO message = privateMessageService.findMessageById(messageId);
        return ResponseEntity.ok(message);
    }
}
