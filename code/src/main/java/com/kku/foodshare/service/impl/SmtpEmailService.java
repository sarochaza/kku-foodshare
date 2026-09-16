package com.kku.foodshare.service.impl;

import com.kku.foodshare.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class SmtpEmailService
        implements EmailService {

    private final JavaMailSender mailSender;

    private final String sender;

    public SmtpEmailService(
            JavaMailSender mailSender,

            @Value(
                "${app.mail.from:no-reply@localhost}"
            )
            String sender) {

        this.mailSender = mailSender;

        this.sender = sender;
    }

    @Override
    public void sendPasswordResetEmail(
            String recipient,
            String resetUrl) {

        MimeMessage message =
                mailSender.createMimeMessage();

        try {

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                        message,
                        false,
                        StandardCharsets.UTF_8.name()
                    );

            helper.setFrom(sender);

            helper.setTo(recipient);

            helper.setSubject(
                "ตั้งรหัสผ่านใหม่ | KKU FoodShare"
            );

            helper.setText(
                buildHtml(resetUrl),
                true
            );

            mailSender.send(message);

        } catch (
            MessagingException |
            MailException exception
        ) {

            throw new IllegalStateException(
                "Unable to send password reset email",
                exception
            );
        }
    }

    private String buildHtml(
            String resetUrl) {

        return """
            <!doctype html>
            <html lang="th">
            <body style="
                font-family: Arial, sans-serif;
                color: #172554;
                line-height: 1.6;
            ">

                <h2>ตั้งรหัสผ่านใหม่</h2>

                <p>
                    เราได้รับคำขอเปลี่ยนรหัสผ่าน
                    สำหรับบัญชี KKU FoodShare ของคุณ
                </p>

                <p>
                    <a
                        href="%s"
                        style="
                            background: #2563eb;
                            color: white;
                            padding: 12px 20px;
                            border-radius: 10px;
                            text-decoration: none;
                        ">
                        ตั้งรหัสผ่านใหม่
                    </a>
                </p>

                <p>
                    ลิงก์นี้ใช้ได้ 15 นาที
                    และใช้ได้เพียงครั้งเดียว
                </p>

                <p>
                    หากคุณไม่ได้ส่งคำขอนี้
                    สามารถละเว้นอีเมลฉบับนี้ได้
                </p>

            </body>
            </html>
            """.formatted(resetUrl);
    }
}