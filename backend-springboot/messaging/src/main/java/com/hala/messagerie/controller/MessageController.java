package com.hala.messagerie.controller;

import com.hala.messagerie.dto.MessageDTO;
import com.hala.messagerie.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping
    public ResponseEntity<Void> envoyerMessage(@RequestBody MessageDTO messageDTO) {
        messageService.envoyer_msg(messageDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<MessageDTO>> recevoirMessages(@PathVariable("userId") Integer userId) {
        List<MessageDTO> messages = messageService.recevoir_msg(userId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> supprimerMessage(@PathVariable("messageId") Integer messageId) {
        messageService.supprimer_msg(messageId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/allMessages")
    public ResponseEntity<List<MessageDTO>> consulterMessages() {
        List<MessageDTO> messages = messageService.consulter_msg();
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
}