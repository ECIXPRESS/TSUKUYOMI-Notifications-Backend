package edu.dosw.application.services;

import edu.dosw.application.ports.EventServicePort;
import edu.dosw.domain.ports.NotificationRepositoryPort;
import edu.dosw.domain.ports.EmailServicePort;
import edu.dosw.domain.ports.WebSocketEmitterPort;
import edu.dosw.application.dto.command.NotificationCommand;
import edu.dosw.application.dto.command.PasswordResetNotificationCommand;
import edu.dosw.domain.model.Notification;
import edu.dosw.domain.model.ValueObject.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationApplicationService implements EventServicePort {

    private final NotificationRepositoryPort notificationRepositoryPort;
    private final EmailServicePort emailServicePort;
    private final WebSocketEmitterPort webSocketEmitterPort;

    @Override
    @Transactional
    public void processSuccessfulLogin(NotificationCommand command) {
        try {
            log.info("Processing login for user: {}, email: {}", command.getUserId(), command.getEmail());

            Notification notification = createLoginNotification(command);
            Notification savedNotification = notificationRepositoryPort.save(notification);

            boolean emailSuccessful = emailServicePort.sendNotificationEmail(savedNotification);
            savedNotification.addDeliveryAttempt(Channel.EMAIL, emailSuccessful,
                    emailSuccessful ? null : "Error sending email");

            notificationRepositoryPort.save(savedNotification);
            webSocketEmitterPort.emitUserNotification(command.getUserId(), savedNotification);

            log.info("Login notification processed successfully: {}", savedNotification.getId().getValue());

        } catch (Exception e) {
            log.error("Error processing login notification: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing login notification", e);
        }
    }

    @Override
    @Transactional
    public void processNewOrder(NotificationCommand command) {
        try {
            log.info("Processing new order notification: {}", command.getOrderId());

            Notification notification = createNewOrderNotification(command);
            Notification savedNotification = notificationRepositoryPort.save(notification);

            webSocketEmitterPort.emitUserNotification(command.getUserId(), savedNotification);

            log.info("New order notification processed successfully: {}", command.getOrderId());

        } catch (Exception e) {
            log.error("Error processing new order notification: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing new order notification", e);
        }
    }

    @Override
    @Transactional
    public void processOrderStatusChange(NotificationCommand command) {
        try {
            log.info("Processing order status change: {}", command.getOrderStatus());

            Notification notification = createOrderStatusNotification(command);
            Notification savedNotification = notificationRepositoryPort.save(notification);

            boolean emailSuccessful = emailServicePort.sendNotificationEmail(savedNotification);
            savedNotification.addDeliveryAttempt(Channel.EMAIL, emailSuccessful,
                    emailSuccessful ? null : "Error sending email");

            notificationRepositoryPort.save(savedNotification);
            webSocketEmitterPort.emitUserNotification(command.getUserId(), savedNotification);

            log.info("Order status notification processed successfully: {}", command.getOrderId());

        } catch (Exception e) {
            log.error("Error processing order status notification: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing order status notification", e);
        }
    }

    @Override
    @Transactional
    public void processPasswordResetRequest(NotificationCommand command) {
        throw new UnsupportedOperationException("Use processPasswordResetRequest(PasswordResetNotificationCommand) instead");
    }

    @Transactional
    public void processPasswordResetRequest(PasswordResetNotificationCommand command) {
        try {
            log.info("Processing password reset request for: {}", command.getEmail());

            Notification notification = createPasswordResetNotification(command);
            Notification savedNotification = notificationRepositoryPort.save(notification);

            boolean emailSuccessful = emailServicePort.sendHtmlEmail(
                    command.getEmail(),
                    "Código de Verificación - Recuperación de Contraseña",
                    buildPasswordResetEmailHtml(command.getName(), command.getVerificationCode())
            );

            savedNotification.addDeliveryAttempt(Channel.EMAIL, emailSuccessful,
                    emailSuccessful ? null : "Error sending email");

            notificationRepositoryPort.save(savedNotification);
            webSocketEmitterPort.emitUserNotification(command.getUserId(), savedNotification);

            log.info("Password reset notification processed successfully for: {}", command.getEmail());

        } catch (Exception e) {
            log.error("Error processing password reset notification: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing password reset notification", e);
        }
    }

    @Override
    @Transactional
    public void processPasswordResetVerified(NotificationCommand command) {
        throw new UnsupportedOperationException("Use processPasswordResetVerified(PasswordResetNotificationCommand) instead");
    }

    @Transactional
    public void processPasswordResetVerified(PasswordResetNotificationCommand command) {
        try {
            log.info("Processing password reset verification for: {}", command.getEmail());

            Notification notification = createPasswordResetVerifiedNotification(command);
            Notification savedNotification = notificationRepositoryPort.save(notification);

            webSocketEmitterPort.emitUserNotification(command.getUserId(), savedNotification);

            log.info("Password reset verification processed successfully for: {}", command.getEmail());

        } catch (Exception e) {
            log.error("Error processing password reset verification: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing password reset verification", e);
        }
    }

    @Override
    @Transactional
    public void processPasswordResetCompleted(NotificationCommand command) {
        throw new UnsupportedOperationException("Use processPasswordResetCompleted(PasswordResetNotificationCommand) instead");
    }

    @Transactional
    public void processPasswordResetCompleted(PasswordResetNotificationCommand command) {
        try {
            log.info("Processing password reset completion for: {}", command.getEmail());

            Notification notification = createPasswordResetCompletedNotification(command);
            Notification savedNotification = notificationRepositoryPort.save(notification);

            boolean emailSuccessful = emailServicePort.sendHtmlEmail(
                    command.getEmail(),
                    "Contraseña Actualizada Exitosamente",
                    buildPasswordResetCompletedEmailHtml(command.getName())
            );

            savedNotification.addDeliveryAttempt(Channel.EMAIL, emailSuccessful,
                    emailSuccessful ? null : "Error sending email");

            notificationRepositoryPort.save(savedNotification);
            webSocketEmitterPort.emitUserNotification(command.getUserId(), savedNotification);

            log.info("Password reset completion processed successfully for: {}", command.getEmail());

        } catch (Exception e) {
            log.error("Error processing password reset completion: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing password reset completion", e);
        }
    }

    private Notification createPasswordResetNotification(PasswordResetNotificationCommand command) {
        return Notification.builder()
                .id(new NotificationId(UUID.randomUUID().toString()))
                .userId(command.getUserId())
                .userEmail(command.getEmail())
                .title("Código de Verificación - Recuperación de Contraseña")
                .message("Se ha solicitado un código de verificación para recuperar tu contraseña. Código: " + command.getVerificationCode())
                .type(NotificationType.SECURITY_PASSWORD_RESET)
                .status(NotificationStatus.PENDING)
                .channels(java.util.List.of(Channel.EMAIL))
                .deliveryAttempts(new java.util.ArrayList<>())
                .createdAt(java.time.LocalDateTime.now())
                .metadata("{\"verificationCode\":\"" + command.getVerificationCode() + "\",\"action\":\"password_reset_request\"}")
                .build();
    }

    private Notification createPasswordResetVerifiedNotification(PasswordResetNotificationCommand command) {
        return Notification.builder()
                .id(new NotificationId(UUID.randomUUID().toString()))
                .userId(command.getUserId())
                .userEmail(command.getEmail())
                .title("Código Verificado Exitosamente")
                .message("Tu código de verificación ha sido validado correctamente")
                .type(NotificationType.SECURITY_PASSWORD_RESET)
                .status(NotificationStatus.PENDING)
                .channels(java.util.List.of(Channel.WEB_SOCKET))
                .deliveryAttempts(new java.util.ArrayList<>())
                .createdAt(java.time.LocalDateTime.now())
                .metadata("{\"action\":\"password_reset_verified\"}")
                .build();
    }

    private Notification createPasswordResetCompletedNotification(PasswordResetNotificationCommand command) {
        return Notification.builder()
                .id(new NotificationId(UUID.randomUUID().toString()))
                .userId(command.getUserId())
                .userEmail(command.getEmail())
                .title("Contraseña Actualizada Exitosamente")
                .message("Tu contraseña ha sido cambiada correctamente")
                .type(NotificationType.SECURITY_PASSWORD_RESET)
                .status(NotificationStatus.PENDING)
                .channels(java.util.List.of(Channel.EMAIL, Channel.WEB_SOCKET))
                .deliveryAttempts(new java.util.ArrayList<>())
                .createdAt(java.time.LocalDateTime.now())
                .metadata("{\"action\":\"password_reset_completed\"}")
                .build();
    }

    private String buildPasswordResetEmailHtml(String name, String verificationCode) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }\n" +
                "        .container { background-color: white; padding: 30px; border-radius: 10px; max-width: 600px; margin: 0 auto; }\n" +
                "        .code { font-size: 32px; font-weight: bold; color: #2563eb; text-align: center; margin: 20px 0; }\n" +
                "        .footer { margin-top: 30px; font-size: 12px; color: #666; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h2>Hola " + name + ",</h2>\n" +
                "        <p>Has solicitado recuperar tu contraseña. Usa el siguiente código de verificación:</p>\n" +
                "        <div class=\"code\">" + verificationCode + "</div>\n" +
                "        <p>Este código expirará en 15 minutos.</p>\n" +
                "        <p>Si no solicitaste este cambio, por favor ignora este mensaje.</p>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>Saludos,<br>El equipo de ECI Express</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String buildPasswordResetCompletedEmailHtml(String name) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }\n" +
                "        .container { background-color: white; padding: 30px; border-radius: 10px; max-width: 600px; margin: 0 auto; }\n" +
                "        .success { color: #059669; font-weight: bold; }\n" +
                "        .footer { margin-top: 30px; font-size: 12px; color: #666; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h2>Hola " + name + ",</h2>\n" +
                "        <p class=\"success\">Tu contraseña ha sido actualizada exitosamente.</p>\n" +
                "        <p>Si realizaste este cambio, no necesitas hacer nada más.</p>\n" +
                "        <p>Si no reconoces esta actividad, por favor contacta a soporte inmediatamente.</p>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>Saludos,<br>El equipo de ECI Express</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
    private Notification createLoginNotification(NotificationCommand command) {
        return Notification.builder()
                .id(new NotificationId(UUID.randomUUID().toString()))
                .userId(command.getUserId())
                .userEmail(command.getEmail())
                .title("Login detected")
                .message("Your account was accessed from IP: " + command.getIp())
                .type(NotificationType.SECURITY_LOGIN)
                .status(NotificationStatus.PENDING)
                .channels(java.util.List.of(Channel.EMAIL, Channel.WEB_SOCKET))
                .deliveryAttempts(new java.util.ArrayList<>())
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }

    private Notification createNewOrderNotification(NotificationCommand command) {
        return Notification.builder()
                .id(new NotificationId(UUID.randomUUID().toString()))
                .userId(command.getUserId())
                .userEmail(command.getEmail())
                .title("New Order Received")
                .message("You have a new order #" + command.getOrderId() + " to prepare")
                .type(NotificationType.SELLER_NEW_ORDER)
                .status(NotificationStatus.PENDING)
                .channels(java.util.List.of(Channel.WEB_SOCKET, Channel.EMAIL))
                .deliveryAttempts(new java.util.ArrayList<>())
                .createdAt(java.time.LocalDateTime.now())
                .metadata("{\"orderId\":\"" + command.getOrderId() + "\",\"pointOfSaleId\":\"" + command.getPointOfSaleId() + "\"}")
                .build();
    }

    private Notification createOrderStatusNotification(NotificationCommand command) {
        String statusMessage = getStatusMessage(command.getOrderStatus());
        return Notification.builder()
                .id(new NotificationId(UUID.randomUUID().toString()))
                .userId(command.getUserId())
                .userEmail(command.getEmail())
                .title("Order Status Update")
                .message("Order #" + command.getOrderId() + " is now " + statusMessage)
                .type(NotificationType.ORDER_CONFIRMED)
                .status(NotificationStatus.PENDING)
                .channels(java.util.List.of(Channel.EMAIL, Channel.WEB_SOCKET))
                .deliveryAttempts(new java.util.ArrayList<>())
                .createdAt(java.time.LocalDateTime.now())
                .metadata("{\"orderId\":\"" + command.getOrderId() + "\",\"status\":\"" + command.getOrderStatus() + "\"}")
                .build();
    }

    private String getStatusMessage(String status) {
        return switch (status.toLowerCase()) {
            case "confirmed" -> "confirmed and in preparation";
            case "preparation" -> "being prepared";
            case "ready" -> "ready for pickup";
            case "delivered" -> "delivered";
            case "refunded" -> "refunded";
            default -> status;
        };
    }
}