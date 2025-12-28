package com.niranzan.exam.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    @Qualifier("emailTemplateEngine")
    private SpringTemplateEngine emailTemplateEngine;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${spring.mail.enabled:false}")
    private boolean mailEnabled;

    public void sendPasswordResetEmail(String to, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        
        // If mail is enabled and mailSender is available, send actual email
        if (mailEnabled && mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
                );

                // Prepare the evaluation context
                Context context = new Context();
                context.setVariable("resetLink", resetLink);
                context.setVariable("resetToken", resetToken);
                context.setVariable("frontendUrl", frontendUrl);

                // Process the template
                String htmlContent = emailTemplateEngine.process("password-reset", context);

                helper.setTo(to);
                helper.setSubject("Password Reset Request - Exam Portal");
                helper.setText(htmlContent, true); // true = isHtml

                mailSender.send(message);
                
                log.info("=========================================");
                log.info("✓ Password Reset Email Sent Successfully");
                log.info("To: {}", to);
                log.info("=========================================");
            } catch (MessagingException e) {
                log.error("=========================================");
                log.error("✗ Failed to send email: {}", e.getMessage());
                log.error("Falling back to console output...", e);
                log.error("=========================================");
                // Fall through to console output
                printResetLinkToConsole(to, resetLink, resetToken);
            } catch (Exception e) {
                log.error("=========================================");
                log.error("✗ Failed to process email template: {}", e.getMessage());
                log.error("Falling back to console output...", e);
                log.error("=========================================");
                // Fall through to console output
                printResetLinkToConsole(to, resetLink, resetToken);
            }
        } else {
            // In development/local, print to console
            printResetLinkToConsole(to, resetLink, resetToken);
        }
    }

    private void printResetLinkToConsole(String to, String resetLink, String resetToken) {
        log.info("=========================================");
        log.info("Password Reset Email (Console Mode)");
        log.info("To: {}", to);
        log.info("Reset Link: {}", resetLink);
        log.info("Token: {}", resetToken);
        log.info("=========================================");
        log.info("Note: Set spring.mail.enabled=true in application-local.yml to send actual emails");
        log.info("=========================================");
    }
}
