package edu.dosw.domain.ports;

import edu.dosw.domain.model.Notification;
import java.util.List;
import java.util.Optional;

public interface NotificationRepositoryPort {
    Notification save(Notification notification);
    Optional<Notification> findById(String id);
    List<Notification> findByUserId(String userId);
    List<Notification> findPendingNotifications();
    List<Notification> findByUserIdAndStatus(String userId, String status);
    boolean existsByUserIdAndType(String userId, String type);
    long countByUserIdAndStatus(String userId, String status);
}