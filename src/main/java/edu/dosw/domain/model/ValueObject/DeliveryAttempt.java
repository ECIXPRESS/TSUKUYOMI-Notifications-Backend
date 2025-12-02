package edu.dosw.domain.model.ValueObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DeliveryAttempt {
    private Channel channel;
    private boolean successful;
    private String error;
    private LocalDateTime timestamp;
}