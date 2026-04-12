package com.onnyth.onnythserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());

        // Database connectivity check (SELECT 1 — no data is inserted or modified)
        Map<String, Object> db = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("SELECT 1");
            db.put("status", "UP");
            db.put("database", connection.getMetaData().getDatabaseProductName());
            db.put("url", connection.getMetaData().getURL());
        } catch (Exception e) {
            db.put("status", "DOWN");
            db.put("error", e.getMessage());
            response.put("status", "DOWN");
            response.put("database", db);
            return ResponseEntity.status(503).body(response);
        }

        response.put("database", db);
        return ResponseEntity.ok(response);
    }
}

