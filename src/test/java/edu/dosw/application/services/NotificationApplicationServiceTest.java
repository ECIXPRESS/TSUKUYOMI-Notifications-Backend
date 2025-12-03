package edu.dosw.application.services;

import edu.dosw.application.dto.command.LoginEventCommand;
import edu.dosw.application.dto.command.NotificationCommand;
import edu.dosw.application.dto.command.PasswordResetNotificationCommand;
import edu.dosw.application.dto.command.PaymentCommand;
import edu.dosw.domain.model.Notification;
import edu.dosw.domain.model.ValueObject.Channel;
import edu.dosw.domain.model.ValueObject.NotificationStatus;
import edu.dosw.domain.ports.EmailServicePort;
import edu.dosw.domain.ports.NotificationRepositoryPort;
import edu.dosw.domain.ports.WebSocketEmitterPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationApplicationServiceTest {

    @Mock
    private NotificationRepositoryPort notificationRepositoryPort;

    @Mock
    private EmailServicePort emailServicePort;

    @Mock
    private WebSocketEmitterPort webSocketEmitterPort;

    @InjectMocks
    private NotificationApplicationService notificationApplicationService;

    @BeforeEach
    void setUp() {
        // This method intentionally left empty
    }

    @Test
    void processSuccessfulLogin_ShouldProcessSuccessfully() {
        // Given
        LoginEventCommand command = new LoginEventCommand();
        command.setUserId("user123");
        command.setEmail("test@example.com");
        command.setName("Test User");
        command.setIp("192.168.1.1");

        when(notificationRepositoryPort.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailServicePort.sendHtmlEmail(anyString(), anyString(), anyString())).thenReturn(true);
        doNothing().when(webSocketEmitterPort).emitUserNotification(anyString(), any(Notification.class));

        // When
        notificationApplicationService.processSuccessfulLogin(command);

        // Then
        verify(notificationRepositoryPort, times(2)).save(any(Notification.class));
        verify(emailServicePort, times(1)).sendHtmlEmail(
                eq("test@example.com"),
                eq(" Nueva Actividad de Inicio de Sesión - ECI Express"),
                anyString()
        );
        verify(webSocketEmitterPort, times(1)).emitUserNotification(eq("user123"), any(Notification.class));
    }

    @Test
    void processSuccessfulLogin_ShouldHandleEmailFailure() {
        // Given
        LoginEventCommand command = new LoginEventCommand();
        command.setUserId("user123");
        command.setEmail("test@example.com");
        command.setName("Test User");
        command.setIp("192.168.1.1");

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepositoryPort.save(notificationCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailServicePort.sendHtmlEmail(anyString(), anyString(), anyString())).thenReturn(false);

        // When
        notificationApplicationService.processSuccessfulLogin(command);

        // Then
        Notification savedNotification = notificationCaptor.getAllValues().get(1);
        assertEquals(NotificationStatus.FAILED, savedNotification.getStatus());
        assertEquals(1, savedNotification.getDeliveryAttempts().size());
        assertFalse(savedNotification.getDeliveryAttempts().get(0).isSuccessful());
    }

    @Test
    void processSuccessfulLogin_ShouldThrowRuntimeExceptionOnError() {
        // Given
        LoginEventCommand command = new LoginEventCommand();
        command.setUserId("user123");
        command.setEmail("test@example.com");
        command.setName("Test User");
        command.setIp("192.168.1.1");

        when(notificationRepositoryPort.save(any(Notification.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            notificationApplicationService.processSuccessfulLogin(command);
        });
    }

    @Test
    void processNewOrder_ShouldProcessSuccessfully() {
        // Given
        NotificationCommand command = new NotificationCommand();
        command.setUserId("seller123");
        command.setEmail("seller@example.com");
        command.setOrderId("order456");
        command.setPointOfSaleId("pos789");

        when(notificationRepositoryPort.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(webSocketEmitterPort).emitUserNotification(anyString(), any(Notification.class));

        // When
        notificationApplicationService.processNewOrder(command);

        // Then
        verify(notificationRepositoryPort, times(1)).save(any(Notification.class));
        verify(webSocketEmitterPort, times(1)).emitUserNotification(eq("seller123"), any(Notification.class));
        verifyNoInteractions(emailServicePort);
    }

    @Test
    void processNewOrder_ShouldThrowRuntimeExceptionOnError() {
        // Given
        NotificationCommand command = new NotificationCommand();
        command.setUserId("seller123");
        command.setEmail("seller@example.com");
        command.setOrderId("order456");

        when(notificationRepositoryPort.save(any(Notification.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            notificationApplicationService.processNewOrder(command);
        });
    }

    @Test
    void processOrderStatusChange_ShouldProcessSuccessfully() {
        // Given
        NotificationCommand command = new NotificationCommand();
        command.setUserId("user123");
        command.setEmail("user@example.com");
        command.setOrderId("order456");
        command.setOrderStatus("confirmed");

        when(notificationRepositoryPort.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailServicePort.sendNotificationEmail(any(Notification.class))).thenReturn(true);

        // When
        notificationApplicationService.processOrderStatusChange(command);

        // Then
        verify(notificationRepositoryPort, times(2)).save(any(Notification.class));
        verify(emailServicePort, times(1)).sendNotificationEmail(any(Notification.class));
        verify(webSocketEmitterPort, times(1)).emitUserNotification(eq("user123"), any(Notification.class));
    }

    @Test
    void processOrderStatusChange_ShouldHandleEmailFailure() {
        // Given
        NotificationCommand command = new NotificationCommand();
        command.setUserId("user123");
        command.setEmail("user@example.com");
        command.setOrderId("order456");
        command.setOrderStatus("ready");

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepositoryPort.save(notificationCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailServicePort.sendNotificationEmail(any(Notification.class))).thenReturn(false);

        // When
        notificationApplicationService.processOrderStatusChange(command);

        // Then
        Notification savedNotification = notificationCaptor.getAllValues().get(1);
        assertEquals(NotificationStatus.FAILED, savedNotification.getStatus());
        assertFalse(savedNotification.getDeliveryAttempts().get(0).isSuccessful());
    }

    @Test
    void processPasswordResetRequest_ShouldProcessSuccessfully() {
        // Given
        PasswordResetNotificationCommand command = new PasswordResetNotificationCommand();
        command.setUserId("user123");
        command.setEmail("user@example.com");
        command.setName("Test User");
        command.setVerificationCode("123456");

        when(notificationRepositoryPort.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailServicePort.sendHtmlEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // When
        notificationApplicationService.processPasswordResetRequest(command);

        // Then
        verify(notificationRepositoryPort, times(2)).save(any(Notification.class));
        verify(emailServicePort, times(1)).sendHtmlEmail(
                eq("user@example.com"),
                eq("Código de Verificación - Recuperación de Contraseña"),
                anyString()
        );
        verify(webSocketEmitterPort, times(1)).emitUserNotification(eq("user123"), any(Notification.class));
    }

    @Test
    void processPasswordResetVerified_ShouldProcessSuccessfully() {
        // Given
        PasswordResetNotificationCommand command = new PasswordResetNotificationCommand();
        command.setUserId("user123");
        command.setEmail("user@example.com");
        command.setName("Test User");

        when(notificationRepositoryPort.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationApplicationService.processPasswordResetVerified(command);

        // Then
        verify(notificationRepositoryPort, times(1)).save(any(Notification.class));
        verify(webSocketEmitterPort, times(1)).emitUserNotification(eq("user123"), any(Notification.class));
        verifyNoInteractions(emailServicePort);
    }

    @Test
    void processPasswordResetCompleted_ShouldProcessSuccessfully() {
        // Given
        PasswordResetNotificationCommand command = new PasswordResetNotificationCommand();
        command.setUserId("user123");
        command.setEmail("user@example.com");
        command.setName("Test User");

        when(notificationRepositoryPort.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailServicePort.sendHtmlEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // When
        notificationApplicationService.processPasswordResetCompleted(command);

        // Then
        verify(notificationRepositoryPort, times(2)).save(any(Notification.class));
        verify(emailServicePort, times(1)).sendHtmlEmail(
                eq("user@example.com"),
                eq("Contraseña Actualizada Exitosamente"),
                anyString()
        );
        verify(webSocketEmitterPort, times(1)).emitUserNotification(eq("user123"), any(Notification.class));
    }

    @Test
    void processPaymentCompleted_ShouldProcessSuccessfully() {
        // Given
        PaymentCommand command = new PaymentCommand();
        command.setUserId("user123");
        command.setEmail("user@example.com");
        command.setName("Test User");
        command.setOrderId("order456");
        command.setAmount(50000.0);
        command.setPaymentMethod("Credit Card");
        command.setCurrency("COP");

        when(notificationRepositoryPort.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailServicePort.sendHtmlEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // When
        notificationApplicationService.processPaymentCompleted(command);

        // Then
        verify(notificationRepositoryPort, times(2)).save(any(Notification.class));
        verify(emailServicePort, times(1)).sendHtmlEmail(
                eq("user@example.com"),
                eq("Pago Completado Exitosamente - Orden #order456"),
                anyString()
        );
        verify(webSocketEmitterPort, times(1)).emitUserNotification(eq("user123"), any(Notification.class));
    }

    @Test
    void processPaymentFailed_ShouldProcessSuccessfully() {
        // Given
        PaymentCommand command = new PaymentCommand();
        command.setUserId("user123");
        command.setEmail("user@example.com");
        command.setName("Test User");
        command.setOrderId("order456");
        command.setPaymentMethod("Credit Card");

        when(notificationRepositoryPort.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailServicePort.sendHtmlEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // When
        notificationApplicationService.processPaymentFailed(command);

        // Then
        verify(notificationRepositoryPort, times(2)).save(any(Notification.class));
        verify(emailServicePort, times(1)).sendHtmlEmail(
                eq("user@example.com"),
                eq("Problema con tu Pago - Orden #order456"),
                anyString()
        );
        verify(webSocketEmitterPort, times(1)).emitUserNotification(eq("user123"), any(Notification.class));
    }

    @Test
    void processNewOrder_ShouldCreateNotificationWithCorrectMetadata() {
        // Given
        NotificationCommand command = new NotificationCommand();
        command.setUserId("seller123");
        command.setEmail("seller@example.com");
        command.setOrderId("order789");
        command.setPointOfSaleId("pos123");

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepositoryPort.save(notificationCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationApplicationService.processNewOrder(command);

        // Then
        Notification capturedNotification = notificationCaptor.getValue();
        assertNotNull(capturedNotification);
        assertEquals("seller123", capturedNotification.getUserId());
        assertEquals("seller@example.com", capturedNotification.getUserEmail());
        assertTrue(capturedNotification.getTitle().contains("New Order"));
        assertTrue(capturedNotification.getMessage().contains("order789"));
        assertNotNull(capturedNotification.getMetadata());
        assertTrue(capturedNotification.getMetadata().contains("order789"));
        assertTrue(capturedNotification.getMetadata().contains("pos123"));
    }

    @Test
    void processOrderStatusChange_ShouldCreateNotificationWithCorrectStatus() {
        // Given
        NotificationCommand command = new NotificationCommand();
        command.setUserId("user123");
        command.setEmail("user@example.com");
        command.setOrderId("order456");
        command.setOrderStatus("delivered");

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepositoryPort.save(notificationCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        when(emailServicePort.sendNotificationEmail(any(Notification.class))).thenReturn(true);

        // When
        notificationApplicationService.processOrderStatusChange(command);

        // Then
        Notification capturedNotification = notificationCaptor.getAllValues().get(0);
        assertNotNull(capturedNotification);
        assertTrue(capturedNotification.getMessage().contains("delivered"));
        assertNotNull(capturedNotification.getMetadata());
        assertTrue(capturedNotification.getMetadata().contains("delivered"));
    }
}

