package com.example.RealMatch.notification.infrastructure.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import com.example.RealMatch.notification.domain.entity.Notification;
import com.example.RealMatch.user.domain.entity.User;
import com.example.RealMatch.user.domain.entity.enums.NotificationChannel;
import com.example.RealMatch.user.domain.repository.UserRepository;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationChannelSender {

    private static final Logger LOG = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${spring.mail.from:realmatch.lab@gmail.com}")
    private String fromAddress;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean isAvailable() {
        return mailSender != null;
    }

    @Override
    public String send(Notification notification) throws Exception {
        User user = userRepository.findById(notification.getUserId())
                .orElseThrow(() -> new PermanentSendFailureException(
                        "User not found for email. userId=" + notification.getUserId()));

        String email = user.getEmail();
        if (email == null || email.isBlank()) {
            throw new PermanentSendFailureException(
                    "User has no email address. userId=" + notification.getUserId());
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(fromAddress);
        helper.setTo(email);
        helper.setSubject("[RealMatch] " + notification.getTitle());
        helper.setText(buildHtmlContent(notification), true);

        mailSender.send(mimeMessage);

        LOG.info("[Email] Sent. userId={}, to={}, notificationId={}",
                notification.getUserId(), email, notification.getId());
        return "email-sent-to:" + email;
    }

    private String buildHtmlContent(Notification notification) {
        String notificationUrl = frontendUrl + "/notifications";
        // HTML Injection 방지를 위해 사용자 입력값 이스케이프 처리
        String escapedTitle = HtmlUtils.htmlEscape(notification.getTitle());
        String escapedBody = HtmlUtils.htmlEscape(notification.getBody());

        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head><meta charset="UTF-8"></head>
                <body style="font-family: 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: #f8f9fa; border-radius: 12px; padding: 32px;">
                        <h2 style="color: #333; margin-bottom: 16px;">%s</h2>
                        <p style="color: #555; font-size: 16px; line-height: 1.6;">%s</p>
                        <div style="margin-top: 24px;">
                            <a href="%s" style="display: inline-block; background: #4A90D9; color: white; padding: 12px 24px; border-radius: 8px; text-decoration: none; font-size: 14px;">
                                알림 확인하기
                            </a>
                        </div>
                    </div>
                    <p style="color: #999; font-size: 12px; margin-top: 16px; text-align: center;">
                        이 메일은 RealMatch에서 발송되었습니다.
                    </p>
                </body>
                </html>
                """.formatted(escapedTitle, escapedBody, notificationUrl);
    }
}
