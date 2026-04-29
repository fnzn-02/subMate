package com.onAir.submate.global.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MailService {

    private final RestClient brevoRestClient;

    @Value("${brevo.api-key}")
    private String apiKey;

    @Value("${brevo.sender-email}")
    private String senderEmail;

    public MailService() {
        this.brevoRestClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .build();
    }

    public void sendVerificationCode(String toEmail, String code) {
        send(toEmail, "[SubMate] 이메일 인증 코드", buildEmailHtml(
                "이메일 인증",
                "아래 인증 코드를 앱에 입력해주세요.",
                code,
                "이 코드는 10분간 유효합니다. 본인이 요청하지 않았다면 이 메일을 무시하세요."
        ));
    }

    public void sendPasswordResetCode(String toEmail, String code) {
        send(toEmail, "[SubMate] 비밀번호 재설정 코드", buildEmailHtml(
                "비밀번호 재설정",
                "아래 인증 코드를 앱에 입력해주세요.",
                code,
                "이 코드는 10분간 유효합니다. 본인이 요청하지 않았다면 즉시 비밀번호를 변경하세요."
        ));
    }

    public void sendPaymentReminder(String toEmail, String serviceName, int amount, String paymentDate, int daysLeft) {
        String subject = daysLeft == 1
                ? "[SubMate] 내일 결제 예정 - " + serviceName
                : "[SubMate] " + daysLeft + "일 후 결제 예정 - " + serviceName;

        String title = daysLeft == 1 ? "내일 결제됩니다" : daysLeft + "일 후 결제됩니다";
        String subtitle = String.format("%s 구독료 %,d원이 %s에 결제될 예정입니다.", serviceName, amount, paymentDate);
        String tip = daysLeft == 3
                ? "해지를 원하신다면 지금 바로 처리하세요."
                : "마지막 알림입니다. 필요하지 않다면 오늘 안에 해지하세요.";

        send(toEmail, subject, buildReminderHtml(title, subtitle, serviceName, amount, paymentDate, tip));
    }

    private String buildReminderHtml(String title, String subtitle, String serviceName,
                                     int amount, String paymentDate, String tip) {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"></head>
                <body style="margin:0;padding:0;background:#F1F5F9;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#F1F5F9;padding:40px 0;">
                    <tr><td align="center">
                      <table width="480" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                        <tr>
                          <td style="background:linear-gradient(135deg,#6366F1 0%%,#8B5CF6 100%%);padding:36px 40px;text-align:center;">
                            <div style="font-size:28px;font-weight:800;color:#ffffff;letter-spacing:-0.5px;">SubMate</div>
                            <div style="font-size:13px;color:rgba(255,255,255,0.75);margin-top:4px;">구독 관리의 모든 것</div>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:40px 40px 32px;">
                            <h1 style="margin:0 0 8px;font-size:22px;font-weight:700;color:#0F172A;">%s</h1>
                            <p style="margin:0 0 32px;font-size:15px;color:#64748B;line-height:1.6;">%s</p>
                            <div style="background:#F8FAFC;border:2px solid #E2E8F0;border-radius:12px;padding:24px;margin-bottom:24px;">
                              <div style="font-size:13px;color:#94A3B8;margin-bottom:4px;">서비스</div>
                              <div style="font-size:18px;font-weight:700;color:#0F172A;margin-bottom:16px;">%s</div>
                              <div style="font-size:13px;color:#94A3B8;margin-bottom:4px;">결제 금액</div>
                              <div style="font-size:28px;font-weight:800;color:#6366F1;margin-bottom:16px;">%,d원</div>
                              <div style="font-size:13px;color:#94A3B8;margin-bottom:4px;">결제 예정일</div>
                              <div style="font-size:16px;font-weight:600;color:#0F172A;">%s</div>
                            </div>
                            <p style="margin:0;font-size:13px;color:#94A3B8;line-height:1.6;text-align:center;">%s</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:20px 40px 32px;border-top:1px solid #F1F5F9;text-align:center;">
                            <p style="margin:0;font-size:12px;color:#CBD5E1;">© 2025 SubMate. All rights reserved.</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(title, subtitle, serviceName, amount, paymentDate, tip);
    }

    private String buildEmailHtml(String title, String subtitle, String code, String footer) {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0"></head>
                <body style="margin:0;padding:0;background:#F1F5F9;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#F1F5F9;padding:40px 0;">
                    <tr><td align="center">
                      <table width="480" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                        <!-- 헤더 -->
                        <tr>
                          <td style="background:linear-gradient(135deg,#6366F1 0%%,#8B5CF6 100%%);padding:36px 40px;text-align:center;">
                            <div style="font-size:28px;font-weight:800;color:#ffffff;letter-spacing:-0.5px;">SubMate</div>
                            <div style="font-size:13px;color:rgba(255,255,255,0.75);margin-top:4px;">구독 관리의 모든 것</div>
                          </td>
                        </tr>
                        <!-- 본문 -->
                        <tr>
                          <td style="padding:40px 40px 32px;">
                            <h1 style="margin:0 0 8px;font-size:22px;font-weight:700;color:#0F172A;">%s</h1>
                            <p style="margin:0 0 32px;font-size:15px;color:#64748B;line-height:1.6;">%s</p>
                            <!-- 코드 박스 -->
                            <div style="background:#F8FAFC;border:2px solid #E2E8F0;border-radius:12px;padding:28px;text-align:center;margin-bottom:32px;">
                              <div style="font-size:11px;font-weight:600;color:#94A3B8;letter-spacing:2px;text-transform:uppercase;margin-bottom:12px;">인증 코드</div>
                              <div style="font-size:40px;font-weight:800;color:#6366F1;letter-spacing:12px;">%s</div>
                            </div>
                            <p style="margin:0;font-size:13px;color:#94A3B8;line-height:1.6;text-align:center;">%s</p>
                          </td>
                        </tr>
                        <!-- 푸터 -->
                        <tr>
                          <td style="padding:20px 40px 32px;border-top:1px solid #F1F5F9;text-align:center;">
                            <p style="margin:0;font-size:12px;color:#CBD5E1;">© 2025 SubMate. All rights reserved.</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(title, subtitle, code, footer);
    }

    private void send(String toEmail, String subject, String htmlContent) {
        try {
            Map<String, Object> body = Map.of(
                    "sender", Map.of("name", "SubMate", "email", senderEmail),
                    "to", List.of(Map.of("email", toEmail)),
                    "subject", subject,
                    "htmlContent", htmlContent
            );

            brevoRestClient.post()
                    .uri("/smtp/email")
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("이메일 발송 성공: {}", toEmail);
        } catch (Exception e) {
            log.error("이메일 발송 실패: {} - {}", toEmail, e.getMessage());
        }
    }
}
