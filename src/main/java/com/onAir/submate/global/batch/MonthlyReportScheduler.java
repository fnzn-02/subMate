package com.onAir.submate.global.batch;

import com.onAir.submate.domain.report.service.MonthlyReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlyReportScheduler {

    private final MonthlyReportService monthlyReportService;

    /**
     * 매월 1일 오전 9시에 전월 리포트 일괄 생성
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 9 1 * *")
    public void generateMonthlyReports() {
        log.info("월간 리포트 스케줄러 시작");
        try {
            monthlyReportService.generateAllReports();
            log.info("월간 리포트 스케줄러 완료");
        } catch (Exception e) {
            log.error("월간 리포트 스케줄러 오류", e);
        }
    }
}
