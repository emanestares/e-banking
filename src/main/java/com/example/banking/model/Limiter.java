package com.example.banking.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

@Entity
@Table(name = "limiters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Limiter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "limiter_key", nullable = false, unique = true, length = 100)
    private String limiterKey;

    @DecimalMin(value = "100", message = "Limit must be at least 100")
    @Column(name = "limiter_value", nullable = false, length = 255)
    private String limiterValue;

    @Column(length = 500)
    private String description;
}