package com.onAir.submate.global.batch;

import com.onAir.submate.domain.subscription.entity.Subscription;
import com.onAir.submate.domain.subscription.repository.SubscriptionRepository;
import com.onAir.submate.domain.user.entity.User;
import com.onAir.submate.global.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReminderScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final MailService mailService;

    // 매일 오전 9시 실행
    @Scheduled(cron = "0 0 9 * * *")
    public void sendPaymentReminders() {
        LocalDate today = LocalDate.now();

        for (int daysLeft : List.of(1, 3)) {
            LocalDate targetDate = today.plusDays(daysLeft);
            List<Subscription> subscriptions = subscriptionRepository.findByNextPaymentDate(targetDate);

            for (Subscription sub : subscriptions) {
                User user = sub.getUser();
                try {
                    mailService.sendPaymentReminder(
                            user.getEmail(),
                            user.getNickname(),
                            sub.getServiceName(),
                            sub.getNextPaymentDate(),
                            daysLeft
                    );
                } catch (Exception e) {
                    log.error("결제 알림 발송 실패 - userId: {}, subscriptionId: {}", user.getId(), sub.getId(), e);
                }
            }
        }
    }
}
