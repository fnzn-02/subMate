package com.onAir.submate.domain.exchange.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "exchange_rates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    // USD → KRW 매매기준율
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal usdToKrw;

    @Builder
    public ExchangeRate(LocalDate date, BigDecimal usdToKrw) {
        this.date = date;
        this.usdToKrw = usdToKrw;
    }

    public void update(BigDecimal usdToKrw) {
        this.usdToKrw = usdToKrw;
    }
}
