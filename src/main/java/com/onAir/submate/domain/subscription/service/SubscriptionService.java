package com.onAir.submate.domain.subscription.service;

import com.onAir.submate.domain.exchange.service.ExchangeRateService;
import com.onAir.submate.domain.subscription.dto.SubscriptionRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.onAir.submate.domain.subscription.dto.SubscriptionResponse;
import com.onAir.submate.domain.subscription.entity.Subscription;
import com.onAir.submate.domain.subscription.repository.SubscriptionRepository;
import com.onAir.submate.domain.user.entity.User;
import com.onAir.submate.domain.user.repository.UserRepository;
import com.onAir.submate.global.exception.CustomException;
import com.onAir.submate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ExchangeRateService exchangeRateService;

    @Transactional
    public SubscriptionResponse create(Long userId, SubscriptionRequest request) {
        User user = getUser(userId);

        // 달러 결제면 환율 적용해서 원화로 변환 후 저장
        BigDecimal price = request.isDollar()
                ? exchangeRateService.convertUsdToKrw(request.price())
                        .setScale(0, RoundingMode.HALF_UP)
                : request.price();

        Subscription subscription = Subscription.builder()
                .user(user)
                .serviceName(request.serviceName())
                .price(price)
                .originalPrice(request.isDollar() ? request.price() : null)
                .paymentCycle(request.paymentCycle())
                .nextPaymentDate(request.nextPaymentDate())
                .isFreeTrial(request.isFreeTrial())
                .isDollar(request.isDollar())
                .category(request.category())
                .paymentMethod(request.paymentMethod())
                .build();

        // 추가 순서 기반 sort_order 자동 부여
        long count = subscriptionRepository.countByUserAndIsFreeTrial(user, false)
                   + subscriptionRepository.countByUserAndIsFreeTrial(user, true);
        subscription.updateSortOrder((int) count + 1);

        return SubscriptionResponse.from(subscriptionRepository.save(subscription), exchangeRateService);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getMySubscriptions(Long userId) {
        User user = getUser(userId);
        return subscriptionRepository.findByUserOrderBySortOrderAscCreatedAtAsc(user)
                .stream()
                .map(s -> SubscriptionResponse.from(s, exchangeRateService))
                .toList();
    }

    @Transactional
    public SubscriptionResponse update(Long userId, Long subscriptionId, SubscriptionRequest request) {
        Subscription subscription = getSubscriptionWithOwnerCheck(userId, subscriptionId);

        BigDecimal updatedPrice = request.isDollar()
                ? exchangeRateService.convertUsdToKrw(request.price())
                        .setScale(0, RoundingMode.HALF_UP)
                : request.price();

        subscription.update(
                request.serviceName(),
                updatedPrice,
                request.isDollar() ? request.price() : null,
                request.paymentCycle(),
                request.nextPaymentDate(),
                request.isFreeTrial(),
                request.isDollar(),
                request.category(),
                request.paymentMethod()
        );

        return SubscriptionResponse.from(subscription, exchangeRateService);
    }

    @Transactional
    public void delete(Long userId, Long subscriptionId) {
        Subscription subscription = getSubscriptionWithOwnerCheck(userId, subscriptionId);
        subscriptionRepository.delete(subscription);
    }

    @Transactional
    public void reorder(Long userId, List<Long> orderedIds) {
        for (int i = 0; i < orderedIds.size(); i++) {
            Subscription sub = getSubscriptionWithOwnerCheck(userId, orderedIds.get(i));
            sub.updateSortOrder(i + 1);
        }
    }

    private Subscription getSubscriptionWithOwnerCheck(Long userId, Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (!subscription.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.SUBSCRIPTION_ACCESS_DENIED);
        }

        return subscription;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
