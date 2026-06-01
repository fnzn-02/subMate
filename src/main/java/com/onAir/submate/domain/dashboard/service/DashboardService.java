package com.onAir.submate.domain.dashboard.service;

import com.onAir.submate.domain.dashboard.dto.DashboardResponse;
import com.onAir.submate.domain.dashboard.dto.DashboardResponse.UpcomingPayment;
import com.onAir.submate.domain.subscription.entity.PaymentCycle;
import com.onAir.submate.domain.subscription.entity.Subscription;
import com.onAir.submate.domain.subscription.repository.SubscriptionRepository;
import com.onAir.submate.domain.user.entity.User;
import com.onAir.submate.domain.user.repository.UserRepository;
import com.onAir.submate.global.exception.CustomException;
import com.onAir.submate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final BigDecimal MONTHS_IN_YEAR = BigDecimal.valueOf(12);

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Subscription> all = subscriptionRepository.findByUserOrderBySortOrderAscCreatedAtAsc(user);
        LocalDate today = LocalDate.now();

        BigDecimal totalMonthlySpending = calculateTotalMonthlySpending(all);
        int totalCount = all.size();
        long freeTrialCount = all.stream().filter(Subscription::isFreeTrial).count();

        List<UpcomingPayment> upcoming = getUpcomingPayments(user, today);
        Map<String, BigDecimal> spendingByCategory = calculateSpendingByCategory(all);

        return new DashboardResponse(
                totalMonthlySpending,
                totalCount,
                (int) freeTrialCount,
                upcoming,
                spendingByCategory
        );
    }

    // 연간 구독은 ÷12 하여 월 환산
    private BigDecimal calculateTotalMonthlySpending(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .map(s -> s.getPaymentCycle() == PaymentCycle.YEARLY
                        ? s.getPrice().divide(MONTHS_IN_YEAR, 0, RoundingMode.HALF_UP)
                        : s.getPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<UpcomingPayment> getUpcomingPayments(User user, LocalDate today) {
        LocalDate sevenDaysLater = today.plusDays(7);
        List<UpcomingPayment> upcoming = subscriptionRepository
                .findByUserAndNextPaymentDateBetweenOrderByNextPaymentDateAsc(user, today, sevenDaysLater)
                .stream()
                .map(s -> new UpcomingPayment(
                        s.getId(),
                        s.getServiceName(),
                        s.getPrice(),
                        s.getNextPaymentDate(),
                        (int) ChronoUnit.DAYS.between(today, s.getNextPaymentDate()),
                        s.isFreeTrial()
                ))
                .toList();

        if (!upcoming.isEmpty()) return upcoming;

        // 7일 내 결제 없으면 가장 가까운 결제 1건 표시
        return subscriptionRepository
                .findByUserAndNextPaymentDateAfterOrderByNextPaymentDateAsc(user, today)
                .stream()
                .limit(1)
                .map(s -> new UpcomingPayment(
                        s.getId(),
                        s.getServiceName(),
                        s.getPrice(),
                        s.getNextPaymentDate(),
                        (int) ChronoUnit.DAYS.between(today, s.getNextPaymentDate()),
                        s.isFreeTrial()
                ))
                .toList();
    }

    // 카테고리별 월 환산 지출 합계, 금액 내림차순 정렬
    private Map<String, BigDecimal> calculateSpendingByCategory(List<Subscription> subscriptions) {
        return subscriptions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getCategory().name(),
                        Collectors.reducing(BigDecimal.ZERO,
                                s -> s.getPaymentCycle() == PaymentCycle.YEARLY
                                        ? s.getPrice().divide(MONTHS_IN_YEAR, 0, RoundingMode.HALF_UP)
                                        : s.getPrice(),
                                BigDecimal::add)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }
}
