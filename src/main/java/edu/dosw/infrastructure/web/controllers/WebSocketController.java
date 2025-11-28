package edu.dosw.infrastructure.web.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/notifications.subscribe")
    @SendTo("/topic/notifications.status")
    public Map<String, Object> handleSubscription(Map<String, Object> message) {
        String userId = (String) message.get("userId");
        log.info("User {} subscribed to notifications", userId);

        return Map.of(
                "type", "SUBSCRIPTION_CONFIRMED",
                "userId", userId,
                "timestamp", LocalDateTime.now(),
                "message", "Successfully subscribed to notifications"
        );
    }

    @MessageMapping("/notifications.markRead")
    public void handleMarkAsRead(Map<String, Object> message) {
        String notificationId = (String) message.get("notificationId");
        String userId = (String) message.get("userId");

        log.info("User {} marked notification {} as read via WebSocket", userId, notificationId);

        // Aquí podrías integrar con el servicio para marcar como leída
        messagingTemplate.convertAndSend("/topic/notifications/" + userId,
                Map.of(
                        "type", "NOTIFICATION_READ",
                        "notificationId", notificationId,
                        "userId", userId,
                        "timestamp", LocalDateTime.now()
                )
        );
    }

    @MessageMapping("/notifications.test")
    @SendTo("/topic/notifications.test")
    public Map<String, Object> handleTestMessage(Map<String, Object> message) {
        log.info("Received test WebSocket message: {}", message);

        return Map.of(
                "type", "TEST_RESPONSE",
                "originalMessage", message,
                "timestamp", LocalDateTime.now(),
                "serverTime", System.currentTimeMillis()
        );
    }
}