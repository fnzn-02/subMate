package com.onAir.submate.domain.subscription.repository;

import com.onAir.submate.domain.subscription.entity.Subscription;
import com.onAir.submate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserOrderBySortOrderAscCreatedAtAsc(User user);

    // 배치: 특정 날짜에 결제 예정인 구독 조회
    List<Subscription> findByNextPaymentDate(LocalDate date);

    // 배치: 특정 날짜 범위 내 결제 예정 구독 조회 (D-7 알림용)
    List<Subscription> findByNextPaymentDateBetween(LocalDate from, LocalDate to);

    // 대시보드: 사용자의 무료체험 구독 수 조회
    long countByUserAndIsFreeTrial(User user, boolean isFreeTrial);

    // 대시보드: 사용자의 7일 이내 결제 예정 구독 조회
    List<Subscription> findByUserAndNextPaymentDateBetweenOrderByNextPaymentDateAsc(
            User user, LocalDate from, LocalDate to);

    // 스케줄러: 결제일이 오늘 이전인 구독 (날짜 전진 대상)
    List<Subscription> findByNextPaymentDateBefore(LocalDate date);

    // 환율 업데이트: 달러 구독 전체 조회
    List<Subscription> findByIsDollarTrue();

    // 회원 탈퇴: 유저 구독 일괄 삭제
    @Modifying
    @Query("DELETE FROM Subscription s WHERE s.user = :user")
    void deleteAllByUser(User user);
}
