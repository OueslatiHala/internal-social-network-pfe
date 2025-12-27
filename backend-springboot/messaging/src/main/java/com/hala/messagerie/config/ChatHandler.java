package com.hala.messagerie.config;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

public class ChatHandler extends TextWebSocketHandler {

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // Logique pour traiter les messages reçus et envoyer des réponses
        String payload = message.getPayload();
        // Vous pouvez traiter le message et le diffuser aux autres clients ici
        session.sendMessage(new TextMessage("Message reçu : " + payload));
    }
}