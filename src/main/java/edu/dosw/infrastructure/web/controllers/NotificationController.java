package edu.dosw.infrastructure.web.controllers;

import edu.dosw.application.ports.NotificationQueryPort;
import edu.dosw.application.dto.query.NotificationResponse;
import edu.dosw.infrastructure.web.mappers.NotificationWebMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryPort notificationQueryPort;
    private final NotificationWebMapper notificationWebMapper;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @PathVariable String userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {
        try {
            List<NotificationResponse> notifications = notificationQueryPort.getByUserId(userId);

            if (status != null && !status.isEmpty()) {
                notifications = notifications.stream()
                        .filter(notification -> notification.getStatus().equalsIgnoreCase(status))
                        .toList();
            }

            if (type != null && !type.isEmpty()) {
                notifications = notifications.stream()
                        .filter(notification -> notification.getType().equalsIgnoreCase(type))
                        .toList();
            }

            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadUserNotifications(
            @PathVariable String userId) {
        try {
            List<NotificationResponse> notifications = notificationQueryPort.getUnreadByUserId(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotification(@PathVariable String id) {
        try {
            return notificationQueryPort.getById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String id) {
        try {
            notificationQueryPort.markAsRead(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        try {
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<Object> getUserNotificationStats(@PathVariable String userId) {
        try {
            List<NotificationResponse> notifications = notificationQueryPort.getByUserId(userId);

            long total = notifications.size();
            long unread = notifications.stream()
                    .filter(n -> "SENT".equals(n.getStatus()) || "PENDING".equals(n.getStatus()))
                    .count();
            long read = notifications.stream()
                    .filter(n -> "READ".equals(n.getStatus()))
                    .count();

            Map<String, Object> stats = Map.of(
                    "totalNotifications", total,
                    "unreadNotifications", unread,
                    "readNotifications", read,
                    "userId", userId
            );

            return ResponseEntity.ok().body(stats);

        } catch (Exception e) {
            log.error("Error getting stats for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}