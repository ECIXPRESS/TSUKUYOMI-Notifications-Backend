package edu.dosw.application.services;

import edu.dosw.application.ports.NotificationQueryPort;
import edu.dosw.domain.ports.NotificationRepositoryPort;
import edu.dosw.application.dto.query.NotificationResponse;
import edu.dosw.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationQueryService implements NotificationQueryPort {

    private final NotificationRepositoryPort notificationRepositoryPort;

    @Override
    public List<NotificationResponse> getByUserId(String userId) {
        List<Notification> notifications = notificationRepositoryPort.findByUserId(userId);
        return notifications.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<NotificationResponse> getById(String id) {
        return notificationRepositoryPort.findById(id)
                .map(this::toResponse);
    }

    @Override
    public void markAsRead(String id) {
        Optional<Notification> notificationOpt = notificationRepositoryPort.findById(id);
        notificationOpt.ifPresent(notification -> {
            notification.markAsRead();
            notificationRepositoryPort.save(notification);
        });
    }

    @Override
    public List<NotificationResponse> getUnreadByUserId(String userId) {
        List<Notification> notifications = notificationRepositoryPort.findByUserId(userId);
        return notifications.stream()
                .filter(notification -> notification.getStatus() == edu.dosw.domain.model.ValueObject.NotificationStatus.SENT ||
                        notification.getStatus() == edu.dosw.domain.model.ValueObject.NotificationStatus.PENDING)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId().getValue())
                .userId(notification.getUserId())
                .userEmail(notification.getUserEmail())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .status(notification.getStatus().name())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .metadata(notification.getMetadata())
                .build();
    }
}