package com.example.my_app.service;

import android.os.AsyncTask;

import javax.mail.MessagingException;

public class SendEmailTask extends AsyncTask<Void, Void, Boolean> {

    private String title;
    private String body;
    private String mailRecipient;

    public SendEmailTask(String title, String body, String mailRecipient) {
        this.title = title;
        this.body = body;
        this.mailRecipient = mailRecipient;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            EmailSender emailSender = new EmailSender();
            emailSender.sendEmail(title, body, mailRecipient);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
