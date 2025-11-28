package edu.dosw.infrastructure.persistence.mappers;

import edu.dosw.domain.model.Notification;
import edu.dosw.domain.model.ValueObject.*;
import edu.dosw.infrastructure.persistence.documents.NotificationDocument;
import edu.dosw.infrastructure.persistence.documents.DeliveryAttemptDocument;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class NotificationMongoMapper {

    public NotificationDocument toDocument(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationDocument document = new NotificationDocument();
        document.setId(notification.getId().getValue());
        document.setUserId(notification.getUserId());
        document.setUserEmail(notification.getUserEmail());  // ← MAPEAR EMAIL
        document.setTitle(notification.getTitle());
        document.setMessage(notification.getMessage());
        document.setType(notification.getType().name());
        document.setStatus(notification.getStatus().name());
        document.setChannels(notification.getChannels().stream()
                .map(Enum::name)
                .collect(Collectors.toList()));
        document.setDeliveryAttempts(notification.getDeliveryAttempts().stream()
                .map(this::toDeliveryAttemptDocument)
                .collect(Collectors.toList()));
        document.setCreatedAt(notification.getCreatedAt());
        document.setReadAt(notification.getReadAt());
        document.setMetadata(notification.getMetadata());
        return document;
    }

    public Notification toDomain(NotificationDocument document) {
        if (document == null) {
            return null;
        }

        return Notification.builder()
                .id(new NotificationId(document.getId()))
                .userId(document.getUserId())
                .userEmail(document.getUserEmail())
                .title(document.getTitle())
                .message(document.getMessage())
                .type(NotificationType.valueOf(document.getType()))
                .status(NotificationStatus.valueOf(document.getStatus()))
                .channels(document.getChannels().stream()
                        .map(Channel::valueOf)
                        .collect(Collectors.toList()))
                .deliveryAttempts(document.getDeliveryAttempts().stream()
                        .map(this::toDeliveryAttemptDomain)
                        .collect(Collectors.toList()))
                .createdAt(document.getCreatedAt())
                .readAt(document.getReadAt())
                .metadata(document.getMetadata())
                .build();
    }

    private DeliveryAttemptDocument toDeliveryAttemptDocument(DeliveryAttempt attempt) {
        return new DeliveryAttemptDocument(
                attempt.getChannel().name(),
                attempt.isSuccessful(),
                attempt.getError(),
                attempt.getTimestamp()
        );
    }

    private DeliveryAttempt toDeliveryAttemptDomain(DeliveryAttemptDocument document) {
        return new DeliveryAttempt(
                Channel.valueOf(document.getChannel()),
                document.isSuccessful(),
                document.getError(),
                document.getTimestamp()
        );
    }
}