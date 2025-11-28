package edu.dosw.infrastructure.persistence.documents;

import edu.dosw.infrastructure.persistence.documents.DeliveryAttemptDocument;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "notifications")
public class NotificationDocument {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String userEmail;    // ← NUEVO CAMPO

    private String title;
    private String message;

    @Indexed
    private String type;

    @Indexed
    private String status;

    private List<String> channels;
    private List<DeliveryAttemptDocument> deliveryAttempts;

    @Indexed
    private LocalDateTime createdAt;

    private LocalDateTime readAt;
    private LocalDateTime updatedAt;
    private String metadata;
    private Map<String, Object> additionalData;
}