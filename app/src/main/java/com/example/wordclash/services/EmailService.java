package com.example.wordclash.services;

import android.os.Handler;
import android.os.Looper;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for sending verification emails
 * Note: This is a simplified version. In production, you should use:
 * - Firebase Authentication email verification
 * - SendGrid API
 * - Amazon SES
 * - Or another email service provider
 */
public class EmailService {

    public interface EmailCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Generate a random 6-digit verification code
     */
    public static String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * Send verification email
     *
     * IMPORTANT: This is a mock implementation!
     *
     * For production, you need to:
     * 1. Add email service dependency (e.g., JavaMail API, SendGrid)
     * 2. Configure SMTP settings or API keys
     * 3. Implement actual email sending
     *
     * Example with JavaMail:
     * - Add to build.gradle: implementation 'com.sun.mail:android-mail:1.6.7'
     * - Configure SMTP (Gmail, SendGrid, etc.)
     *
     * Example with Firebase Auth:
     * - Use Firebase Authentication's sendPasswordResetEmail()
     */
    public static void sendVerificationEmail(String email, String code, EmailCallback callback) {
        executorService.execute(() -> {
            try {
                // Simulate network delay
                Thread.sleep(1000);

                // TODO: Replace with actual email sending implementation
                // For now, this just logs the code
                System.out.println("Verification code for " + email + ": " + code);

                // In development, the code is just stored in memory
                // In production, send actual email here

                mainHandler.post(() -> callback.onSuccess());

            } catch (Exception e) {
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }
}