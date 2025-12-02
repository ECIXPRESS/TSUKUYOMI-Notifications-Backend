package edu.dosw.application.services;

import edu.dosw.application.ports.EventServicePort;
import edu.dosw.domain.ports.NotificationRepositoryPort;
import edu.dosw.domain.ports.EmailServicePort;
import edu.dosw.domain.ports.WebSocketEmitterPort;
import edu.dosw.application.dto.command.NotificationCommand;
import edu.dosw.application.dto.command.PasswordResetNotificationCommand;
import edu.dosw.application.dto.command.PaymentCommand;
import edu.dosw.application.dto.command.LoginEventCommand;
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
    public void processSuccessfulLogin(LoginEventCommand command) {
        try {
            log.info("Processing login for user: {}, email: {}", command.getUserId(), command.getEmail());

            Notification notification = createLoginNotification(command);
            Notification savedNotification = notificationRepositoryPort.save(notification);

            boolean emailSuccessful = emailServicePort.sendHtmlEmail(
                    command.getEmail(),
                    " Nueva Actividad de Inicio de Sesión - ECI Express",
                    buildLoginEmailHtml(command.getName(), command.getIp())
            );

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

    @Override
    @Transactional
    public void processPaymentCompleted(PaymentCommand command) {
        try {
            log.info("Processing payment completed notification - Order: {}, User: {}, Amount: {}",
                    command.getOrderId(), command.getUserId(), command.getAmount());

            Notification notification = createPaymentCompletedNotification(command);
            Notification savedNotification = notificationRepositoryPort.save(notification);

            boolean emailSuccessful = emailServicePort.sendHtmlEmail(
                    command.getEmail(),
                    "Pago Completado Exitosamente - Orden #" + command.getOrderId(),
                    buildPaymentCompletedEmailHtml(command.getName(), command.getOrderId(), command.getAmount(), command.getPaymentMethod())
            );

            savedNotification.addDeliveryAttempt(Channel.EMAIL, emailSuccessful,
                    emailSuccessful ? null : "Error sending email");

            notificationRepositoryPort.save(savedNotification);
            webSocketEmitterPort.emitUserNotification(command.getUserId(), savedNotification);

            log.info("Payment completed notification processed successfully - Order: {}", command.getOrderId());

        } catch (Exception e) {
            log.error("Error processing payment completed notification: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing payment completed notification", e);
        }
    }

    @Override
    @Transactional
    public void processPaymentFailed(PaymentCommand command) {
        try {
            log.info("Processing payment failed notification - Order: {}, User: {}",
                    command.getOrderId(), command.getUserId());

            Notification notification = createPaymentFailedNotification(command);
            Notification savedNotification = notificationRepositoryPort.save(notification);

            boolean emailSuccessful = emailServicePort.sendHtmlEmail(
                    command.getEmail(),
                    "Problema con tu Pago - Orden #" + command.getOrderId(),
                    buildPaymentFailedEmailHtml(command.getName(), command.getOrderId(), command.getPaymentMethod())
            );

            savedNotification.addDeliveryAttempt(Channel.EMAIL, emailSuccessful,
                    emailSuccessful ? null : "Error sending email");

            notificationRepositoryPort.save(savedNotification);
            webSocketEmitterPort.emitUserNotification(command.getUserId(), savedNotification);

            log.info("Payment failed notification processed successfully - Order: {}", command.getOrderId());

        } catch (Exception e) {
            log.error("Error processing payment failed notification: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing payment failed notification", e);
        }
    }


    private Notification createPaymentCompletedNotification(PaymentCommand command) {
        return Notification.builder()
                .id(new NotificationId(UUID.randomUUID().toString()))
                .userId(command.getUserId())
                .userEmail(command.getEmail())
                .title("Pago Completado")
                .message("Tu pago de $" + command.getAmount() + " para la orden #" + command.getOrderId() + " ha sido completado exitosamente")
                .type(NotificationType.PAYMENT_COMPLETED)
                .status(NotificationStatus.PENDING)
                .channels(java.util.List.of(Channel.EMAIL, Channel.WEB_SOCKET))
                .deliveryAttempts(new java.util.ArrayList<>())
                .createdAt(java.time.LocalDateTime.now())
                .metadata(buildPaymentMetadata(command, "completed"))
                .build();
    }

    private Notification createPaymentFailedNotification(PaymentCommand command) {
        return Notification.builder()
                .id(new NotificationId(UUID.randomUUID().toString()))
                .userId(command.getUserId())
                .userEmail(command.getEmail())
                .title("Pago Fallido")
                .message("Hubo un problema con tu pago para la orden #" + command.getOrderId() + ". Por favor intenta nuevamente")
                .type(NotificationType.PAYMENT_FAILED)
                .status(NotificationStatus.PENDING)
                .channels(java.util.List.of(Channel.EMAIL, Channel.WEB_SOCKET))
                .deliveryAttempts(new java.util.ArrayList<>())
                .createdAt(java.time.LocalDateTime.now())
                .metadata(buildPaymentMetadata(command, "failed"))
                .build();
    }

    private String buildPaymentMetadata(PaymentCommand command, String status) {
        return String.format(
                "{\"orderId\":\"%s\",\"amount\":%.2f,\"paymentMethod\":\"%s\",\"paymentStatus\":\"%s\",\"currency\":\"%s\"}",
                command.getOrderId(),
                command.getAmount(),
                command.getPaymentMethod(),
                status,
                command.getCurrency() != null ? command.getCurrency() : "COP"
        );
    }


    private String buildPaymentCompletedEmailHtml(String name, String orderId, Double amount, String paymentMethod) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }\n" +
                "        .container { background-color: white; padding: 30px; border-radius: 10px; max-width: 600px; margin: 0 auto; }\n" +
                "        .success { color: #059669; font-weight: bold; font-size: 24px; }\n" +
                "        .amount { font-size: 32px; font-weight: bold; color: #2563eb; text-align: center; margin: 20px 0; }\n" +
                "        .details { background-color: #f8fafc; padding: 15px; border-radius: 5px; margin: 20px 0; }\n" +
                "        .footer { margin-top: 30px; font-size: 12px; color: #666; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h2>Hola " + name + ",</h2>\n" +
                "        <p class=\"success\">¡Pago Completado Exitosamente!</p>\n" +
                "        <div class=\"amount\">$" + String.format("%.2f", amount) + " COP</div>\n" +
                "        <div class=\"details\">\n" +
                "            <p><strong>Número de Orden:</strong> #" + orderId + "</p>\n" +
                "            <p><strong>Método de Pago:</strong> " + paymentMethod + "</p>\n" +
                "            <p><strong>Fecha:</strong> " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "</p>\n" +
                "        </div>\n" +
                "        <p>Tu pago ha sido procesado correctamente. Ahora puedes hacer seguimiento a tu orden.</p>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>Saludos,<br>El equipo de ECI Express</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private String buildPaymentFailedEmailHtml(String name, String orderId, String paymentMethod) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }\n" +
                "        .container { background-color: white; padding: 30px; border-radius: 10px; max-width: 600px; margin: 0 auto; }\n" +
                "        .error { color: #dc2626; font-weight: bold; font-size: 24px; }\n" +
                "        .details { background-color: #fef2f2; padding: 15px; border-radius: 5px; margin: 20px 0; }\n" +
                "        .action { background-color: #2563eb; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 10px 0; }\n" +
                "        .footer { margin-top: 30px; font-size: 12px; color: #666; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h2>Hola " + name + ",</h2>\n" +
                "        <p class=\"error\">Problema con tu Pago</p>\n" +
                "        <div class=\"details\">\n" +
                "            <p><strong>Número de Orden:</strong> #" + orderId + "</p>\n" +
                "            <p><strong>Método de Pago:</strong> " + paymentMethod + "</p>\n" +
                "            <p>Lo sentimos, hubo un problema al procesar tu pago. Esto puede deberse a:</p>\n" +
                "            <ul>\n" +
                "                <li>Fondos insuficientes</li>\n" +
                "                <li>Información de la tarjeta incorrecta</li>\n" +
                "                <li>Problemas temporales del sistema</li>\n" +
                "            </ul>\n" +
                "        </div>\n" +
                "        <p>Por favor intenta nuevamente o utiliza otro método de pago.</p>\n" +
                "        <a href=\"#\" class=\"action\">Reintentar Pago</a>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>Si necesitas ayuda, contáctanos a soporte@eciexpress.com</p>\n" +
                "            <p>Saludos,<br>El equipo de ECI Express</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
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

    private Notification createLoginNotification(LoginEventCommand command) {
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

    private String buildPasswordResetEmailHtml(String name, String verificationCode) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }\n" +
                "        .container { background-color: white; padding: 30px; border-radius: 10px; max-width: 600px; margin: 0 auto; }\n" +
                "        .code { font-size: 32px; font-weight: bold; color: #FFB44D; text-align: center; margin: 20px 0; }\n" +
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
                "        .success { color: #FFB44D; font-weight: bold; }\n" +
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

    private String buildLoginEmailHtml(String name, String ip) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <style>\n" +
                "        body { \n" +
                "            font-family: 'Arial', sans-serif; \n" +
                "            background-color: #f8f9fa; \n" +
                "            padding: 20px; \n" +
                "            margin: 0;\n" +
                "        }\n" +
                "        .container { \n" +
                "            background-color: white; \n" +
                "            padding: 40px; \n" +
                "            border-radius: 12px; \n" +
                "            max-width: 600px; \n" +
                "            margin: 0 auto; \n" +
                "            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);\n" +
                "            border-left: 5px solid #FFB44D;\n" +
                "        }\n" +
                "        .header { \n" +
                "            color: #FFB44D; \n" +
                "            font-weight: bold; \n" +
                "            font-size: 28px; \n" +
                "            margin-bottom: 20px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .highlight { \n" +
                "            color: #FFB44D; \n" +
                "            font-weight: bold; \n" +
                "        }\n" +
                "        .security-box { \n" +
                "            background-color: #FFF9F0; \n" +
                "            border: 2px solid #FFB44D;\n" +
                "            border-radius: 8px; \n" +
                "            padding: 20px; \n" +
                "            margin: 20px 0;\n" +
                "        }\n" +
                "        .info-item { \n" +
                "            margin: 10px 0; \n" +
                "            font-size: 16px; \n" +
                "        }\n" +
                "        .warning { \n" +
                "            background-color: #FFF3E0; \n" +
                "            border-left: 4px solid #FFB44D;\n" +
                "            padding: 15px; \n" +
                "            margin: 20px 0;\n" +
                "            border-radius: 4px;\n" +
                "        }\n" +
                "        .footer { \n" +
                "            margin-top: 30px; \n" +
                "            font-size: 14px; \n" +
                "            color: #666; \n" +
                "            text-align: center;\n" +
                "            border-top: 1px solid #eee;\n" +
                "            padding-top: 20px;\n" +
                "        }\n" +
                "        .logo { \n" +
                "            color: #FFB44D; \n" +
                "            font-weight: bold; \n" +
                "            font-size: 20px; \n" +
                "            margin-bottom: 20px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .button { \n" +
                "            background-color: #FFB44D; \n" +
                "            color: white; \n" +
                "            padding: 12px 30px; \n" +
                "            text-decoration: none; \n" +
                "            border-radius: 6px; \n" +
                "            display: inline-block; \n" +
                "            margin: 20px 0; \n" +
                "            font-weight: bold;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"logo\">ECI EXPRESS</div>\n" +
                "        <div class=\"header\"> Actividad de Inicio de Sesión Detectada</div>\n" +
                "        \n" +
                "        <h2>Hola <span class=\"highlight\">" + name + "</span>,</h2>\n" +
                "        \n" +
                "        <p>Hemos detectado un nuevo inicio de sesión en tu cuenta. Aquí están los detalles:</p>\n" +
                "        \n" +
                "        <div class=\"security-box\">\n" +
                "            <div class=\"info-item\">\n" +
                "                <strong> Dirección IP:</strong> <span class=\"highlight\">" + ip + "</span>\n" +
                "            </div>\n" +
                "            <div class=\"info-item\">\n" +
                "                <strong> Fecha y Hora:</strong> " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")) + "\n" +
                "            </div>\n" +
                "            <div class=\"info-item\">\n" +
                "                <strong> Ubicación:</strong> Aproximada basada en la IP\n" +
                "            </div>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div class=\"warning\">\n" +
                "            <strong>¿No reconoces esta actividad?</strong>\n" +
                "            <p>Si no iniciaste sesión recientemente, tu cuenta podría estar en riesgo. Te recomendamos:</p>\n" +
                "            <ul>\n" +
                "                <li>Cambiar tu contraseña inmediatamente</li>\n" +
                "                <li>Revisar los dispositivos conectados a tu cuenta</li>\n" +
                "                <li>Contactar a soporte si necesitas ayuda</li>\n" +
                "            </ul>\n" +
                "        </div>\n" +
                "        \n" +
                "        <div style=\"text-align: center;\">\n" +
                "            <a href=\"#\" class=\"button\">Verificar Actividad de la Cuenta</a>\n" +
                "        </div>\n" +
                "        \n" +
                "        <p>Si realizaste este inicio de sesión, puedes ignorar este mensaje con tranquilidad.</p>\n" +
                "        \n" +
                "        <div class=\"footer\">\n" +
                "            <p>💛 Por tu seguridad, el equipo de <strong>ECI Express</strong></p>\n" +
                "            <p>Si tienes alguna pregunta, contáctanos a <a href=\"mailto:soporte@eciexpress.com\" style=\"color: #FFB44D;\">soporte@eciexpress.com</a></p>\n" +
                "            <p style=\"font-size: 12px; margin-top: 10px;\">Este es un mensaje automático, por favor no respondas a este correo.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}