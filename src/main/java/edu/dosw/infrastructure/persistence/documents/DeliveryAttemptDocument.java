package edu.dosw.infrastructure.persistence.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DeliveryAttemptDocument {
    private String channel;
    private boolean successful;
    private String error;
    private LocalDateTime timestamp;
}