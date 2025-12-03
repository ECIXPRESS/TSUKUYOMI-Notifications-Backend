package edu.dosw.domain.model;

import edu.dosw.domain.model.ValueObject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .id(new NotificationId("test123"))
                .userId("user123")
                .userEmail("test@example.com")
                .title("Test Title")
                .message("Test Message")
                .type(NotificationType.ORDER_CONFIRMED)
                .status(NotificationStatus.PENDING)
                .channels(new ArrayList<>(Arrays.asList(Channel.EMAIL, Channel.WEB_SOCKET)))
                .deliveryAttempts(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .metadata("{\"key\":\"value\"}")
                .build();
    }

    @Test
    void addDeliveryAttempt_ShouldAddAttemptAndSetStatusToSentWhenSuccessful() {
        // Given
        assertTrue(notification.getDeliveryAttempts().isEmpty());
        assertEquals(NotificationStatus.PENDING, notification.getStatus());

        // When
        notification.addDeliveryAttempt(Channel.EMAIL, true, null);

        // Then
        assertEquals(1, notification.getDeliveryAttempts().size());
        assertEquals(NotificationStatus.SENT, notification.getStatus());

        DeliveryAttempt attempt = notification.getDeliveryAttempts().get(0);
        assertEquals(Channel.EMAIL, attempt.getChannel());
        assertTrue(attempt.isSuccessful());
        assertNull(attempt.getError());
        assertNotNull(attempt.getTimestamp());
    }

    @Test
    void addDeliveryAttempt_ShouldAddAttemptAndSetStatusToFailedWhenUnsuccessful() {
        // Given
        assertTrue(notification.getDeliveryAttempts().isEmpty());
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        String errorMessage = "Connection timeout";

        // When
        notification.addDeliveryAttempt(Channel.EMAIL, false, errorMessage);

        // Then
        assertEquals(1, notification.getDeliveryAttempts().size());
        assertEquals(NotificationStatus.FAILED, notification.getStatus());

        DeliveryAttempt attempt = notification.getDeliveryAttempts().get(0);
        assertEquals(Channel.EMAIL, attempt.getChannel());
        assertFalse(attempt.isSuccessful());
        assertEquals(errorMessage, attempt.getError());
        assertNotNull(attempt.getTimestamp());
    }

    @Test
    void addDeliveryAttempt_ShouldAddMultipleAttempts() {
        // Given
        assertTrue(notification.getDeliveryAttempts().isEmpty());

        // When
        notification.addDeliveryAttempt(Channel.EMAIL, false, "First attempt failed");
        notification.addDeliveryAttempt(Channel.EMAIL, true, null);

        // Then
        assertEquals(2, notification.getDeliveryAttempts().size());
        assertEquals(NotificationStatus.SENT, notification.getStatus());

        DeliveryAttempt firstAttempt = notification.getDeliveryAttempts().get(0);
        assertFalse(firstAttempt.isSuccessful());
        assertEquals("First attempt failed", firstAttempt.getError());

        DeliveryAttempt secondAttempt = notification.getDeliveryAttempts().get(1);
        assertTrue(secondAttempt.isSuccessful());
        assertNull(secondAttempt.getError());
    }

    @Test
    void markAsRead_ShouldSetStatusToReadAndSetReadAtTimestamp() {
        // Given
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        assertNull(notification.getReadAt());

        // When
        notification.markAsRead();

        // Then
        assertEquals(NotificationStatus.READ, notification.getStatus());
        assertNotNull(notification.getReadAt());
        assertTrue(notification.getReadAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(notification.getReadAt().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void markAsRead_ShouldUpdateReadAtEvenIfAlreadyRead() {
        // Given
        notification.markAsRead();
        LocalDateTime firstReadAt = notification.getReadAt();

        try {
            Thread.sleep(10); // Small delay to ensure different timestamp
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        notification.markAsRead();

        // Then
        assertEquals(NotificationStatus.READ, notification.getStatus());
        assertNotNull(notification.getReadAt());
        assertTrue(notification.getReadAt().isAfter(firstReadAt) ||
                   notification.getReadAt().equals(firstReadAt));
    }

    @Test
    void builder_ShouldCreateNotificationWithAllFields() {
        // Given
        NotificationId id = new NotificationId("id123");
        String userId = "user456";
        String userEmail = "user@example.com";
        String title = "Title";
        String message = "Message";
        NotificationType type = NotificationType.PAYMENT_COMPLETED;
        NotificationStatus status = NotificationStatus.SENT;
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime readAt = LocalDateTime.now();
        String metadata = "{\"test\":true}";

        // When
        Notification notification = Notification.builder()
                .id(id)
                .userId(userId)
                .userEmail(userEmail)
                .title(title)
                .message(message)
                .type(type)
                .status(status)
                .channels(new ArrayList<>())
                .deliveryAttempts(new ArrayList<>())
                .createdAt(createdAt)
                .readAt(readAt)
                .metadata(metadata)
                .build();

        // Then
        assertEquals(id, notification.getId());
        assertEquals(userId, notification.getUserId());
        assertEquals(userEmail, notification.getUserEmail());
        assertEquals(title, notification.getTitle());
        assertEquals(message, notification.getMessage());
        assertEquals(type, notification.getType());
        assertEquals(status, notification.getStatus());
        assertEquals(createdAt, notification.getCreatedAt());
        assertEquals(readAt, notification.getReadAt());
        assertEquals(metadata, notification.getMetadata());
        assertNotNull(notification.getChannels());
        assertNotNull(notification.getDeliveryAttempts());
    }
}

