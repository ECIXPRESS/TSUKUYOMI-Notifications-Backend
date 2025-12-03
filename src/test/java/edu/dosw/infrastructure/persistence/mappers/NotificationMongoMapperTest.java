package edu.dosw.infrastructure.persistence.mappers;

import edu.dosw.domain.model.Notification;
import edu.dosw.domain.model.ValueObject.*;
import edu.dosw.infrastructure.persistence.documents.DeliveryAttemptDocument;
import edu.dosw.infrastructure.persistence.documents.NotificationDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationMongoMapperTest {

    private NotificationMongoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NotificationMongoMapper();
    }

    @Test
    void toDocument_ShouldMapAllFieldsCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime readAt = LocalDateTime.now().minusHours(1);

        DeliveryAttempt deliveryAttempt = new DeliveryAttempt(
                Channel.EMAIL,
                true,
                null,
                now
        );

        Notification notification = Notification.builder()
                .id(new NotificationId("notif123"))
                .userId("user123")
                .userEmail("test@example.com")
                .title("Test Title")
                .message("Test Message")
                .type(NotificationType.ORDER_CONFIRMED)
                .status(NotificationStatus.SENT)
                .channels(Arrays.asList(Channel.EMAIL, Channel.WEB_SOCKET))
                .deliveryAttempts(Arrays.asList(deliveryAttempt))
                .createdAt(now)
                .readAt(readAt)
                .metadata("{\"key\":\"value\"}")
                .build();

        // When
        NotificationDocument document = mapper.toDocument(notification);

        // Then
        assertNotNull(document);
        assertEquals("notif123", document.getId());
        assertEquals("user123", document.getUserId());
        assertEquals("test@example.com", document.getUserEmail());
        assertEquals("Test Title", document.getTitle());
        assertEquals("Test Message", document.getMessage());
        assertEquals("ORDER_CONFIRMED", document.getType());
        assertEquals("SENT", document.getStatus());
        assertEquals(2, document.getChannels().size());
        assertTrue(document.getChannels().contains("EMAIL"));
        assertTrue(document.getChannels().contains("WEB_SOCKET"));
        assertEquals(1, document.getDeliveryAttempts().size());
        assertEquals(now, document.getCreatedAt());
        assertEquals(readAt, document.getReadAt());
        assertEquals("{\"key\":\"value\"}", document.getMetadata());
    }

    @Test
    void toDocument_ShouldHandleNullNotification() {
        // When
        NotificationDocument document = mapper.toDocument(null);

        // Then
        assertNull(document);
    }

    @Test
    void toDocument_ShouldMapDeliveryAttemptsCorrectly() {
        // Given
        LocalDateTime timestamp1 = LocalDateTime.now();
        LocalDateTime timestamp2 = LocalDateTime.now().minusMinutes(5);

        DeliveryAttempt attempt1 = new DeliveryAttempt(
                Channel.EMAIL,
                true,
                null,
                timestamp1
        );

        DeliveryAttempt attempt2 = new DeliveryAttempt(
                Channel.WEB_SOCKET,
                false,
                "Connection timeout",
                timestamp2
        );

        Notification notification = Notification.builder()
                .id(new NotificationId("notif123"))
                .userId("user123")
                .userEmail("test@example.com")
                .title("Title")
                .message("Message")
                .type(NotificationType.ORDER_CONFIRMED)
                .status(NotificationStatus.SENT)
                .channels(new ArrayList<>())
                .deliveryAttempts(Arrays.asList(attempt1, attempt2))
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .metadata(null)
                .build();

        // When
        NotificationDocument document = mapper.toDocument(notification);

        // Then
        assertNotNull(document);
        assertEquals(2, document.getDeliveryAttempts().size());

        DeliveryAttemptDocument docAttempt1 = document.getDeliveryAttempts().get(0);
        assertEquals("EMAIL", docAttempt1.getChannel());
        assertTrue(docAttempt1.isSuccessful());
        assertNull(docAttempt1.getError());
        assertEquals(timestamp1, docAttempt1.getTimestamp());

        DeliveryAttemptDocument docAttempt2 = document.getDeliveryAttempts().get(1);
        assertEquals("WEB_SOCKET", docAttempt2.getChannel());
        assertFalse(docAttempt2.isSuccessful());
        assertEquals("Connection timeout", docAttempt2.getError());
        assertEquals(timestamp2, docAttempt2.getTimestamp());
    }

    @Test
    void toDomain_ShouldMapAllFieldsCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime readAt = LocalDateTime.now().minusHours(1);

        DeliveryAttemptDocument deliveryAttemptDoc = new DeliveryAttemptDocument(
                "EMAIL",
                true,
                null,
                now
        );

        NotificationDocument document = new NotificationDocument();
        document.setId("notif123");
        document.setUserId("user123");
        document.setUserEmail("test@example.com");
        document.setTitle("Test Title");
        document.setMessage("Test Message");
        document.setType("ORDER_CONFIRMED");
        document.setStatus("SENT");
        document.setChannels(Arrays.asList("EMAIL", "WEB_SOCKET"));
        document.setDeliveryAttempts(Arrays.asList(deliveryAttemptDoc));
        document.setCreatedAt(now);
        document.setReadAt(readAt);
        document.setMetadata("{\"key\":\"value\"}");

        // When
        Notification notification = mapper.toDomain(document);

        // Then
        assertNotNull(notification);
        assertEquals("notif123", notification.getId().getValue());
        assertEquals("user123", notification.getUserId());
        assertEquals("test@example.com", notification.getUserEmail());
        assertEquals("Test Title", notification.getTitle());
        assertEquals("Test Message", notification.getMessage());
        assertEquals(NotificationType.ORDER_CONFIRMED, notification.getType());
        assertEquals(NotificationStatus.SENT, notification.getStatus());
        assertEquals(2, notification.getChannels().size());
        assertTrue(notification.getChannels().contains(Channel.EMAIL));
        assertTrue(notification.getChannels().contains(Channel.WEB_SOCKET));
        assertEquals(1, notification.getDeliveryAttempts().size());
        assertEquals(now, notification.getCreatedAt());
        assertEquals(readAt, notification.getReadAt());
        assertEquals("{\"key\":\"value\"}", notification.getMetadata());
    }

    @Test
    void toDomain_ShouldHandleNullDocument() {
        // When
        Notification notification = mapper.toDomain(null);

        // Then
        assertNull(notification);
    }

    @Test
    void toDomain_ShouldMapDeliveryAttemptsCorrectly() {
        // Given
        LocalDateTime timestamp1 = LocalDateTime.now();
        LocalDateTime timestamp2 = LocalDateTime.now().minusMinutes(5);

        DeliveryAttemptDocument docAttempt1 = new DeliveryAttemptDocument(
                "EMAIL",
                true,
                null,
                timestamp1
        );

        DeliveryAttemptDocument docAttempt2 = new DeliveryAttemptDocument(
                "WEB_SOCKET",
                false,
                "Connection timeout",
                timestamp2
        );

        NotificationDocument document = new NotificationDocument();
        document.setId("notif123");
        document.setUserId("user123");
        document.setUserEmail("test@example.com");
        document.setTitle("Title");
        document.setMessage("Message");
        document.setType("ORDER_CONFIRMED");
        document.setStatus("SENT");
        document.setChannels(new ArrayList<>());
        document.setDeliveryAttempts(Arrays.asList(docAttempt1, docAttempt2));
        document.setCreatedAt(LocalDateTime.now());
        document.setReadAt(null);
        document.setMetadata(null);

        // When
        Notification notification = mapper.toDomain(document);

        // Then
        assertNotNull(notification);
        assertEquals(2, notification.getDeliveryAttempts().size());

        DeliveryAttempt attempt1 = notification.getDeliveryAttempts().get(0);
        assertEquals(Channel.EMAIL, attempt1.getChannel());
        assertTrue(attempt1.isSuccessful());
        assertNull(attempt1.getError());
        assertEquals(timestamp1, attempt1.getTimestamp());

        DeliveryAttempt attempt2 = notification.getDeliveryAttempts().get(1);
        assertEquals(Channel.WEB_SOCKET, attempt2.getChannel());
        assertFalse(attempt2.isSuccessful());
        assertEquals("Connection timeout", attempt2.getError());
        assertEquals(timestamp2, attempt2.getTimestamp());
    }

    @Test
    void toDocument_toDomain_ShouldBeReversible() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        Notification originalNotification = Notification.builder()
                .id(new NotificationId("notif123"))
                .userId("user123")
                .userEmail("test@example.com")
                .title("Test Title")
                .message("Test Message")
                .type(NotificationType.PAYMENT_COMPLETED)
                .status(NotificationStatus.READ)
                .channels(Arrays.asList(Channel.EMAIL, Channel.PUSH, Channel.WEB_SOCKET))
                .deliveryAttempts(new ArrayList<>())
                .createdAt(now)
                .readAt(now)
                .metadata("{\"test\":true}")
                .build();

        // When
        NotificationDocument document = mapper.toDocument(originalNotification);
        Notification resultNotification = mapper.toDomain(document);

        // Then
        assertNotNull(resultNotification);
        assertEquals(originalNotification.getId().getValue(), resultNotification.getId().getValue());
        assertEquals(originalNotification.getUserId(), resultNotification.getUserId());
        assertEquals(originalNotification.getUserEmail(), resultNotification.getUserEmail());
        assertEquals(originalNotification.getTitle(), resultNotification.getTitle());
        assertEquals(originalNotification.getMessage(), resultNotification.getMessage());
        assertEquals(originalNotification.getType(), resultNotification.getType());
        assertEquals(originalNotification.getStatus(), resultNotification.getStatus());
        assertEquals(originalNotification.getChannels().size(), resultNotification.getChannels().size());
        assertEquals(originalNotification.getCreatedAt(), resultNotification.getCreatedAt());
        assertEquals(originalNotification.getReadAt(), resultNotification.getReadAt());
        assertEquals(originalNotification.getMetadata(), resultNotification.getMetadata());
    }

    @Test
    void toDocument_ShouldHandleEmptyCollections() {
        // Given
        Notification notification = Notification.builder()
                .id(new NotificationId("notif123"))
                .userId("user123")
                .userEmail("test@example.com")
                .title("Title")
                .message("Message")
                .type(NotificationType.SYSTEM)
                .status(NotificationStatus.PENDING)
                .channels(new ArrayList<>())
                .deliveryAttempts(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .metadata(null)
                .build();

        // When
        NotificationDocument document = mapper.toDocument(notification);

        // Then
        assertNotNull(document);
        assertNotNull(document.getChannels());
        assertTrue(document.getChannels().isEmpty());
        assertNotNull(document.getDeliveryAttempts());
        assertTrue(document.getDeliveryAttempts().isEmpty());
    }

    @Test
    void toDomain_ShouldHandleEmptyCollections() {
        // Given
        NotificationDocument document = new NotificationDocument();
        document.setId("notif123");
        document.setUserId("user123");
        document.setUserEmail("test@example.com");
        document.setTitle("Title");
        document.setMessage("Message");
        document.setType("SYSTEM");
        document.setStatus("PENDING");
        document.setChannels(new ArrayList<>());
        document.setDeliveryAttempts(new ArrayList<>());
        document.setCreatedAt(LocalDateTime.now());
        document.setReadAt(null);
        document.setMetadata(null);

        // When
        Notification notification = mapper.toDomain(document);

        // Then
        assertNotNull(notification);
        assertNotNull(notification.getChannels());
        assertTrue(notification.getChannels().isEmpty());
        assertNotNull(notification.getDeliveryAttempts());
        assertTrue(notification.getDeliveryAttempts().isEmpty());
    }
}

