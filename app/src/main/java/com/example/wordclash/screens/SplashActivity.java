package com.example.wordclash.screens;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
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
    private ProgressBar progressBar;
    private Handler handler = new Handler();

    private static final int SPLASH_DURATION = 2500; // 2.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeViews();
        startAnimations();
        checkAuthentication();
    }

    private void initializeViews() {
        logoCard = findViewById(R.id.logoCard);
        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);
        progressBar = findViewById(R.id.progressBar);

        // Initially hide views for animation
        logoCard.setScaleX(0f);
        logoCard.setScaleY(0f);
        logoCard.setAlpha(0f);
        ivLogo.setRotation(0f);
        tvAppName.setAlpha(0f);
        tvAppName.setTranslationY(50f);
        progressBar.setAlpha(0f);
    }

    private void startAnimations() {
        // Logo card animation - scale up with bounce
        ObjectAnimator scaleXLogo = ObjectAnimator.ofFloat(logoCard, "scaleX", 0f, 1f);
        ObjectAnimator scaleYLogo = ObjectAnimator.ofFloat(logoCard, "scaleY", 0f, 1f);
        ObjectAnimator alphaLogo = ObjectAnimator.ofFloat(logoCard, "alpha", 0f, 1f);

        scaleXLogo.setDuration(800);
        scaleYLogo.setDuration(800);
        alphaLogo.setDuration(800);

        scaleXLogo.setInterpolator(new OvershootInterpolator());
        scaleYLogo.setInterpolator(new OvershootInterpolator());

        AnimatorSet logoSet = new AnimatorSet();
        logoSet.playTogether(scaleXLogo, scaleYLogo, alphaLogo);
        logoSet.setStartDelay(200);
        logoSet.start();

        // Logo image rotation animation - subtle spin
        handler.postDelayed(() -> {
            ObjectAnimator rotateLogo = ObjectAnimator.ofFloat(ivLogo, "rotation", 0f, 360f);
            rotateLogo.setDuration(1000);
            rotateLogo.setInterpolator(new AccelerateDecelerateInterpolator());
            rotateLogo.start();
        }, 400);

        // App name animation - fade in and slide up
        handler.postDelayed(() -> {
            ObjectAnimator alphaName = ObjectAnimator.ofFloat(tvAppName, "alpha", 0f, 1f);
            ObjectAnimator translateName = ObjectAnimator.ofFloat(tvAppName, "translationY", 50f, 0f);

            alphaName.setDuration(600);
            translateName.setDuration(600);

            alphaName.setInterpolator(new AccelerateDecelerateInterpolator());
            translateName.setInterpolator(new AccelerateDecelerateInterpolator());

            AnimatorSet nameSet = new AnimatorSet();
            nameSet.playTogether(alphaName, translateName);
            nameSet.start();
        }, 800);

        // Progress bar animation - fade in
        handler.postDelayed(() -> {
            ObjectAnimator alphaProgress = ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 1f);
            alphaProgress.setDuration(400);
            alphaProgress.start();
        }, 1200);
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