package edu.dosw.infrastructure.web.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Health check requested");
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "notifications-service",
                "timestamp", java.time.LocalDateTime.now().toString(),
                "version", "1.0.0"
        ));
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readinessCheck() {
        log.info("Readiness check requested");
        return ResponseEntity.ok(Map.of(
                "status", "READY",
                "service", "notifications-service",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> livenessCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "LIVE",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}