package edu.dosw.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCommand {
    private String userId;
    private String email;
    private String name;
    private String ip;
    private String orderId;
    private String orderStatus;
    private String pointOfSaleId;
    private String pickupTime;
}