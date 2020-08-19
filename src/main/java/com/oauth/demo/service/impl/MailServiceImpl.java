package com.oauth.demo.service.impl;

import com.oauth.demo.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender emailSender;

    @Autowired
    public MailServiceImpl(final JavaMailSender mailSender) {
        this.emailSender = mailSender;
    }

    @Override
    public void sendEmail(String subject, String to, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

}
