package com.example.wordclash.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.view.View;

import com.example.wordclash.models.User;

import java.util.Locale;

/**
 * Utility class for managing language and layout direction
 */
public class LanguageUtils {

    /**
     * Apply language settings based on user's learning preference
     * If learning English -> UI in Hebrew (RTL)
     * If learning Hebrew -> UI in English (LTR)
     */
    public static void applyLanguageSettings(Context context, User user) {
        if (user == null) return;

        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";

        // If learning English, UI should be in Hebrew
        // If learning Hebrew, UI should be in English
        String uiLanguage = learningLanguage.equals("english") ? "he" : "en";

        Locale locale = new Locale(uiLanguage);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    /**
     * Set layout direction based on user's learning preference
     */
    public static void setLayoutDirection(Activity activity, User user) {
        if (user == null) return;

        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";

        // If learning English, UI is in Hebrew (RTL)
        // If learning Hebrew, UI is in English (LTR)
        int direction = learningLanguage.equals("english")
                ? View.LAYOUT_DIRECTION_RTL
                : View.LAYOUT_DIRECTION_LTR;

        if (activity.getWindow() != null && activity.getWindow().getDecorView() != null) {
            activity.getWindow().getDecorView().setLayoutDirection(direction);
        }
    }

    /**
     * Check if current UI should be RTL
     */
    public static boolean isRTL(User user) {
        if (user == null) return false;
        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";
        return learningLanguage.equals("english"); // Learning English = UI in Hebrew = RTL
    }

    /**
     * Get UI language code
     */
    public static String getUILanguageCode(User user) {
        if (user == null) return "he";
        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";
        return learningLanguage.equals("english") ? "he" : "en";
    }

    /**
     * Get learning language display name in UI language
     */
    public static String getLearningLanguageDisplayName(Context context, User user) {
        if (user == null) return "";
        String learningLanguage = user.getLearningLanguage();
        if (learningLanguage == null) learningLanguage = "english";

        // Return in UI language
        if (learningLanguage.equals("english")) {
            return "אנגלית"; // Hebrew UI
        } else {
            return "Hebrew"; // English UI
        }
    }
}