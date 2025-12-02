package edu.dosw.application.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String userId;
    private String userEmail;
    private String title;
    private String message;
    private String type;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private LocalDateTime updatedAt;
    private String metadata;
    private List<String> channels;
    private List<DeliveryAttemptResponse> deliveryAttempts;
    private Map<String, Object> additionalData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryAttemptResponse {
        private String channel;
        private boolean successful;
        private String error;
        private LocalDateTime timestamp;
        private String status;
    }
}