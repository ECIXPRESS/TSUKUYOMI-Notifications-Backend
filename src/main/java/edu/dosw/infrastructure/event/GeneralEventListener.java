package edu.dosw.infrastructure.event;

import edu.dosw.application.ports.EventServicePort;
import edu.dosw.application.dto.command.NotificationCommand;
import edu.dosw.application.dto.command.PasswordResetNotificationCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneralEventListener implements MessageListener {

    private final EventServicePort eventServicePort;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern);
        String body = new String(message.getBody());

        try {
            log.info("Evento recibido - Canal: {}", channel);

            EventWrapper eventWrapper = objectMapper.readValue(body, EventWrapper.class);

            log.info("Procesando evento - Tipo: {}, ID: {}",
                    eventWrapper.getEventType(), eventWrapper.getEventId());

            switch (eventWrapper.getEventType()) {
                case "login.success":
                    handleLoginSuccess(eventWrapper);
                    break;
                case "order.new":
                    handleNewOrder(eventWrapper);
                    break;
                case "order.status.change":
                    handleOrderStatusChange(eventWrapper);
                    break;
                case "user.registered":
                    handleUserRegistered(eventWrapper);
                    break;
                case "password.reset.requested":
                    handlePasswordResetRequested(eventWrapper);
                    break;
                case "password.reset.verified":
                    handlePasswordResetVerified(eventWrapper);
                    break;
                case "password.reset.completed":
                    handlePasswordResetCompleted(eventWrapper);
                    break;
                default:
                    log.warn("Tipo de evento no manejado: {}", eventWrapper.getEventType());
                    handleUnknownEvent(eventWrapper);
            }

            log.info("Evento procesado - Tipo: {}, ID: {}",
                    eventWrapper.getEventType(), eventWrapper.getEventId());

        } catch (Exception e) {
            log.error("Error procesando evento del canal {}: {}", channel, e.getMessage(), e);
            log.error("Body del evento problemático: {}", body);
        }
    }

    private void handleLoginSuccess(EventWrapper wrapper) {
        try {
            LoginEventData data = objectMapper.convertValue(wrapper.getData(), LoginEventData.class);

            NotificationCommand command = new NotificationCommand();
            command.setUserId(data.getUserId());
            command.setEmail(data.getEmail());
            command.setName(data.getName());
            command.setIp(data.getIp());

            eventServicePort.processSuccessfulLogin(command);

            log.info("Notificación de login procesada para: {}", data.getEmail());

        } catch (Exception e) {
            log.error("Error en handleLoginSuccess: {}", e.getMessage(), e);
        }
    }

    private void handleNewOrder(EventWrapper wrapper) {
        try {
            OrderEventData data = objectMapper.convertValue(wrapper.getData(), OrderEventData.class);

            NotificationCommand command = new NotificationCommand();
            command.setUserId(data.getUserId());
            command.setOrderId(data.getOrderId());

            eventServicePort.processNewOrder(command);

            log.info("Notificación de nueva orden procesada: {}", data.getOrderId());

        } catch (Exception e) {
            log.error("Error en handleNewOrder: {}", e.getMessage(), e);
        }
    }

    private void handleOrderStatusChange(EventWrapper wrapper) {
        try {
            OrderStatusEventData data = objectMapper.convertValue(wrapper.getData(), OrderStatusEventData.class);

            NotificationCommand command = new NotificationCommand();
            command.setUserId(data.getUserId());
            command.setOrderId(data.getOrderId());
            command.setOrderStatus(data.getNewStatus());

            eventServicePort.processOrderStatusChange(command);

            log.info("Notificación de cambio de estado procesada: {} -> {}",
                    data.getOldStatus(), data.getNewStatus());

        } catch (Exception e) {
            log.error("Error en handleOrderStatusChange: {}", e.getMessage(), e);
        }
    }

    private void handleUserRegistered(EventWrapper wrapper) {
        try {
            UserRegisteredEventData data = objectMapper.convertValue(wrapper.getData(), UserRegisteredEventData.class);

            NotificationCommand command = new NotificationCommand();
            command.setUserId(data.getUserId());
            command.setEmail(data.getEmail());
            command.setName(data.getName());

            log.info("Notificación de registro procesada para: {}", data.getEmail());

        } catch (Exception e) {
            log.error("Error en handleUserRegistered: {}", e.getMessage(), e);
        }
    }

    private void handlePasswordResetRequested(EventWrapper wrapper) {
        try {
            PasswordResetRequestEventData data = objectMapper.convertValue(wrapper.getData(), PasswordResetRequestEventData.class);

            PasswordResetNotificationCommand command = new PasswordResetNotificationCommand();
            command.setUserId(data.getUserId());
            command.setEmail(data.getEmail());
            command.setName(data.getName());
            command.setVerificationCode(data.getVerificationCode());

            // Cast al service para usar el método sobrecargado
            if (eventServicePort instanceof edu.dosw.application.services.NotificationApplicationService) {
                edu.dosw.application.services.NotificationApplicationService service =
                        (edu.dosw.application.services.NotificationApplicationService) eventServicePort;
                service.processPasswordResetRequest(command);
            } else {
                log.error("EventServicePort no es una instancia de NotificationApplicationService");
            }

            log.info("Notificación de password reset procesada para: {}", data.getEmail());

        } catch (Exception e) {
            log.error("Error en handlePasswordResetRequested: {}", e.getMessage(), e);
        }
    }

    private void handlePasswordResetVerified(EventWrapper wrapper) {
        try {
            PasswordResetVerifiedEventData data = objectMapper.convertValue(wrapper.getData(), PasswordResetVerifiedEventData.class);

            PasswordResetNotificationCommand command = new PasswordResetNotificationCommand();
            command.setEmail(data.getEmail());
            command.setVerificationCode(data.getVerificationCode());

            if (eventServicePort instanceof edu.dosw.application.services.NotificationApplicationService) {
                edu.dosw.application.services.NotificationApplicationService service =
                        (edu.dosw.application.services.NotificationApplicationService) eventServicePort;
                service.processPasswordResetVerified(command);
            } else {
                log.error("EventServicePort no es una instancia de NotificationApplicationService");
            }

            log.info("Notificación de password reset verificado para: {}", data.getEmail());

        } catch (Exception e) {
            log.error("Error en handlePasswordResetVerified: {}", e.getMessage(), e);
        }
    }

    private void handlePasswordResetCompleted(EventWrapper wrapper) {
        try {
            PasswordResetCompletedEventData data = objectMapper.convertValue(wrapper.getData(), PasswordResetCompletedEventData.class);

            PasswordResetNotificationCommand command = new PasswordResetNotificationCommand();
            command.setUserId(data.getUserId());
            command.setEmail(data.getEmail());
            command.setName(data.getName());

            if (eventServicePort instanceof edu.dosw.application.services.NotificationApplicationService) {
                edu.dosw.application.services.NotificationApplicationService service =
                        (edu.dosw.application.services.NotificationApplicationService) eventServicePort;
                service.processPasswordResetCompleted(command);
            } else {
                log.error("EventServicePort no es una instancia de NotificationApplicationService");
            }

            log.info("Notificación de password reset completado para: {}", data.getEmail());

        } catch (Exception e) {
            log.error("Error en handlePasswordResetCompleted: {}", e.getMessage(), e);
        }
    }

    private void handleUnknownEvent(EventWrapper wrapper) {
        log.info("Evento desconocido recibido - Tipo: {}, Datos: {}",
                wrapper.getEventType(), wrapper.getData());
    }

    @lombok.Data
    public static class EventWrapper {
        private String eventId;
        private String eventType;
        private String timestamp;
        private String version;
        private Object data;
    }

    @lombok.Data
    public static class LoginEventData {
        private String email;
        private String userId;
        private String name;
        private String ip;
        private String userAgent;
    }

    @lombok.Data
    public static class OrderEventData {
        private String orderId;
        private String userId;
        private String customerEmail;
        private Double amount;
        private String currency;
    }

    @lombok.Data
    public static class OrderStatusEventData {
        private String orderId;
        private String userId;
        private String oldStatus;
        private String newStatus;
        private String changedAt;
    }

    @lombok.Data
    public static class UserRegisteredEventData {
        private String email;
        private String userId;
        private String name;
        private String role;
        private String registrationDate;
    }

    @lombok.Data
    public static class PasswordResetRequestEventData {
        private String email;
        private String userId;
        private String name;
        private String verificationCode;
    }

    @lombok.Data
    public static class PasswordResetVerifiedEventData {
        private String email;
        private String verificationCode;
    }

    @lombok.Data
    public static class PasswordResetCompletedEventData {
        private String email;
        private String userId;
        private String name;
        private boolean success;
    }
}