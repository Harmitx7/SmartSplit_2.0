package com.smartsplit.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.smartsplit.app.R;
import com.smartsplit.app.ui.motion.IosMotion;
import com.smartsplit.app.ui.auth.AuthActivity;

/**
 * Entry point activity.
 * Shows for 1.5 seconds, then routes to:
 *   - AuthActivity if not logged in
 *   - MainActivity if already authenticated
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        playSplashMotion();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            Class<?> destination;

            if (auth.getCurrentUser() != null) {
                destination = MainActivity.class;
            } else {
                destination = AuthActivity.class;
            }

            startActivity(new Intent(SplashActivity.this, destination));
            finish();
        }, 1500);
    }

    private void playSplashMotion() {
        IosMotion.animateIn(findViewById(R.id.tv_splash_icon), 0);
        IosMotion.animateIn(findViewById(R.id.tv_splash_title), 70);
        IosMotion.animateIn(findViewById(R.id.tv_splash_subtitle), 120);
        IosMotion.animateIn(findViewById(R.id.progressBar), 190);
    }
}
