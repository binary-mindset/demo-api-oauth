package com.oauth.demo.service;

public interface MailService {

    void sendEmail(String subject, String to, String text);
}
