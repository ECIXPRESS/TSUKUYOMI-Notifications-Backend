package edu.dosw.infrastructure.web.controllers;

import edu.dosw.application.ports.EventServicePort;
import edu.dosw.application.dto.command.NotificationCommand;
import edu.dosw.application.dto.command.LoginEventCommand; // ✅ Nuevo DTO
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventServicePort eventServicePort;

    @PostMapping("/successful-login")
    public ResponseEntity<Map<String, String>> receiveSuccessfulLogin(@RequestBody LoginEventCommand loginCommand) {
        try {
            log.info("Received successful login event for user: {}", loginCommand.getUserId());

            LoginEventCommand command = new LoginEventCommand();
            command.setUserId(loginCommand.getUserId());
            command.setEmail(loginCommand.getEmail());
            command.setName(loginCommand.getName());
            command.setIp(loginCommand.getIp());

            eventServicePort.processSuccessfulLogin(command);
            return ResponseEntity.accepted().body(Map.of("message", "Login event processed successfully"));

        } catch (Exception e) {
            log.error("Error processing login event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process login event"));
        }
    }

    @PostMapping("/new-order")
    public ResponseEntity<Map<String, String>> receiveNewOrder(@RequestBody NotificationCommand command) {
        try {
            log.info("Received new order event: {}", command.getOrderId());
            eventServicePort.processNewOrder(command);
            return ResponseEntity.accepted().body(Map.of("message", "New order event processed successfully"));
        } catch (Exception e) {
            log.error("Error processing new order event: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process new order event"));
        }
    }

    @PostMapping("/order-status-change")
    public ResponseEntity<Map<String, String>> receiveOrderStatusChange(@RequestBody NotificationCommand command) {
        try {
            log.info("Received order status change: {} -> {}", command.getOrderId(), command.getOrderStatus());
            eventServicePort.processOrderStatusChange(command);
            return ResponseEntity.accepted().body(Map.of("message", "Order status change processed successfully"));
        } catch (Exception e) {
            log.error("Error processing order status change: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to process order status change"));
        }
    }
}