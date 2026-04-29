package com.onAir.submate.global.batch;

import com.onAir.submate.domain.notification.entity.NotificationLog;
import com.onAir.submate.domain.notification.entity.NotificationType;
import com.onAir.submate.domain.notification.repository.NotificationLogRepository;
import com.onAir.submate.domain.subscription.entity.Subscription;
import com.onAir.submate.domain.subscription.repository.SubscriptionRepository;
import com.onAir.submate.global.infra.FcmService;
import com.onAir.submate.global.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentNotificationScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final MailService mailService;
    private final FcmService fcmService;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("M월 d일");

    // 매일 자정 - 지난 결제일 자동 전진
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void advancePaymentDates() {
        LocalDate today = LocalDate.now();
        List<Subscription> overdue = subscriptionRepository.findByNextPaymentDateBefore(today);

        for (Subscription sub : overdue) {
            sub.advanceNextPaymentDate();
        }

        if (!overdue.isEmpty()) {
            log.info("결제일 전진 처리: {}건", overdue.size());
        }
    }

    // 매일 오전 9시 - D-3, D-1 알림 발송
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendPaymentReminders() {
        LocalDate today = LocalDate.now();
        sendReminders(today.plusDays(3), NotificationType.D3, 3);
        sendReminders(today.plusDays(1), NotificationType.D1, 1);
    }

    private void sendReminders(LocalDate targetDate, NotificationType type, int daysLeft) {
        List<Subscription> subs = subscriptionRepository.findByNextPaymentDate(targetDate);
        log.info("{} 알림 대상: {}건 ({})", type, subs.size(), targetDate);

        for (Subscription sub : subs) {
            // 중복 발송 방지
            if (notificationLogRepository.existsBySubscriptionIdAndTypeAndSentDate(
                    sub.getId(), type, LocalDate.now())) {
                continue;
            }

            String email = sub.getUser().getEmail();
            String fcmToken = sub.getUser().getFcmToken();
            String serviceName = sub.getServiceName();
            int amount = sub.getPrice().intValue();
            String dateStr = targetDate.format(DATE_FORMAT);

            // 이메일 발송
            mailService.sendPaymentReminder(email, serviceName, amount, dateStr, daysLeft);

            // FCM 푸시 발송
            if (fcmToken != null) {
                String title = daysLeft == 1 ? "💳 내일 결제 예정" : "💳 " + daysLeft + "일 후 결제 예정";
                String body = String.format("%s %,d원이 %s에 결제됩니다.", serviceName, amount, dateStr);
                fcmService.sendNotification(fcmToken, title, body);
            }

            // 발송 기록 저장
            notificationLogRepository.save(
                    NotificationLog.builder()
                            .user(sub.getUser())
                            .subscription(sub)
                            .type(type)
                            .sentDate(LocalDate.now())
                            .build()
            );
        }
    }
}
