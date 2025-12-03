package edu.dosw.infrastructure.web.controllers;

import edu.dosw.application.dto.query.NotificationResponse;
import edu.dosw.application.ports.NotificationQueryPort;
import edu.dosw.infrastructure.web.mappers.NotificationWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationQueryPort notificationQueryPort;

    @Mock
    private NotificationWebMapper notificationWebMapper;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        notificationResponse = NotificationResponse.builder()
                .id("notif123")
                .userId("user123")
                .userEmail("test@example.com")
                .title("Test Notification")
                .message("Test Message")
                .type("ORDER_CONFIRMED")
                .status("SENT")
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .metadata("{\"orderId\":\"order123\"}")
                .build();
    }

    @Test
    void getUserNotifications_ShouldReturnNotifications() {
        // Given
        String userId = "user123";
        List<NotificationResponse> notifications = Arrays.asList(notificationResponse);
        when(notificationQueryPort.getByUserId(userId)).thenReturn(notifications);

        // When
        ResponseEntity<List<NotificationResponse>> response = notificationController.getUserNotifications(userId, null, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("notif123", response.getBody().get(0).getId());
        verify(notificationQueryPort, times(1)).getByUserId(userId);
    }

    @Test
    void getUserNotifications_ShouldFilterByStatus() {
        // Given
        String userId = "user123";
        NotificationResponse sentNotif = NotificationResponse.builder()
                .id("notif1")
                .userId(userId)
                .status("SENT")
                .build();

        NotificationResponse readNotif = NotificationResponse.builder()
                .id("notif2")
                .userId(userId)
                .status("READ")
                .build();

        List<NotificationResponse> notifications = Arrays.asList(sentNotif, readNotif);
        when(notificationQueryPort.getByUserId(userId)).thenReturn(notifications);

        // When
        ResponseEntity<List<NotificationResponse>> response = notificationController.getUserNotifications(userId, "SENT", null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("SENT", response.getBody().get(0).getStatus());
    }

    @Test
    void getUserNotifications_ShouldFilterByType() {
        // Given
        String userId = "user123";
        NotificationResponse orderNotif = NotificationResponse.builder()
                .id("notif1")
                .userId(userId)
                .type("ORDER_CONFIRMED")
                .build();

        NotificationResponse paymentNotif = NotificationResponse.builder()
                .id("notif2")
                .userId(userId)
                .type("PAYMENT_COMPLETED")
                .build();

        List<NotificationResponse> notifications = Arrays.asList(orderNotif, paymentNotif);
        when(notificationQueryPort.getByUserId(userId)).thenReturn(notifications);

        // When
        ResponseEntity<List<NotificationResponse>> response = notificationController.getUserNotifications(userId, null, "ORDER_CONFIRMED");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("ORDER_CONFIRMED", response.getBody().get(0).getType());
    }

    @Test
    void getUserNotifications_ShouldFilterByStatusAndType() {
        // Given
        String userId = "user123";
        NotificationResponse matchingNotif = NotificationResponse.builder()
                .id("notif1")
                .userId(userId)
                .status("SENT")
                .type("ORDER_CONFIRMED")
                .build();

        NotificationResponse nonMatchingNotif = NotificationResponse.builder()
                .id("notif2")
                .userId(userId)
                .status("READ")
                .type("PAYMENT_COMPLETED")
                .build();

        List<NotificationResponse> notifications = Arrays.asList(matchingNotif, nonMatchingNotif);
        when(notificationQueryPort.getByUserId(userId)).thenReturn(notifications);

        // When
        ResponseEntity<List<NotificationResponse>> response = notificationController.getUserNotifications(userId, "SENT", "ORDER_CONFIRMED");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("notif1", response.getBody().get(0).getId());
    }

    @Test
    void getUserNotifications_ShouldReturnInternalServerErrorOnException() {
        // Given
        String userId = "user123";
        when(notificationQueryPort.getByUserId(userId)).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<List<NotificationResponse>> response = notificationController.getUserNotifications(userId, null, null);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getUnreadUserNotifications_ShouldReturnUnreadNotifications() {
        // Given
        String userId = "user123";
        List<NotificationResponse> unreadNotifications = Arrays.asList(notificationResponse);
        when(notificationQueryPort.getUnreadByUserId(userId)).thenReturn(unreadNotifications);

        // When
        ResponseEntity<List<NotificationResponse>> response = notificationController.getUnreadUserNotifications(userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(notificationQueryPort, times(1)).getUnreadByUserId(userId);
    }

    @Test
    void getUnreadUserNotifications_ShouldReturnInternalServerErrorOnException() {
        // Given
        String userId = "user123";
        when(notificationQueryPort.getUnreadByUserId(userId)).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<List<NotificationResponse>> response = notificationController.getUnreadUserNotifications(userId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getNotification_ShouldReturnNotificationWhenFound() {
        // Given
        String id = "notif123";
        when(notificationQueryPort.getById(id)).thenReturn(Optional.of(notificationResponse));

        // When
        ResponseEntity<NotificationResponse> response = notificationController.getNotification(id);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("notif123", response.getBody().getId());
        verify(notificationQueryPort, times(1)).getById(id);
    }

    @Test
    void getNotification_ShouldReturnNotFoundWhenNotExists() {
        // Given
        String id = "notif999";
        when(notificationQueryPort.getById(id)).thenReturn(Optional.empty());

        // When
        ResponseEntity<NotificationResponse> response = notificationController.getNotification(id);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(notificationQueryPort, times(1)).getById(id);
    }

    @Test
    void getNotification_ShouldReturnInternalServerErrorOnException() {
        // Given
        String id = "notif123";
        when(notificationQueryPort.getById(id)).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<NotificationResponse> response = notificationController.getNotification(id);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void markAsRead_ShouldReturnOkWhenSuccessful() {
        // Given
        String id = "notif123";
        doNothing().when(notificationQueryPort).markAsRead(id);

        // When
        ResponseEntity<Void> response = notificationController.markAsRead(id);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationQueryPort, times(1)).markAsRead(id);
    }

    @Test
    void markAsRead_ShouldReturnInternalServerErrorOnException() {
        // Given
        String id = "notif123";
        doThrow(new RuntimeException("Database error")).when(notificationQueryPort).markAsRead(id);

        // When
        ResponseEntity<Void> response = notificationController.markAsRead(id);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void deleteNotification_ShouldReturnOk() {
        // Given
        String id = "notif123";

        // When
        ResponseEntity<Void> response = notificationController.deleteNotification(id);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getUserNotificationStats_ShouldReturnStatsCorrectly() {
        // Given
        String userId = "user123";

        NotificationResponse sentNotif1 = NotificationResponse.builder()
                .id("notif1")
                .userId(userId)
                .status("SENT")
                .build();

        NotificationResponse sentNotif2 = NotificationResponse.builder()
                .id("notif2")
                .userId(userId)
                .status("PENDING")
                .build();

        NotificationResponse readNotif = NotificationResponse.builder()
                .id("notif3")
                .userId(userId)
                .status("READ")
                .build();

        List<NotificationResponse> notifications = Arrays.asList(sentNotif1, sentNotif2, readNotif);
        when(notificationQueryPort.getByUserId(userId)).thenReturn(notifications);

        // When
        ResponseEntity<Object> response = notificationController.getUserNotificationStats(userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) response.getBody();
        assertEquals(3L, stats.get("totalNotifications"));
        assertEquals(2L, stats.get("unreadNotifications"));
        assertEquals(1L, stats.get("readNotifications"));
        assertEquals(userId, stats.get("userId"));
    }

    @Test
    void getUserNotificationStats_ShouldReturnZeroStatsWhenNoNotifications() {
        // Given
        String userId = "user123";
        when(notificationQueryPort.getByUserId(userId)).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<Object> response = notificationController.getUserNotificationStats(userId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) response.getBody();
        assertEquals(0L, stats.get("totalNotifications"));
        assertEquals(0L, stats.get("unreadNotifications"));
        assertEquals(0L, stats.get("readNotifications"));
    }

    @Test
    void getUserNotificationStats_ShouldReturnInternalServerErrorOnException() {
        // Given
        String userId = "user123";
        when(notificationQueryPort.getByUserId(userId)).thenThrow(new RuntimeException("Database error"));

        // When
        ResponseEntity<Object> response = notificationController.getUserNotificationStats(userId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void getUserNotifications_ShouldReturnEmptyListWhenNoNotifications() {
        // Given
        String userId = "user123";
        when(notificationQueryPort.getByUserId(userId)).thenReturn(new ArrayList<>());

        // When
        ResponseEntity<List<NotificationResponse>> response = notificationController.getUserNotifications(userId, null, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }
}

