package edu.dosw.infrastructure.web.controllers;

import edu.dosw.application.dto.command.LoginEventCommand;
import edu.dosw.application.dto.command.NotificationCommand;
import edu.dosw.application.ports.EventServicePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventServicePort eventServicePort;

    @InjectMocks
    private EventController eventController;

    private LoginEventCommand loginEventCommand;
    private NotificationCommand notificationCommand;

    @BeforeEach
    void setUp() {
        loginEventCommand = new LoginEventCommand();
        loginEventCommand.setUserId("user123");
        loginEventCommand.setEmail("test@example.com");
        loginEventCommand.setName("Test User");
        loginEventCommand.setIp("192.168.1.1");

        notificationCommand = new NotificationCommand();
        notificationCommand.setUserId("user123");
        notificationCommand.setOrderId("order456");
        notificationCommand.setOrderStatus("CONFIRMED");
    }

    @Test
    void receiveSuccessfulLogin_ShouldProcessLoginEventSuccessfully() {
        // Given
        doNothing().when(eventServicePort).processSuccessfulLogin(any(LoginEventCommand.class));

        // When
        ResponseEntity<Map<String, String>> response = eventController.receiveSuccessfulLogin(loginEventCommand);

        // Then
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Login event processed successfully", response.getBody().get("message"));
        verify(eventServicePort, times(1)).processSuccessfulLogin(any(LoginEventCommand.class));
    }

    @Test
    void receiveSuccessfulLogin_ShouldReturnErrorOnException() {
        // Given
        doThrow(new RuntimeException("Service error")).when(eventServicePort).processSuccessfulLogin(any(LoginEventCommand.class));

        // When
        ResponseEntity<Map<String, String>> response = eventController.receiveSuccessfulLogin(loginEventCommand);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Failed to process login event", response.getBody().get("error"));
    }

    @Test
    void receiveNewOrder_ShouldProcessNewOrderEventSuccessfully() {
        // Given
        doNothing().when(eventServicePort).processNewOrder(any(NotificationCommand.class));

        // When
        ResponseEntity<Map<String, String>> response = eventController.receiveNewOrder(notificationCommand);

        // Then
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New order event processed successfully", response.getBody().get("message"));
        verify(eventServicePort, times(1)).processNewOrder(any(NotificationCommand.class));
    }

    @Test
    void receiveNewOrder_ShouldReturnErrorOnException() {
        // Given
        doThrow(new RuntimeException("Service error")).when(eventServicePort).processNewOrder(any(NotificationCommand.class));

        // When
        ResponseEntity<Map<String, String>> response = eventController.receiveNewOrder(notificationCommand);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Failed to process new order event", response.getBody().get("error"));
    }

    @Test
    void receiveOrderStatusChange_ShouldProcessStatusChangeSuccessfully() {
        // Given
        doNothing().when(eventServicePort).processOrderStatusChange(any(NotificationCommand.class));

        // When
        ResponseEntity<Map<String, String>> response = eventController.receiveOrderStatusChange(notificationCommand);

        // Then
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Order status change processed successfully", response.getBody().get("message"));
        verify(eventServicePort, times(1)).processOrderStatusChange(any(NotificationCommand.class));
    }

    @Test
    void receiveOrderStatusChange_ShouldReturnErrorOnException() {
        // Given
        doThrow(new RuntimeException("Service error")).when(eventServicePort).processOrderStatusChange(any(NotificationCommand.class));

        // When
        ResponseEntity<Map<String, String>> response = eventController.receiveOrderStatusChange(notificationCommand);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Failed to process order status change", response.getBody().get("error"));
    }

    @Test
    void receiveSuccessfulLogin_ShouldSetAllCommandFields() {
        // Given
        doNothing().when(eventServicePort).processSuccessfulLogin(any(LoginEventCommand.class));

        // When
        eventController.receiveSuccessfulLogin(loginEventCommand);

        // Then
        verify(eventServicePort).processSuccessfulLogin(argThat(command ->
                "user123".equals(command.getUserId()) &&
                "test@example.com".equals(command.getEmail()) &&
                "Test User".equals(command.getName()) &&
                "192.168.1.1".equals(command.getIp())
        ));
    }

    @Test
    void receiveNewOrder_ShouldProcessWithCorrectOrderId() {
        // Given
        doNothing().when(eventServicePort).processNewOrder(any(NotificationCommand.class));

        // When
        eventController.receiveNewOrder(notificationCommand);

        // Then
        verify(eventServicePort).processNewOrder(argThat(command ->
                "order456".equals(command.getOrderId())
        ));
    }

    @Test
    void receiveOrderStatusChange_ShouldProcessWithCorrectStatus() {
        // Given
        doNothing().when(eventServicePort).processOrderStatusChange(any(NotificationCommand.class));

        // When
        eventController.receiveOrderStatusChange(notificationCommand);

        // Then
        verify(eventServicePort).processOrderStatusChange(argThat(command ->
                "CONFIRMED".equals(command.getOrderStatus())
        ));
    }
}

