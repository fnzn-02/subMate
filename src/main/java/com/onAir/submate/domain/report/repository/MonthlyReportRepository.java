package com.onAir.submate.domain.report.repository;

import com.onAir.submate.domain.report.entity.MonthlyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {

    Optional<MonthlyReport> findByUserIdAndYearMonth(Long userId, String yearMonth);

    List<MonthlyReport> findByUserIdOrderByYearMonthDesc(Long userId);
}
