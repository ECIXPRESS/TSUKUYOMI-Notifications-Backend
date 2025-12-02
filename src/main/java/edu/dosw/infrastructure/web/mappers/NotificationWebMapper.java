package edu.dosw.infrastructure.web.mappers;

import edu.dosw.domain.model.Notification;
import edu.dosw.application.dto.query.NotificationResponse;
import edu.dosw.domain.model.ValueObject.DeliveryAttempt;
import edu.dosw.application.dto.query.NotificationResponse.DeliveryAttemptResponse;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class NotificationWebMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

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
                .channels(notification.getChannels().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList()))
                .deliveryAttempts(notification.getDeliveryAttempts().stream()
                        .map(this::toDeliveryAttemptResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private DeliveryAttemptResponse toDeliveryAttemptResponse(DeliveryAttempt attempt) {
        return DeliveryAttemptResponse.builder()
                .channel(attempt.getChannel().name())
                .successful(attempt.isSuccessful())
                .error(attempt.getError())
                .timestamp(attempt.getTimestamp())
                .status(attempt.isSuccessful() ? "SUCCESS" : "FAILED")
                .build();
    }
}