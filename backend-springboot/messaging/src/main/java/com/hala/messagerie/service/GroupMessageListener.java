package com.hala.messagerie.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class GroupMessageListener {

    @KafkaListener(topics = "group_messages", groupId = "group-messages-group")
    public void listenGroupMessages(String message) {
        // Logique pour traiter le message reçu
        System.out.println("Received message: " + message);
    }
}