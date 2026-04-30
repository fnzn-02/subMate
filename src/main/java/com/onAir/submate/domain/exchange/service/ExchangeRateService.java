package com.onAir.submate.domain.exchange.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onAir.submate.domain.exchange.entity.ExchangeRate;
import com.onAir.submate.domain.exchange.repository.ExchangeRateRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${koreaexim.api-key}")
    private String apiKey;

    public static final List<String> DOLLAR_SERVICES = List.of(
            "ChatGPT", "Claude", "Gemini"
    );

    @PostConstruct
    public void initExchangeRate() {
        if (exchangeRateRepository.findByDate(LocalDate.now()).isEmpty()) {
            fetchAndSaveExchangeRate();
        }
    }

    @Scheduled(cron = "0 30 11 * * MON-FRI")
    @Transactional
    public void fetchAndSaveExchangeRate() {
        try {

            // 오늘 데이터 없으면 최대 7일 전까지 재시도 (주말/공휴일 대비)
            for (int i = 0; i < 7; i++) {
                String dateStr = LocalDate.now().minusDays(i)
                        .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String urlStr = "https://oapi.koreaexim.go.kr/site/program/financial/exchangeJSON"
                        + "?authkey=" + apiKey
                        + "&searchdate=" + dateStr
                        + "&data=AP01";

                URLConnection conn = new URL(urlStr).openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "curl/8.7.1");

                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }
                String body = sb.toString();

                if (body.isBlank() || body.equals("[]")) {
                    log.info("환율 데이터 없음, 이전 날짜 재시도: {}", dateStr);
                    continue;
                }

                List<Map<String, Object>> list = objectMapper.readValue(body, new TypeReference<>() {});
                if (list == null || list.isEmpty()) continue;

                for (Map<String, Object> item : list) {
                    if ("USD".equals(item.get("cur_unit"))) {
                        BigDecimal rate = new BigDecimal(
                                item.get("deal_bas_r").toString().replace(",", ""));
                        LocalDate date = LocalDate.now();
                        exchangeRateRepository.findByDate(date).ifPresentOrElse(
                                existing -> existing.update(rate),
                                () -> exchangeRateRepository.save(
                                        ExchangeRate.builder().date(date).usdToKrw(rate).build())
                        );
                        log.info("환율 업데이트 완료: 1 USD = {} KRW (기준일: {})", rate, dateStr);
                        return;
                    }
                }
            }
            log.warn("7일간 환율 데이터를 가져오지 못했습니다.");
        } catch (Exception e) {
            log.error("환율 API 호출 실패", e);
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal getLatestUsdToKrw() {
        return exchangeRateRepository.findTopByOrderByDateDesc()
                .map(ExchangeRate::getUsdToKrw)
                .orElse(new BigDecimal("1350"));
    }

    public BigDecimal convertUsdToKrw(BigDecimal usdAmount) {
        return usdAmount.multiply(getLatestUsdToKrw());
    }

    public boolean isDollarService(String serviceName) {
        return DOLLAR_SERVICES.contains(serviceName);
    }
}
