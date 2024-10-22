package com.capellax.ecommerce.service;

import com.capellax.ecommerce.exception.EmailFailureException;
import com.capellax.ecommerce.model.VerificationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${email.from}")
    private String fromAddress;

    @Value("${app.frontend.url}")
    private String url;

    private JavaMailSender javaMailSender;

    private SimpleMailMessage makeMailMessage() {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromAddress);
        return simpleMailMessage;
    }

    public void sendVerificationEmail(
            VerificationToken verificationToken
    ) throws EmailFailureException {
        SimpleMailMessage message = makeMailMessage();
        message.setTo(verificationToken.getUser().getEmail());
        message.setSubject("Verify your email to activate your account.");
        message.setText("Please follow the link below to verify your email to activate your account.\n" +
                url + "/auth/verify?token=" + verificationToken.getToken());

        try {
            javaMailSender.send(message);
        } catch (MailException exp) {
            throw new EmailFailureException();
        }
    }

}
