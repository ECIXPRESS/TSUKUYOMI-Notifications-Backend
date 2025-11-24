package edu.dosw.application.ports;

import edu.dosw.application.dto.command.NotificationCommand;
import edu.dosw.application.dto.command.PasswordResetNotificationCommand;

public interface EventServicePort {
    void processSuccessfulLogin(NotificationCommand command);
    void processNewOrder(NotificationCommand command);
    void processOrderStatusChange(NotificationCommand command);
    void processPasswordResetRequest(NotificationCommand command);
    void processPasswordResetVerified(NotificationCommand command);
    void processPasswordResetCompleted(NotificationCommand command);
}