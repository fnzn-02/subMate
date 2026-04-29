package com.onAir.submate.domain.exchange.service;

import com.onAir.submate.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateScheduler {

    private final ExchangeRateService exchangeRateService;
    private final SubscriptionRepository subscriptionRepository;

    // 매달 1일 오전 12시 - 달러 구독 가격 현재 환율로 일괄 업데이트
    @Scheduled(cron = "0 0 12 1 * *")
    @Transactional
    public void updateDollarSubscriptionPrices() {
        BigDecimal rate = exchangeRateService.getLatestUsdToKrw();
        var dollarSubs = subscriptionRepository.findByIsDollarTrue();

        if (dollarSubs.isEmpty()) return;

        for (var sub : dollarSubs) {
            if (sub.getOriginalPrice() == null) continue;
            BigDecimal newPrice = sub.getOriginalPrice()
                    .multiply(rate)
                    .setScale(0, RoundingMode.HALF_UP);
            sub.updatePrice(newPrice);
        }

        log.info("달러 구독 가격 업데이트 완료: {}개, 적용 환율: {} KRW", dollarSubs.size(), rate);
    }
}
