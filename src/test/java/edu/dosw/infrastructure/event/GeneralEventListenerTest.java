package edu.dosw.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.application.dto.command.LoginEventCommand;
import edu.dosw.application.dto.command.NotificationCommand;
import edu.dosw.application.dto.command.PasswordResetNotificationCommand;
import edu.dosw.application.dto.command.PaymentCommand;
import edu.dosw.application.ports.EventServicePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.Message;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeneralEventListenerTest {

    @Mock
    private EventServicePort eventServicePort;

    @Mock
    private Message message;

    @InjectMocks
    private GeneralEventListener generalEventListener;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        generalEventListener = new GeneralEventListener(eventServicePort, objectMapper);
    }

    @Test
    void onMessage_LoginSuccess_ShouldProcessSuccessfulLogin() throws Exception {
        // Given
        byte[] channel = "events:general".getBytes();
        GeneralEventListener.EventWrapper eventWrapper = new GeneralEventListener.EventWrapper();
        eventWrapper.setEventId("evt-123");
        eventWrapper.setEventType("login.success");

        GeneralEventListener.LoginEventData loginData = new GeneralEventListener.LoginEventData();
        loginData.setUserId("user123");
        loginData.setEmail("test@example.com");
        loginData.setName("Test User");
        loginData.setIp("192.168.1.1");
        eventWrapper.setData(loginData);

        String body = objectMapper.writeValueAsString(eventWrapper);
        when(message.getBody()).thenReturn(body.getBytes());

        doNothing().when(eventServicePort).processSuccessfulLogin(any(LoginEventCommand.class));

        // When
        generalEventListener.onMessage(message, channel);

        // Then
        verify(eventServicePort, times(1)).processSuccessfulLogin(any(LoginEventCommand.class));
    }

    @Test
    void onMessage_NewOrder_ShouldProcessNewOrder() throws Exception {
        // Given
        byte[] channel = "events:general".getBytes();
        GeneralEventListener.EventWrapper eventWrapper = new GeneralEventListener.EventWrapper();
        eventWrapper.setEventType("order.new");

        GeneralEventListener.OrderEventData orderData = new GeneralEventListener.OrderEventData();
        orderData.setOrderId("order123");
        orderData.setUserId("seller456");
        eventWrapper.setData(orderData);

        String body = objectMapper.writeValueAsString(eventWrapper);
        when(message.getBody()).thenReturn(body.getBytes());

        // When
        generalEventListener.onMessage(message, channel);

        // Then
        verify(eventServicePort, times(1)).processNewOrder(any(NotificationCommand.class));
    }

    @Test
    void onMessage_OrderStatusChange_ShouldProcessOrderStatusChange() throws Exception {
        // Given
        byte[] channel = "events:general".getBytes();
        GeneralEventListener.EventWrapper eventWrapper = new GeneralEventListener.EventWrapper();
        eventWrapper.setEventType("order.status.change");

        GeneralEventListener.OrderStatusEventData statusData = new GeneralEventListener.OrderStatusEventData();
        statusData.setOrderId("order123");
        statusData.setUserId("user123");
        statusData.setNewStatus("CONFIRMED");
        eventWrapper.setData(statusData);

        String body = objectMapper.writeValueAsString(eventWrapper);
        when(message.getBody()).thenReturn(body.getBytes());

        // When
        generalEventListener.onMessage(message, channel);

        // Then
        verify(eventServicePort, times(1)).processOrderStatusChange(any(NotificationCommand.class));
    }

    @Test
    void onMessage_PasswordResetRequested_ShouldProcess() throws Exception {
        // Given
        byte[] channel = "events:general".getBytes();
        GeneralEventListener.EventWrapper eventWrapper = new GeneralEventListener.EventWrapper();
        eventWrapper.setEventType("password.reset.requested");

        GeneralEventListener.PasswordResetRequestEventData pwdData = new GeneralEventListener.PasswordResetRequestEventData();
        pwdData.setUserId("user123");
        pwdData.setEmail("user@example.com");
        pwdData.setVerificationCode("123456");
        eventWrapper.setData(pwdData);

        String body = objectMapper.writeValueAsString(eventWrapper);
        when(message.getBody()).thenReturn(body.getBytes());

        // When
        generalEventListener.onMessage(message, channel);

        // Then
        verify(eventServicePort, times(1)).processPasswordResetRequest(any(PasswordResetNotificationCommand.class));
    }

    @Test
    void onMessage_PasswordResetCompleted_ShouldProcess() throws Exception {
        // Given
        byte[] channel = "events:general".getBytes();
        GeneralEventListener.EventWrapper eventWrapper = new GeneralEventListener.EventWrapper();
        eventWrapper.setEventType("password.reset.completed");

        GeneralEventListener.PasswordResetCompletedEventData compData = new GeneralEventListener.PasswordResetCompletedEventData();
        compData.setUserId("user123");
        compData.setEmail("user@example.com");
        eventWrapper.setData(compData);

        String body = objectMapper.writeValueAsString(eventWrapper);
        when(message.getBody()).thenReturn(body.getBytes());

        // When
        generalEventListener.onMessage(message, channel);

        // Then
        verify(eventServicePort, times(1)).processPasswordResetCompleted(any(PasswordResetNotificationCommand.class));
    }

    @Test
    void onMessage_PaymentCompleted_ShouldProcess() throws Exception {
        // Given
        byte[] channel = "events:general".getBytes();
        GeneralEventListener.EventWrapper eventWrapper = new GeneralEventListener.EventWrapper();
        eventWrapper.setEventType("payment.completed");

        GeneralEventListener.PaymentEventData payData = new GeneralEventListener.PaymentEventData();
        payData.setOrderId("order123");
        payData.setClientId("client123");
        payData.setCustomerEmail("customer@example.com");
        payData.setFinalAmount(150.0);
        eventWrapper.setData(payData);

        String body = objectMapper.writeValueAsString(eventWrapper);
        when(message.getBody()).thenReturn(body.getBytes());

        // When
        generalEventListener.onMessage(message, channel);

        // Then
        verify(eventServicePort, times(1)).processPaymentCompleted(any(PaymentCommand.class));
    }

    @Test
    void onMessage_PaymentFailed_ShouldProcess() throws Exception {
        // Given
        byte[] channel = "events:general".getBytes();
        GeneralEventListener.EventWrapper eventWrapper = new GeneralEventListener.EventWrapper();
        eventWrapper.setEventType("payment.failed");

        GeneralEventListener.PaymentEventData payData = new GeneralEventListener.PaymentEventData();
        payData.setOrderId("order123");
        payData.setClientId("client123");
        payData.setCustomerEmail("customer@example.com");
        eventWrapper.setData(payData);

        String body = objectMapper.writeValueAsString(eventWrapper);
        when(message.getBody()).thenReturn(body.getBytes());

        // When
        generalEventListener.onMessage(message, channel);

        // Then
        verify(eventServicePort, times(1)).processPaymentFailed(any(PaymentCommand.class));
    }

    @Test
    void onMessage_UnknownEventType_ShouldNotFail() throws Exception {
        // Given
        byte[] channel = "events:general".getBytes();
        GeneralEventListener.EventWrapper eventWrapper = new GeneralEventListener.EventWrapper();
        eventWrapper.setEventType("unknown.event");
        eventWrapper.setData("data");

        String body = objectMapper.writeValueAsString(eventWrapper);
        when(message.getBody()).thenReturn(body.getBytes());

        // When & Then
        assertDoesNotThrow(() -> generalEventListener.onMessage(message, channel));
        verifyNoInteractions(eventServicePort);
    }

    @Test
    void onMessage_InvalidJSON_ShouldNotFail() {
        // Given
        byte[] channel = "events:general".getBytes();
        when(message.getBody()).thenReturn("invalid json".getBytes());

        // When & Then
        assertDoesNotThrow(() -> generalEventListener.onMessage(message, channel));
    }
}
