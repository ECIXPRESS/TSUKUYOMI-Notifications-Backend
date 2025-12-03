package edu.dosw.application.services;

import edu.dosw.application.dto.query.NotificationResponse;
import edu.dosw.domain.model.Notification;
import edu.dosw.domain.model.ValueObject.*;
import edu.dosw.domain.ports.NotificationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {

    @Mock
    private NotificationRepositoryPort notificationRepositoryPort;

    @InjectMocks
    private NotificationQueryService notificationQueryService;

    private Notification notification;
    private String testUserId;
    private String testNotificationId;

    @BeforeEach
    void setUp() {
        testUserId = "user123";
        testNotificationId = "notif123";

        notification = Notification.builder()
                .id(new NotificationId(testNotificationId))
                .userId(testUserId)
                .userEmail("test@example.com")
                .title("Test Title")
                .message("Test Message")
                .type(NotificationType.ORDER_CONFIRMED)
                .status(NotificationStatus.SENT)
                .channels(new ArrayList<>(Arrays.asList(Channel.EMAIL, Channel.WEB_SOCKET)))
                .deliveryAttempts(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .metadata("{\"key\":\"value\"}")
                .build();
    }

    @Test
    void getByUserId_ShouldReturnListOfNotificationResponses() {
        // Given
        List<Notification> notifications = Arrays.asList(notification);
        when(notificationRepositoryPort.findByUserId(testUserId)).thenReturn(notifications);

        // When
        List<NotificationResponse> result = notificationQueryService.getByUserId(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());

        NotificationResponse response = result.get(0);
        assertEquals(testNotificationId, response.getId());
        assertEquals(testUserId, response.getUserId());
        assertEquals("test@example.com", response.getUserEmail());
        assertEquals("Test Title", response.getTitle());
        assertEquals("Test Message", response.getMessage());
        assertEquals("ORDER_CONFIRMED", response.getType());
        assertEquals("SENT", response.getStatus());
        assertEquals("{\"key\":\"value\"}", response.getMetadata());
        assertNull(response.getReadAt());

        verify(notificationRepositoryPort, times(1)).findByUserId(testUserId);
    }

    @Test
    void getByUserId_ShouldReturnEmptyListWhenNoNotificationsFound() {
        // Given
        when(notificationRepositoryPort.findByUserId(testUserId)).thenReturn(new ArrayList<>());

        // When
        List<NotificationResponse> result = notificationQueryService.getByUserId(testUserId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(notificationRepositoryPort, times(1)).findByUserId(testUserId);
    }

    @Test
    void getById_ShouldReturnNotificationResponseWhenFound() {
        // Given
        when(notificationRepositoryPort.findById(testNotificationId)).thenReturn(Optional.of(notification));

        // When
        Optional<NotificationResponse> result = notificationQueryService.getById(testNotificationId);

        // Then
        assertTrue(result.isPresent());

        NotificationResponse response = result.get();
        assertEquals(testNotificationId, response.getId());
        assertEquals(testUserId, response.getUserId());
        assertEquals("test@example.com", response.getUserEmail());
        assertEquals("Test Title", response.getTitle());
        assertEquals("Test Message", response.getMessage());
        assertEquals("ORDER_CONFIRMED", response.getType());
        assertEquals("SENT", response.getStatus());

        verify(notificationRepositoryPort, times(1)).findById(testNotificationId);
    }

    @Test
    void getById_ShouldReturnEmptyOptionalWhenNotFound() {
        // Given
        when(notificationRepositoryPort.findById(testNotificationId)).thenReturn(Optional.empty());

        // When
        Optional<NotificationResponse> result = notificationQueryService.getById(testNotificationId);

        // Then
        assertFalse(result.isPresent());
        verify(notificationRepositoryPort, times(1)).findById(testNotificationId);
    }

    @Test
    void markAsRead_ShouldMarkNotificationAsReadAndSave() {
        // Given
        when(notificationRepositoryPort.findById(testNotificationId)).thenReturn(Optional.of(notification));
        when(notificationRepositoryPort.save(any(Notification.class))).thenReturn(notification);

        // When
        notificationQueryService.markAsRead(testNotificationId);

        // Then
        assertEquals(NotificationStatus.READ, notification.getStatus());
        assertNotNull(notification.getReadAt());

        verify(notificationRepositoryPort, times(1)).findById(testNotificationId);
        verify(notificationRepositoryPort, times(1)).save(notification);
    }

    @Test
    void markAsRead_ShouldDoNothingWhenNotificationNotFound() {
        // Given
        when(notificationRepositoryPort.findById(testNotificationId)).thenReturn(Optional.empty());

        // When
        notificationQueryService.markAsRead(testNotificationId);

        // Then
        verify(notificationRepositoryPort, times(1)).findById(testNotificationId);
        verify(notificationRepositoryPort, never()).save(any(Notification.class));
    }

    @Test
    void getUnreadByUserId_ShouldReturnOnlyUnreadNotifications() {
        // Given
        Notification sentNotification = Notification.builder()
                .id(new NotificationId("notif1"))
                .userId(testUserId)
                .userEmail("test@example.com")
                .title("Sent Notification")
                .message("Message")
                .type(NotificationType.ORDER_CONFIRMED)
                .status(NotificationStatus.SENT)
                .channels(new ArrayList<>())
                .deliveryAttempts(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .metadata(null)
                .build();

        Notification pendingNotification = Notification.builder()
                .id(new NotificationId("notif2"))
                .userId(testUserId)
                .userEmail("test@example.com")
                .title("Pending Notification")
                .message("Message")
                .type(NotificationType.ORDER_CONFIRMED)
                .status(NotificationStatus.PENDING)
                .channels(new ArrayList<>())
                .deliveryAttempts(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .metadata(null)
                .build();

        Notification readNotification = Notification.builder()
                .id(new NotificationId("notif3"))
                .userId(testUserId)
                .userEmail("test@example.com")
                .title("Read Notification")
                .message("Message")
                .type(NotificationType.ORDER_CONFIRMED)
                .status(NotificationStatus.READ)
                .channels(new ArrayList<>())
                .deliveryAttempts(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .readAt(LocalDateTime.now())
                .metadata(null)
                .build();

        Notification failedNotification = Notification.builder()
                .id(new NotificationId("notif4"))
                .userId(testUserId)
                .userEmail("test@example.com")
                .title("Failed Notification")
                .message("Message")
                .type(NotificationType.ORDER_CONFIRMED)
                .status(NotificationStatus.FAILED)
                .channels(new ArrayList<>())
                .deliveryAttempts(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .metadata(null)
                .build();

        List<Notification> allNotifications = Arrays.asList(
                sentNotification,
                pendingNotification,
                readNotification,
                failedNotification
        );

        when(notificationRepositoryPort.findByUserId(testUserId)).thenReturn(allNotifications);

        // When
        List<NotificationResponse> result = notificationQueryService.getUnreadByUserId(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        assertTrue(result.stream().anyMatch(r -> r.getId().equals("notif1")));
        assertTrue(result.stream().anyMatch(r -> r.getId().equals("notif2")));
        assertFalse(result.stream().anyMatch(r -> r.getId().equals("notif3")));
        assertFalse(result.stream().anyMatch(r -> r.getId().equals("notif4")));

        verify(notificationRepositoryPort, times(1)).findByUserId(testUserId);
    }

    @Test
    void getUnreadByUserId_ShouldReturnEmptyListWhenAllNotificationsAreRead() {
        // Given
        Notification readNotification = Notification.builder()
                .id(new NotificationId("notif1"))
                .userId(testUserId)
                .userEmail("test@example.com")
                .title("Read Notification")
                .message("Message")
                .type(NotificationType.ORDER_CONFIRMED)
                .status(NotificationStatus.READ)
                .channels(new ArrayList<>())
                .deliveryAttempts(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .readAt(LocalDateTime.now())
                .metadata(null)
                .build();

        when(notificationRepositoryPort.findByUserId(testUserId)).thenReturn(Arrays.asList(readNotification));

        // When
        List<NotificationResponse> result = notificationQueryService.getUnreadByUserId(testUserId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(notificationRepositoryPort, times(1)).findByUserId(testUserId);
    }

    @Test
    void toResponse_ShouldMapNotificationWithReadAt() {
        // Given
        LocalDateTime readAt = LocalDateTime.now();
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(readAt);

        when(notificationRepositoryPort.findById(testNotificationId)).thenReturn(Optional.of(notification));

        // When
        Optional<NotificationResponse> result = notificationQueryService.getById(testNotificationId);

        // Then
        assertTrue(result.isPresent());
        NotificationResponse response = result.get();
        assertEquals(readAt, response.getReadAt());
        assertEquals("READ", response.getStatus());
    }
}

