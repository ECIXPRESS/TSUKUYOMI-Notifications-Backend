package edu.dosw.infrastructure.websocket;

import edu.dosw.domain.model.Notification;
import edu.dosw.domain.model.ValueObject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketEmitterAdapterTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketEmitterAdapter webSocketEmitterAdapter;

    private Notification notification;

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
                .channels(new ArrayList<>(Arrays.asList(Channel.WEB_SOCKET)))
                .deliveryAttempts(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .metadata("{\"orderId\":\"order123\"}")
                .build();
    }

    @Test
    void emitUserNotification_ShouldSendMessageToUserTopic() {
        // Given
        String userId = "user123";
        doNothing().when(messagingTemplate).convertAndSend(anyString(), (Object) any());

        // When
        webSocketEmitterAdapter.emitUserNotification(userId, notification);

        // Then
        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/notifications/user123"),
                (Object) any()
        );
    }

    @Test
    void emitUserNotification_ShouldHandleException() {
        // Given
        String userId = "user123";
        doThrow(new RuntimeException("WebSocket error")).when(messagingTemplate).convertAndSend(anyString(), (Object) any());

        // When & Then
        assertDoesNotThrow(() -> webSocketEmitterAdapter.emitUserNotification(userId, notification));

        verify(messagingTemplate, times(1)).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void emitGlobalNotification_ShouldSendMessageToGlobalTopic() {
        // Given
        doNothing().when(messagingTemplate).convertAndSend(anyString(), (Object) any());

        // When
        webSocketEmitterAdapter.emitGlobalNotification(notification);

        // Then
        verify(messagingTemplate, times(1)).convertAndSend(
                eq("/topic/notifications"),
                (Object) any()
        );
    }

    @Test
    void emitGlobalNotification_ShouldHandleException() {
        // Given
        doThrow(new RuntimeException("WebSocket error")).when(messagingTemplate).convertAndSend(anyString(), (Object) any());

        // When & Then
        assertDoesNotThrow(() -> webSocketEmitterAdapter.emitGlobalNotification(notification));

        verify(messagingTemplate, times(1)).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void emitBatchNotifications_ShouldSendMultipleNotifications() {
        // Given
        Notification notification2 = Notification.builder()
                .id(new NotificationId("notif456"))
                .userId("user456")
                .userEmail("test2@example.com")
                .title("Test Notification 2")
                .message("This is another test message")
                .type(NotificationType.PAYMENT_COMPLETED)
                .status(NotificationStatus.SENT)
                .channels(new ArrayList<>(Arrays.asList(Channel.WEB_SOCKET)))
                .deliveryAttempts(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .metadata(null)
                .build();

        List<Notification> notifications = Arrays.asList(notification, notification2);
        doNothing().when(messagingTemplate).convertAndSend(anyString(), (Object) any());

        // When
        webSocketEmitterAdapter.emitBatchNotifications(notifications);

        // Then
        verify(messagingTemplate, times(2)).convertAndSend(anyString(), (Object) any());
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/user123"), (Object) any());
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/notifications/user456"), (Object) any());
    }

    @Test
    void emitBatchNotifications_ShouldHandleEmptyList() {
        // Given
        List<Notification> notifications = new ArrayList<>();

        // When
        webSocketEmitterAdapter.emitBatchNotifications(notifications);

        // Then
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    void emitBatchNotifications_ShouldHandleExceptionAndContinue() {
        // Given
        Notification notification2 = Notification.builder()
                .id(new NotificationId("notif456"))
                .userId("user456")
                .userEmail("test2@example.com")
                .title("Test Notification 2")
                .message("Message 2")
                .type(NotificationType.PAYMENT_COMPLETED)
                .status(NotificationStatus.SENT)
                .channels(new ArrayList<>())
                .deliveryAttempts(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .metadata(null)
                .build();

        List<Notification> notifications = Arrays.asList(notification, notification2);

        doThrow(new RuntimeException("WebSocket error")).when(messagingTemplate).convertAndSend(anyString(), (Object) any());

        // When & Then
        assertDoesNotThrow(() -> webSocketEmitterAdapter.emitBatchNotifications(notifications));
    }

    @Test
    void isUserConnected_ShouldReturnFalse() {
        // When
        boolean result = webSocketEmitterAdapter.isUserConnected("user123");

        // Then
        assertFalse(result);
    }

    @Test
    void getConnectedUsersCount_ShouldReturnZero() {
        // When
        int result = webSocketEmitterAdapter.getConnectedUsersCount();

        // Then
        assertEquals(0, result);
    }

    @Test
    void emitUserNotification_ShouldCreateCorrectWebSocketMessage() {
        // Given
        String userId = "user123";
        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        doNothing().when(messagingTemplate).convertAndSend(anyString(), messageCaptor.capture());

        // When
        webSocketEmitterAdapter.emitUserNotification(userId, notification);

        // Then
        Object capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), (Object) any());
    }
}

