package edu.dosw.application.ports;

import edu.dosw.application.dto.query.NotificationResponse;
import java.util.List;
import java.util.Optional;

public interface NotificationQueryPort {
    List<NotificationResponse> getByUserId(String userId);
    Optional<NotificationResponse> getById(String id);
    void markAsRead(String id);
    List<NotificationResponse> getUnreadByUserId(String userId); // Nuevo método
}