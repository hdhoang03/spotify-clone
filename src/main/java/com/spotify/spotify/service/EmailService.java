package com.spotify.spotify.service;

import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailService {
    JavaMailSender javaMailSender;
    SpringTemplateEngine templateEngine;

    @NonFinal
    @Value("${spring.mail.username}")
    String fromEmail;

    @Async //Gửi mail chạy ngầm không bị block api
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables){
        try {
            //Create content of Thymeleaf in templates folder
            Context context = new Context();
            context.setVariables(variables);

            //Render HTML -> String
            String htmlBody = templateEngine.process(templateName, context);

            //Create an email
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); //true -> Nội dung là HTML

            //Send email
            javaMailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException messagingException){
            log.error("Error sending email to: {}", to, messagingException);
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }
}