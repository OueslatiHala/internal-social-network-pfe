package com.hala.messagerie.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {
    private final Map<Long, List<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void addSession(Long userId, WebSocketSession session) {
        userSessions.computeIfAbsent(userId, k -> new ArrayList<>()).add(session);
    }
    public void removeSession(Long userId, WebSocketSession session) {
        List<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }

    }
    private final Set<Long> connectedUserIds = ConcurrentHashMap.newKeySet();

    public void addConnectedUser(Long userId) {
        connectedUserIds.add(userId);
    }

    public void removeConnectedUser(Long userId) {
        connectedUserIds.remove(userId);
    }

    public boolean isUserConnected(Long userId) {
        return connectedUserIds.contains(userId);
    }

    public List<WebSocketSession> getSessions(Long userId) {
        return userSessions.get(userId);
    }
}
