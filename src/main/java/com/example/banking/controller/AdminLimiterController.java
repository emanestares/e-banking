package com.example.banking.controller;

import com.example.banking.dto.ApiResponse;
import com.example.banking.model.Limiter;
import com.example.banking.repository.LimiterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/limiters")
public class AdminLimiterController {

    @Autowired
    private LimiterRepository limiterRepository;

    @GetMapping
    public ResponseEntity<?> getAllLimiters() {
        // Crucial: Transform the String column to a numeric value for HTML5 inputs
        List<Map<String, Object>> responseList = limiterRepository.findAll().stream().map(l -> {
            BigDecimal numericValue;
            try {
                numericValue = new BigDecimal(l.getLimiterValue());
            } catch (Exception e) {
                numericValue = BigDecimal.ZERO;
            }

            return Map.<String, Object>of(
                    "id", l.getId(),
                    "limiterKey", l.getLimiterKey(),
                    "limiterValue", numericValue, // Sent as a clean JSON number primitive
                    "description", l.getDescription()
            );
        }).collect(Collectors.toList());

        // Wraps perfectly inside your ApiResponse matching res.data.data
        return ResponseEntity.ok(new ApiResponse(true, "Fetched limiters", responseList));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLimiter(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Limiter limiter = limiterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Limiter constraint configuration target not found"));

        Object rawValue = payload.get("limiterValue");
        if (rawValue == null || rawValue.toString().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Limiter ceiling threshold limit value is required", null));
        }

        try {
            BigDecimal parsedValue = new BigDecimal(rawValue.toString());
            if (parsedValue.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Limiter baseline scale value cannot be negative", null));
            }
            // Retain uniform scale consistency across database tables
            limiter.setLimiterValue(parsedValue.setScale(2, java.math.RoundingMode.HALF_UP).toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid numeric decimal configuration pattern", null));
        }

        Limiter savedLimiter = limiterRepository.save(limiter);
        return ResponseEntity.ok(new ApiResponse(true, "Limiter updated successfully", savedLimiter));
    }
}