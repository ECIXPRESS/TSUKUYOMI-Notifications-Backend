package edu.dosw.infrastructure.email;

import edu.dosw.domain.ports.EmailServicePort;
import edu.dosw.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailServiceAdapter implements EmailServicePort {

    private final JavaMailSender mailSender;

    @Override
    public boolean sendNotificationEmail(Notification notification) {
        try {
            if (mailSender instanceof JavaMailSenderImpl) {
                JavaMailSenderImpl impl = (JavaMailSenderImpl) mailSender;
                log.debug("GMAIL CONFIG - Host: {}, Port: {}, Username: {}",
                        impl.getHost(), impl.getPort(), impl.getUsername());
            }

            String userEmail = notification.getUserEmail();
            log.debug("Sending to: {}", userEmail);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("manuelalejandro.guarnizo@gmail.com");
            message.setTo(userEmail);
            message.setSubject(notification.getTitle());
            message.setText(buildEmailText(notification));

            mailSender.send(message);
            log.info("GMAIL SUCCESS - Email sent to: {}", userEmail);
            return true;

        } catch (Exception e) {
            log.error("GMAIL ERROR - Auth failed: {}", e.getMessage());
            log.error("Full error: ", e);
            return false;
        }
    }

    @Override
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("HTML email sent successfully to: {}", to);
            return true;

        } catch (MessagingException e) {
            log.error("Error sending HTML email to {}: {}", to, e.getMessage());
            return false;
        }
    }

    private String buildEmailText(Notification notification) {
        return String.format(
                "%s%n%n%s%n%nDate: %s%nType: %s%n%nThank you,%nECI Express Team",
                notification.getTitle(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getType().name()
        );
    }
}
