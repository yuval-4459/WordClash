package com.example.wordclash.screens;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wordclash.R;
import com.example.wordclash.models.User;
import com.example.wordclash.services.DatabaseService;
import com.example.wordclash.utils.SharedPreferencesUtils;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000;
    private final Handler handler = new Handler();
    private ImageView ivLogo;
    private TextView tvAppName;
    private TextView tvTagline;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initializeViews();
        startAnimations();
        checkAuthentication();
    }

    private void initializeViews() {
        ivLogo = findViewById(R.id.ivLogo);
        tvAppName = findViewById(R.id.tvAppName);
        tvTagline = findViewById(R.id.tvTagline);
        progressBar = findViewById(R.id.progressBar);

        // initially hide for animation entrance
        ivLogo.setScaleX(0.5f);
        ivLogo.setScaleY(0.5f);
        ivLogo.setAlpha(0f);

        tvTagline.setAlpha(0f);
        tvTagline.setTranslationY(50f);

        progressBar.setAlpha(0f);
    }

    private void startAnimations() {
        // Logo: scale up and fade in with bounce
        ObjectAnimator scaleXLogo = ObjectAnimator.ofFloat(ivLogo, "scaleX", 0.5f, 1f);
        ObjectAnimator scaleYLogo = ObjectAnimator.ofFloat(ivLogo, "scaleY", 0.5f, 1f);
        ObjectAnimator alphaLogo = ObjectAnimator.ofFloat(ivLogo, "alpha", 0f, 1f);

        scaleXLogo.setDuration(900);
        scaleYLogo.setDuration(900);
        alphaLogo.setDuration(900);

        scaleXLogo.setInterpolator(new BounceInterpolator());
        scaleYLogo.setInterpolator(new BounceInterpolator());

        AnimatorSet logoSet = new AnimatorSet();
        logoSet.playTogether(scaleXLogo, scaleYLogo, alphaLogo);
        logoSet.setStartDelay(200);
        logoSet.start();

        // subtle pulse after logo appears
        handler.postDelayed(this::startLogoPulseAnimation, 1200);

        // progress bar fade in
        handler.postDelayed(() -> {
            ObjectAnimator alphaProgress = ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 1f);
            alphaProgress.setDuration(600);
            alphaProgress.start();
        }, 1000);

        // tagline slide up and fade in
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
        }, 1400);
    }

    private void startLogoPulseAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivLogo, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivLogo, "scaleY", 1f, 1.05f, 1f);

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
                User oldUser = SharedPreferencesUtils.getUser(SplashActivity.this);

                DatabaseService.getInstance().getUser(oldUser.getId(), new DatabaseService.DatabaseCallback<>() {
                    @Override
                    public void onCompleted(User newUser) {
                        if (newUser == null) {
                            SharedPreferencesUtils.signOutUser(SplashActivity.this);
                            navigateToStart();
                        } else {
                            SharedPreferencesUtils.saveUser(SplashActivity.this, newUser);
                            navigateToMain();
                        }
                    }

                    @Override
                    public void onFailed(Exception e) {
                        navigateToMain();
                    }
                });
            } else {
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
        Intent intent = new Intent(SplashActivity.this, LandingActivity.class);
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