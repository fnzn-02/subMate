package com.onAir.submate.domain.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        BigDecimal totalMonthlySpending,
        int totalSubscriptionCount,
        int freeTrialCount,
        List<UpcomingPayment> upcomingPayments,
        Map<String, BigDecimal> spendingByCategory
) {
    public record UpcomingPayment(
            Long id,
            String serviceName,
            BigDecimal price,
            LocalDate nextPaymentDate,
            int daysLeft,
            boolean isFreeTrial
    ) {}
}
