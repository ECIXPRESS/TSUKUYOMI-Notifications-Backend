package edu.dosw.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCommand {
    private String userId;
    private String email;
    private String name;
    private String orderId;
    private Double amount;
    private String paymentStatus;
    private String paymentMethod;
    private String currency;
}