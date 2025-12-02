package edu.dosw.application.ports;

import edu.dosw.application.dto.command.LoginEventCommand;
import edu.dosw.application.dto.command.NotificationCommand;
import edu.dosw.application.dto.command.PasswordResetNotificationCommand;
import edu.dosw.application.dto.command.PaymentCommand;

public interface EventServicePort {
    void processSuccessfulLogin(LoginEventCommand command);
    void processNewOrder(NotificationCommand command);
    void processOrderStatusChange(NotificationCommand command);
    void processPasswordResetRequest(PasswordResetNotificationCommand command);
    void processPasswordResetVerified(PasswordResetNotificationCommand command);
    void processPasswordResetCompleted(PasswordResetNotificationCommand command);
    void processPaymentCompleted(PaymentCommand command);
    void processPaymentFailed(PaymentCommand command);
}