package com.charity.model;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.Random;

public class gmailAuth {

    private static final String EMAIL_FROM = "saaditya23cs@student.mes.ac.in";
    private static final String APP_PASSWORD = "jxoo hzpw auoh xvxy";

    public static String sendOTP(String recipientEmail) throws MessagingException {
        String otp = generateOTP(6);
        
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, APP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EMAIL_FROM));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("सहयोग Login OTP");
        message.setText("Your ONE-TIME-PASSWORD is " + otp);

        Transport.send(message);
        
        return otp;
    }

    private static String generateOTP(int length) {
        StringBuilder otp = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}