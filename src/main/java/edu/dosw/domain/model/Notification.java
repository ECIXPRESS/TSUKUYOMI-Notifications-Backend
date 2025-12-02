
package edu.dosw.domain.model;

import edu.dosw.domain.model.ValueObject.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class Notification {
    private NotificationId id;
    private String userId;
    private String userEmail;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationStatus status;
    private List<Channel> channels;
    private List<DeliveryAttempt> deliveryAttempts;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private String metadata;

    public void addDeliveryAttempt(Channel channel, boolean successful, String error) {
        this.deliveryAttempts.add(new DeliveryAttempt(channel, successful, error, LocalDateTime.now()));
        if (successful) {
            this.status = NotificationStatus.SENT;
        } else {
            this.status = NotificationStatus.FAILED;
        }
    }

    public void markAsRead() {
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
    }
}