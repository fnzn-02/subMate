package com.onAir.submate.domain.subscription.service;

import com.onAir.submate.domain.subscription.dto.SubscriptionRequest;
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

    @Transactional
    public SubscriptionResponse create(Long userId, SubscriptionRequest request) {
        User user = getUser(userId);

        Subscription subscription = Subscription.builder()
                .user(user)
                .serviceName(request.serviceName())
                .price(request.price())
                .paymentCycle(request.paymentCycle())
                .nextPaymentDate(request.nextPaymentDate())
                .isFreeTrial(request.isFreeTrial())
                .category(request.category())
                .build();

        // 추가 순서 기반 sort_order 자동 부여
        long count = subscriptionRepository.countByUserAndIsFreeTrial(user, false)
                   + subscriptionRepository.countByUserAndIsFreeTrial(user, true);
        subscription.updateSortOrder((int) count + 1);

        return SubscriptionResponse.from(subscriptionRepository.save(subscription));
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getMySubscriptions(Long userId) {
        User user = getUser(userId);
        return subscriptionRepository.findByUserOrderBySortOrderAscCreatedAtAsc(user)
                .stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    @Transactional
    public SubscriptionResponse update(Long userId, Long subscriptionId, SubscriptionRequest request) {
        Subscription subscription = getSubscriptionWithOwnerCheck(userId, subscriptionId);

        subscription.update(
                request.serviceName(),
                request.price(),
                request.paymentCycle(),
                request.nextPaymentDate(),
                request.isFreeTrial(),
                request.category()
        );

        return SubscriptionResponse.from(subscription);
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
