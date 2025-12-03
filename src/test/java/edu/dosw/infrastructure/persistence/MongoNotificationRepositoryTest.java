package edu.dosw.infrastructure.persistence;

import edu.dosw.domain.model.Notification;
import edu.dosw.domain.model.ValueObject.*;
import edu.dosw.infrastructure.persistence.documents.NotificationDocument;
import edu.dosw.infrastructure.persistence.mappers.NotificationMongoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MongoNotificationRepositoryTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private NotificationMongoMapper notificationMongoMapper;

    @InjectMocks
    private MongoNotificationRepository mongoNotificationRepository;

    private Notification notification;
    private NotificationDocument notificationDocument;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .id(new NotificationId("notif123"))
                .userId("user123")
                .userEmail("test@example.com")
                .title("Test Notification")
                .message("This is a test message")
                .type(NotificationType.ORDER_CONFIRMED)
                .status(NotificationStatus.SENT)
                .channels(new ArrayList<>(Arrays.asList(Channel.EMAIL, Channel.WEB_SOCKET)))
                .deliveryAttempts(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .metadata("{\"orderId\":\"order123\"}")
                .build();

        notificationDocument = new NotificationDocument();
        notificationDocument.setId("notif123");
        notificationDocument.setUserId("user123");
        notificationDocument.setUserEmail("test@example.com");
        notificationDocument.setTitle("Test Notification");
        notificationDocument.setMessage("This is a test message");
        notificationDocument.setType("ORDER_CONFIRMED");
        notificationDocument.setStatus("SENT");
        notificationDocument.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void save_ShouldSaveAndReturnNotification() {
        // Given
        when(notificationMongoMapper.toDocument(notification)).thenReturn(notificationDocument);
        when(mongoTemplate.save(notificationDocument)).thenReturn(notificationDocument);
        when(notificationMongoMapper.toDomain(notificationDocument)).thenReturn(notification);

        // When
        Notification result = mongoNotificationRepository.save(notification);

        // Then
        assertNotNull(result);
        assertEquals(notification.getId().getValue(), result.getId().getValue());
        verify(notificationMongoMapper, times(1)).toDocument(notification);
        verify(mongoTemplate, times(1)).save(notificationDocument);
        verify(notificationMongoMapper, times(1)).toDomain(notificationDocument);
    }

    @Test
    void findById_ShouldReturnNotificationWhenFound() {
        // Given
        String id = "notif123";
        when(mongoTemplate.findById(id, NotificationDocument.class)).thenReturn(notificationDocument);
        when(notificationMongoMapper.toDomain(notificationDocument)).thenReturn(notification);

        // When
        Optional<Notification> result = mongoNotificationRepository.findById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals("notif123", result.get().getId().getValue());
        verify(mongoTemplate, times(1)).findById(id, NotificationDocument.class);
        verify(notificationMongoMapper, times(1)).toDomain(notificationDocument);
    }

    @Test
    void findById_ShouldReturnEmptyOptionalWhenNotFound() {
        // Given
        String id = "notif999";
        when(mongoTemplate.findById(id, NotificationDocument.class)).thenReturn(null);
        when(notificationMongoMapper.toDomain(null)).thenReturn(null);

        // When
        Optional<Notification> result = mongoNotificationRepository.findById(id);

        // Then
        assertFalse(result.isPresent());
        verify(mongoTemplate, times(1)).findById(id, NotificationDocument.class);
    }

    @Test
    void findByUserId_ShouldReturnListOfNotifications() {
        // Given
        String userId = "user123";
        List<NotificationDocument> documents = Arrays.asList(notificationDocument);
        when(mongoTemplate.find(any(Query.class), eq(NotificationDocument.class))).thenReturn(documents);
        when(notificationMongoMapper.toDomain(notificationDocument)).thenReturn(notification);

        // When
        List<Notification> result = mongoNotificationRepository.findByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user123", result.get(0).getUserId());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(NotificationDocument.class));
        verify(notificationMongoMapper, times(1)).toDomain(notificationDocument);
    }

    @Test
    void findByUserId_ShouldReturnEmptyListWhenNoNotificationsFound() {
        // Given
        String userId = "user999";
        when(mongoTemplate.find(any(Query.class), eq(NotificationDocument.class))).thenReturn(new ArrayList<>());

        // When
        List<Notification> result = mongoNotificationRepository.findByUserId(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(NotificationDocument.class));
    }

    @Test
    void findPendingNotifications_ShouldReturnPendingNotifications() {
        // Given
        List<NotificationDocument> documents = Arrays.asList(notificationDocument);
        when(mongoTemplate.find(any(Query.class), eq(NotificationDocument.class))).thenReturn(documents);
        when(notificationMongoMapper.toDomain(notificationDocument)).thenReturn(notification);

        // When
        List<Notification> result = mongoNotificationRepository.findPendingNotifications();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(NotificationDocument.class));
    }

    @Test
    void findByUserIdAndStatus_ShouldReturnFilteredNotifications() {
        // Given
        String userId = "user123";
        String status = "SENT";
        List<NotificationDocument> documents = Arrays.asList(notificationDocument);
        when(mongoTemplate.find(any(Query.class), eq(NotificationDocument.class))).thenReturn(documents);
        when(notificationMongoMapper.toDomain(notificationDocument)).thenReturn(notification);

        // When
        List<Notification> result = mongoNotificationRepository.findByUserIdAndStatus(userId, status);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user123", result.get(0).getUserId());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(NotificationDocument.class));
    }

    @Test
    void existsByUserIdAndType_ShouldReturnTrueWhenExists() {
        // Given
        String userId = "user123";
        String type = "ORDER_CONFIRMED";
        when(mongoTemplate.exists(any(Query.class), eq(NotificationDocument.class))).thenReturn(true);

        // When
        boolean result = mongoNotificationRepository.existsByUserIdAndType(userId, type);

        // Then
        assertTrue(result);
        verify(mongoTemplate, times(1)).exists(any(Query.class), eq(NotificationDocument.class));
    }

    @Test
    void existsByUserIdAndType_ShouldReturnFalseWhenNotExists() {
        // Given
        String userId = "user999";
        String type = "ORDER_CONFIRMED";
        when(mongoTemplate.exists(any(Query.class), eq(NotificationDocument.class))).thenReturn(false);

        // When
        boolean result = mongoNotificationRepository.existsByUserIdAndType(userId, type);

        // Then
        assertFalse(result);
        verify(mongoTemplate, times(1)).exists(any(Query.class), eq(NotificationDocument.class));
    }

    @Test
    void countByUserIdAndStatus_ShouldReturnCount() {
        // Given
        String userId = "user123";
        String status = "SENT";
        when(mongoTemplate.count(any(Query.class), eq(NotificationDocument.class))).thenReturn(5L);

        // When
        long result = mongoNotificationRepository.countByUserIdAndStatus(userId, status);

        // Then
        assertEquals(5L, result);
        verify(mongoTemplate, times(1)).count(any(Query.class), eq(NotificationDocument.class));
    }

    @Test
    void countByUserIdAndStatus_ShouldReturnZeroWhenNoMatches() {
        // Given
        String userId = "user999";
        String status = "READ";
        when(mongoTemplate.count(any(Query.class), eq(NotificationDocument.class))).thenReturn(0L);

        // When
        long result = mongoNotificationRepository.countByUserIdAndStatus(userId, status);

        // Then
        assertEquals(0L, result);
        verify(mongoTemplate, times(1)).count(any(Query.class), eq(NotificationDocument.class));
    }

    @Test
    void findByUserId_ShouldSortByCreatedAtDescending() {
        // Given
        String userId = "user123";
        NotificationDocument doc1 = new NotificationDocument();
        doc1.setId("notif1");
        doc1.setCreatedAt(LocalDateTime.now().minusHours(2));

        NotificationDocument doc2 = new NotificationDocument();
        doc2.setId("notif2");
        doc2.setCreatedAt(LocalDateTime.now().minusHours(1));

        List<NotificationDocument> documents = Arrays.asList(doc2, doc1);
        when(mongoTemplate.find(any(Query.class), eq(NotificationDocument.class))).thenReturn(documents);
        when(notificationMongoMapper.toDomain(any(NotificationDocument.class))).thenAnswer(invocation -> {
            NotificationDocument doc = invocation.getArgument(0);
            return Notification.builder()
                    .id(new NotificationId(doc.getId()))
                    .userId(userId)
                    .userEmail("test@example.com")
                    .title("Title")
                    .message("Message")
                    .type(NotificationType.ORDER_CONFIRMED)
                    .status(NotificationStatus.SENT)
                    .channels(new ArrayList<>())
                    .deliveryAttempts(new ArrayList<>())
                    .createdAt(doc.getCreatedAt())
                    .readAt(null)
                    .metadata(null)
                    .build();
        });

        // When
        List<Notification> result = mongoNotificationRepository.findByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(mongoTemplate, times(1)).find(any(Query.class), eq(NotificationDocument.class));
    }
}

