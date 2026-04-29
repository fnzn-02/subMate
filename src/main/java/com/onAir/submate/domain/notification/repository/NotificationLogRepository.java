package com.onAir.submate.domain.notification.repository;

import com.onAir.submate.domain.notification.entity.NotificationLog;
import com.onAir.submate.domain.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    boolean existsBySubscriptionIdAndTypeAndSentDate(
            Long subscriptionId, NotificationType type, LocalDate sentDate);

    void deleteByUserId(Long userId);
}
