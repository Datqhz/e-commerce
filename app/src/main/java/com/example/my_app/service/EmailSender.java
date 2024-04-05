package com.example.my_app.service;

import android.os.AsyncTask;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender extends javax.mail.Authenticator {
    private final String mailHost = "smtp.gmail.com";
    private final String mailPort = "587";
    private final String mailUsername = "filmbruh1973@gmail.com";
    private final String mailPassword = "hyjajtdstkzozuft";

    public void sendEmail(String subject, String body, String mailRecipient) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", mailHost);
        properties.put("mail.smtp.port", mailPort);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.transport.protocol", "smtp");

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUsername, mailPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailUsername));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(mailRecipient));
        message.setSubject(subject);
        message.setText(body);
        Transport.send(message);
    }

}
