package com.example.buddies.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.buddies.R;
import com.example.buddies.ViewModel.ViewModel;
import com.example.buddies.common.AppUtils;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Set splash activity, and add slide movement from bottom to top.
        ImageView splashIv = findViewById(R.id.splash_icon);
        // Glide.with(this).load(R.drawable.paw1_512px).into(splashIv);
        AppUtils.loadImageUsingGlide(
                this,
                AppUtils.getUriOfDrawable("paw1_512px", this),
                null,
                null,
                false,
                null,
                splashIv);
        Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splash);
        splashIv.setAnimation(rotate);

        // Show splash for 2.5 seconds, then move to MainActivity.
        // Also kill this activity using finish() to disable returning to it via back-press.
        new Thread() {
            @Override
            public void run() {
                try
                {
                    // Start Initializing the ViewModel and the Model
                    ViewModel.getInstance();

                    sleep(2500);
                }
                catch (InterruptedException e) { e.printStackTrace(); }
                finally {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }.start();

    }
}
