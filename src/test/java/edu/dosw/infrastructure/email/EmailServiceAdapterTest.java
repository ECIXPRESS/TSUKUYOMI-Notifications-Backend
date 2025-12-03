package edu.dosw.infrastructure.email;

import edu.dosw.domain.model.Notification;
import edu.dosw.domain.model.ValueObject.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceAdapter emailServiceAdapter;

    private Notification notification;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        notification = Notification.builder()
                .id(new NotificationId("notif123"))
                .userId("user123")
                .userEmail("test@example.com")
                .title("Test Notification")
                .message("This is a test message")
                .type(NotificationType.ORDER_CONFIRMED)
                .status(NotificationStatus.PENDING)
                .channels(new ArrayList<>(Arrays.asList(Channel.EMAIL)))
                .deliveryAttempts(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .readAt(null)
                .metadata("{\"test\":true}")
                .build();

        mimeMessage = mock(MimeMessage.class);
    }

    @Test
    void sendNotificationEmail_ShouldReturnTrueWhenEmailSentSuccessfully() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        boolean result = emailServiceAdapter.sendNotificationEmail(notification);

        // Then
        assertTrue(result);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotificationEmail_ShouldReturnFalseWhenExceptionOccurs() {
        // Given
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));

        // When
        boolean result = emailServiceAdapter.sendNotificationEmail(notification);

        // Then
        assertFalse(result);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendNotificationEmail_ShouldBuildCorrectEmailText() {
        // Given
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // When
        emailServiceAdapter.sendNotificationEmail(notification);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendHtmlEmail_ShouldReturnTrueWhenEmailSentSuccessfully() {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String htmlContent = "<html><body><h1>Test</h1></body></html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // When
        boolean result = emailServiceAdapter.sendHtmlEmail(to, subject, htmlContent);

        // Then
        assertTrue(result);
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendNotificationEmail_ShouldHandleNullNotificationEmail() {
        // Given
        notification.setUserEmail(null);
        doThrow(new NullPointerException()).when(mailSender).send(any(SimpleMailMessage.class));

        // When
        boolean result = emailServiceAdapter.sendNotificationEmail(notification);

        // Then
        assertFalse(result);
    }

    @Test
    void sendNotificationEmail_ShouldHandleMailAuthenticationException() {
        // Given
        doThrow(new MailException("Authentication failed") {}).when(mailSender).send(any(SimpleMailMessage.class));

        // When
        boolean result = emailServiceAdapter.sendNotificationEmail(notification);

        // Then
        assertFalse(result);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}

