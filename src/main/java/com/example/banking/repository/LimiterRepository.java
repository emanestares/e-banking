package com.example.banking.repository;

import com.example.banking.model.Limiter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LimiterRepository extends JpaRepository<Limiter, Long> {
    Optional<Limiter> findByLimiterKey(String limiterKey);
}