package com.example.wordclash.screens;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.wordclash.R;
import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;

public class SplashActivity extends AppCompatActivity {

    private CardView logoCard;
    private ImageView ivLogo;
    private TextView tvAppName;
    private TextView tvTagline;
    private ProgressBar progressBar;
    private Handler handler = new Handler();

    private static final int SPLASH_DURATION = 3000; // 3 seconds for beautiful presentation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeViews();
        startBeautifulAnimations();
        checkAuthentication();
    }

    private void initializeViews() {
        logoCard = findViewById(R.id.logoCard);
        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);
        tvTagline = findViewById(R.id.tvTagline);
        progressBar = findViewById(R.id.progressBar);

        // Initially hide views for dramatic entrance
        logoCard.setScaleX(0f);
        logoCard.setScaleY(0f);
        logoCard.setAlpha(0f);
        logoCard.setRotation(-180f);

        ivLogo.setScaleX(0.5f);
        ivLogo.setScaleY(0.5f);
        ivLogo.setAlpha(0f);

        tvAppName.setAlpha(0f);
        tvAppName.setTranslationY(100f);

        tvTagline.setAlpha(0f);
        tvTagline.setTranslationY(50f);

        progressBar.setAlpha(0f);
    }

    private void startBeautifulAnimations() {
        // Animation 1: Logo Card - Dramatic entrance with rotation and bounce
        ObjectAnimator scaleXCard = ObjectAnimator.ofFloat(logoCard, "scaleX", 0f, 1f);
        ObjectAnimator scaleYCard = ObjectAnimator.ofFloat(logoCard, "scaleY", 0f, 1f);
        ObjectAnimator alphaCard = ObjectAnimator.ofFloat(logoCard, "alpha", 0f, 1f);
        ObjectAnimator rotateCard = ObjectAnimator.ofFloat(logoCard, "rotation", -180f, 0f);

        scaleXCard.setDuration(1000);
        scaleYCard.setDuration(1000);
        alphaCard.setDuration(1000);
        rotateCard.setDuration(1000);

        scaleXCard.setInterpolator(new OvershootInterpolator(1.5f));
        scaleYCard.setInterpolator(new OvershootInterpolator(1.5f));
        rotateCard.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet logoCardSet = new AnimatorSet();
        logoCardSet.playTogether(scaleXCard, scaleYCard, alphaCard, rotateCard);
        logoCardSet.setStartDelay(200);
        logoCardSet.start();

        // Animation 2: Logo Image - Scale and fade in
        handler.postDelayed(() -> {
            ObjectAnimator scaleXLogo = ObjectAnimator.ofFloat(ivLogo, "scaleX", 0.5f, 1f);
            ObjectAnimator scaleYLogo = ObjectAnimator.ofFloat(ivLogo, "scaleY", 0.5f, 1f);
            ObjectAnimator alphaLogo = ObjectAnimator.ofFloat(ivLogo, "alpha", 0f, 1f);

            scaleXLogo.setDuration(800);
            scaleYLogo.setDuration(800);
            alphaLogo.setDuration(800);

            scaleXLogo.setInterpolator(new BounceInterpolator());
            scaleYLogo.setInterpolator(new BounceInterpolator());

            AnimatorSet logoImageSet = new AnimatorSet();
            logoImageSet.playTogether(scaleXLogo, scaleYLogo, alphaLogo);
            logoImageSet.start();

            // Continuous subtle pulse animation for logo
            startLogoPulseAnimation();
        }, 600);

        // Animation 3: App Name - Elegant slide up and fade
        handler.postDelayed(() -> {
            ObjectAnimator alphaName = ObjectAnimator.ofFloat(tvAppName, "alpha", 0f, 1f);
            ObjectAnimator translateName = ObjectAnimator.ofFloat(tvAppName, "translationY", 100f, 0f);

            alphaName.setDuration(800);
            translateName.setDuration(800);

            alphaName.setInterpolator(new AccelerateDecelerateInterpolator());
            translateName.setInterpolator(new AccelerateDecelerateInterpolator());

            AnimatorSet nameSet = new AnimatorSet();
            nameSet.playTogether(alphaName, translateName);
            nameSet.start();
        }, 1000);

        // Animation 4: Progress Bar - Smooth fade in
        handler.postDelayed(() -> {
            ObjectAnimator alphaProgress = ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 1f);
            alphaProgress.setDuration(600);
            alphaProgress.start();
        }, 1400);

        // Animation 5: Tagline - Final touch
        handler.postDelayed(() -> {
            ObjectAnimator alphaTagline = ObjectAnimator.ofFloat(tvTagline, "alpha", 0f, 0.9f);
            ObjectAnimator translateTagline = ObjectAnimator.ofFloat(tvTagline, "translationY", 50f, 0f);

            alphaTagline.setDuration(800);
            translateTagline.setDuration(800);

            alphaTagline.setInterpolator(new AccelerateDecelerateInterpolator());
            translateTagline.setInterpolator(new AccelerateDecelerateInterpolator());

            AnimatorSet taglineSet = new AnimatorSet();
            taglineSet.playTogether(alphaTagline, translateTagline);
            taglineSet.start();
        }, 1600);
    }

    private void startLogoPulseAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivLogo, "scaleX", 1f, 1.08f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivLogo, "scaleY", 1f, 1.08f, 1f);

        scaleX.setDuration(2000);
        scaleY.setDuration(2000);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        AnimatorSet pulseSet = new AnimatorSet();
        pulseSet.playTogether(scaleX, scaleY);
        pulseSet.start();
    }

    private void checkAuthentication() {
        handler.postDelayed(() -> {
            if (SharedPreferencesUtils.isUserLoggedIn(SplashActivity.this)) {
                // User is logged in - fetch updated user data
                User oldUser = SharedPreferencesUtils.getUser(SplashActivity.this);

                DatabaseService.getInstance().getUser(oldUser.getId(), new DatabaseService.DatabaseCallback<User>() {
                    @Override
                    public void onCompleted(User newUser) {
                        if (newUser == null) {
                            // User no longer exists in database
                            SharedPreferencesUtils.signOutUser(SplashActivity.this);
                            navigateToStart();
                        } else {
                            // Update local user data
                            SharedPreferencesUtils.saveUser(SplashActivity.this, newUser);
                            navigateToMain();
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        // On error, still navigate to main with cached data
                        navigateToMain();
                    }
                });
            } else {
                // User is not logged in
                navigateToStart();
            }
        }, SPLASH_DURATION);
    }

    private void navigateToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void navigateToStart() {
        Intent intent = new Intent(SplashActivity.this, StartPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}