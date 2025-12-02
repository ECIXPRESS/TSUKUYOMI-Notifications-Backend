package edu.dosw.infrastructure.websocket;

import edu.dosw.domain.ports.WebSocketEmitterPort;
import edu.dosw.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEmitterAdapter implements WebSocketEmitterPort {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void emitUserNotification(String userId, Notification notification) {
        try {
            String destination = "/topic/notifications/" + userId;
            messagingTemplate.convertAndSend(destination, toWebSocketMessage(notification));
            log.info("WebSocket notification sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Error sending WebSocket notification to user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void emitGlobalNotification(Notification notification) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications", toWebSocketMessage(notification));
            log.info("Global WebSocket notification sent");
        } catch (Exception e) {
            log.error("Error sending global WebSocket notification: {}", e.getMessage());
        }
    }

    @Override
    public void emitBatchNotifications(List<Notification> notifications) {
        try {
            for (Notification notification : notifications) {
                emitUserNotification(notification.getUserId(), notification);
            }
            log.info("Batch of {} WebSocket notifications sent", notifications.size());
        } catch (Exception e) {
            log.error("Error sending batch WebSocket notifications: {}", e.getMessage());
        }
    }

    @Override
    public boolean isUserConnected(String userId) {
        return false;
    }

    @Override
    public int getConnectedUsersCount() {
        return 0;
    }

    private WebSocketMessage toWebSocketMessage(Notification notification) {
        return WebSocketMessage.builder()
                .id(notification.getId().getValue())
                .type("NOTIFICATION")
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .notificationType(notification.getType().name())
                .status(notification.getStatus().name())
                .createdAt(notification.getCreatedAt())
                .metadata(notification.getMetadata())
                .timestamp(java.time.LocalDateTime.now())
                .build();
    }

    @lombok.Data
    @lombok.Builder
    private static class WebSocketMessage {
        private String id;
        private String type;
        private String userId;
        private String title;
        private String message;
        private String notificationType;
        private String status;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime timestamp;
        private String metadata;
    }
}