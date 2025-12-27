package com.hala.messagerie.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.WebSocketSession;

import java.security.Principal;

@Component
public class StompEventListener {

    @Autowired
    private WebSocketSessionManager sessionManager;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userIdStr = accessor.getFirstNativeHeader("userId");

        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                sessionManager.addConnectedUser(userId); // 🔥 AJOUT OBLIGATOIRE
                System.out.println("📡 User connected with ID: " + userId);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid userId in STOMP headers");
            }
        }

    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userIdStr = accessor.getFirstNativeHeader("userId");

        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                sessionManager.removeConnectedUser(userId); // 👋 Nettoyage
                System.out.println("❌ User disconnected: " + userId);
            } catch (NumberFormatException ignored) {}
        }
    }


}
