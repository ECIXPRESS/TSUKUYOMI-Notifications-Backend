package edu.dosw.domain.ports;

import edu.dosw.domain.model.Notification;

public interface EmailServicePort {
    boolean sendNotificationEmail(Notification notification);
    boolean sendHtmlEmail(String to, String subject, String htmlContent);
}