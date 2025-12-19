package com.example.wordclash.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;



public class EmailService {
    private static final String TAG = "EmailService";

    // Your Gmail credentials
    private static final String EMAIL = "wordclash445@gmail.com";
    private static final String PASSWORD = "Es4zAGejw4itH5z"; // 16 chars from Google

    public interface EmailCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public static void sendVerificationEmail(String toEmail, String code, EmailCallback callback) {
        executorService.execute(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");

                Session session = Session.getInstance(props, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL, PASSWORD);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
                message.setSubject("WordClash - Verification Code");
                message.setText("Your verification code is: " + code);

                Transport.send(message);

                Log.d(TAG, "Email sent to: " + toEmail);
                mainHandler.post(() -> callback.onSuccess());

            } catch (Exception e) {
                Log.e(TAG, "Failed to send email", e);
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }
}