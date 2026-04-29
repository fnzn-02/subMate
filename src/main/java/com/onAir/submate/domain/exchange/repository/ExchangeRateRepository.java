package com.onAir.submate.domain.exchange.repository;

import com.onAir.submate.domain.exchange.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    Optional<ExchangeRate> findByDate(LocalDate date);
    Optional<ExchangeRate> findTopByOrderByDateDesc();
}
