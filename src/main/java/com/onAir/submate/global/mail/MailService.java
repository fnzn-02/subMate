package com.onAir.submate.global.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Async
    public void sendVerificationCode(String to, String code) {
        String subject = "[subMate] 이메일 인증 코드";
        String html = """
            <div style="font-family:'Apple SD Gothic Neo',sans-serif;max-width:480px;margin:0 auto;padding:32px;background:#f8f9ff;border-radius:16px;">
              <div style="text-align:center;margin-bottom:24px;">
                <div style="display:inline-block;width:48px;height:48px;background:linear-gradient(135deg,#6366f1,#9333ea);border-radius:12px;line-height:48px;color:white;font-size:24px;">✓</div>
                <h1 style="color:#1f2937;font-size:22px;margin:12px 0 4px;">이메일 인증</h1>
                <p style="color:#6b7280;font-size:14px;margin:0;">subMate 회원가입을 완료해주세요</p>
              </div>
              <div style="background:white;border-radius:12px;padding:24px;text-align:center;border:1px solid #e5e7eb;">
                <p style="color:#6b7280;font-size:14px;margin:0 0 12px;">인증 코드</p>
                <div style="font-size:36px;font-weight:800;letter-spacing:8px;color:#6366f1;">%s</div>
                <p style="color:#9ca3af;font-size:12px;margin:12px 0 0;">10분 내에 입력해주세요</p>
              </div>
              <p style="color:#9ca3af;font-size:12px;text-align:center;margin-top:16px;">본인이 요청하지 않은 경우 이 메일을 무시하세요.</p>
            </div>
            """.formatted(code);
        send(to, subject, html);
    }

    @Async
    public void sendPasswordResetCode(String to, String code) {
        String subject = "[subMate] 비밀번호 재설정 코드";
        String html = """
            <div style="font-family:'Apple SD Gothic Neo',sans-serif;max-width:480px;margin:0 auto;padding:32px;background:#f8f9ff;border-radius:16px;">
              <div style="text-align:center;margin-bottom:24px;">
                <div style="display:inline-block;width:48px;height:48px;background:linear-gradient(135deg,#f97316,#ec4899);border-radius:12px;line-height:48px;color:white;font-size:24px;">🔑</div>
                <h1 style="color:#1f2937;font-size:22px;margin:12px 0 4px;">비밀번호 재설정</h1>
                <p style="color:#6b7280;font-size:14px;margin:0;">아래 코드로 비밀번호를 재설정하세요</p>
              </div>
              <div style="background:white;border-radius:12px;padding:24px;text-align:center;border:1px solid #e5e7eb;">
                <p style="color:#6b7280;font-size:14px;margin:0 0 12px;">재설정 코드</p>
                <div style="font-size:36px;font-weight:800;letter-spacing:8px;color:#f97316;">%s</div>
                <p style="color:#9ca3af;font-size:12px;margin:12px 0 0;">10분 내에 입력해주세요</p>
              </div>
              <p style="color:#9ca3af;font-size:12px;text-align:center;margin-top:16px;">본인이 요청하지 않은 경우 즉시 비밀번호를 변경하세요.</p>
            </div>
            """.formatted(code);
        send(to, subject, html);
    }

    @Async
    public void sendPaymentReminder(String to, String nickname, String serviceName,
                                    LocalDate paymentDate, int daysLeft) {
        String dLabel = daysLeft == 0 ? "오늘" : daysLeft + "일 후";
        String subject = "[subMate] %s 결제 %s 예정".formatted(serviceName, dLabel);
        String html = """
            <div style="font-family:'Apple SD Gothic Neo',sans-serif;max-width:480px;margin:0 auto;padding:32px;background:#f8f9ff;border-radius:16px;">
              <div style="text-align:center;margin-bottom:24px;">
                <div style="display:inline-block;width:48px;height:48px;background:linear-gradient(135deg,#6366f1,#9333ea);border-radius:12px;line-height:48px;color:white;font-size:24px;">💳</div>
                <h1 style="color:#1f2937;font-size:22px;margin:12px 0 4px;">결제 임박 알림</h1>
                <p style="color:#6b7280;font-size:14px;margin:0;">%s님의 구독 결제가 곧 예정되어 있어요</p>
              </div>
              <div style="background:white;border-radius:12px;padding:24px;border:1px solid #e5e7eb;">
                <table style="width:100%%;border-collapse:collapse;">
                  <tr><td style="padding:8px 0;color:#6b7280;font-size:14px;">서비스</td><td style="text-align:right;font-weight:700;color:#1f2937;">%s</td></tr>
                  <tr><td style="padding:8px 0;color:#6b7280;font-size:14px;">결제일</td><td style="text-align:right;font-weight:700;color:#1f2937;">%s</td></tr>
                  <tr><td style="padding:8px 0;color:#6b7280;font-size:14px;">D-Day</td><td style="text-align:right;font-weight:700;color:#6366f1;">%s</td></tr>
                </table>
              </div>
              <p style="color:#9ca3af;font-size:12px;text-align:center;margin-top:16px;">subMate에서 구독을 관리하세요</p>
            </div>
            """.formatted(nickname, serviceName, paymentDate.toString(), dLabel);
        send(to, subject, html);
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("메일 발송 실패 - to: {}, subject: {}", to, subject, e);
        }
    }
}
