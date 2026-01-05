package com.example.nutrifit;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HydrationActivity extends AppCompatActivity {

    private ProgressBar waterProgress;
    private TextView tvPercentage, tvTarget;
    private ImageButton btnAddGlass;
    private LottieAnimationView lottieWaterDrop;
    private SharedPreferences sharedPreferences;
    private int currentWaterML = 0;
    private final int DAILY_TARGET = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hydration);

        waterProgress = findViewById(R.id.waterProgress);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvTarget = findViewById(R.id.tvTarget);
        btnAddGlass = findViewById(R.id.btnAddGlass);
        lottieWaterDrop = findViewById(R.id.lottieWaterDrop);

        // --- PROFILE SWITCHING FIX START ---
        // Pehle current logged-in user ki email ya ID uthayen jo aapne login ke waqt save ki hogi
        SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
        String currentUser = userSession.getString("userEmail", "default_user"); // 'userEmail' ko apni key se replace karein

        // Har user ki apni alag SharedPreferences file banegi
        sharedPreferences = getSharedPreferences("HydrationPrefs_" + currentUser, MODE_PRIVATE);
        // --- PROFILE SWITCHING FIX END ---

        checkAndResetDailyWater();
        updateUI();

        btnAddGlass.setOnClickListener(v -> {
            if (currentWaterML < DAILY_TARGET) {
                currentWaterML += 250;

                // Sound logic (optimized)
                playWaterSound();

                lottieWaterDrop.playAnimation();
                sharedPreferences.edit().putInt("todayWater", currentWaterML).apply();
                updateUI();
            } else {
                showCompletionDialog();
            }
        });
    }

    private void playWaterSound() {
        try {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.water);
            if (mp != null) {
                mp.start();
                mp.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAndResetDailyWater() {
        String lastSavedDate = sharedPreferences.getString("lastDate", "");
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (!currentDate.equals(lastSavedDate)) {
            currentWaterML = 0;
            sharedPreferences.edit()
                    .putInt("todayWater", 0)
                    .putString("lastDate", currentDate)
                    .apply();
        } else {
            currentWaterML = sharedPreferences.getInt("todayWater", 0);
        }
    }

    private void updateUI() {
        waterProgress.setMax(DAILY_TARGET);
        waterProgress.setProgress(currentWaterML);
        tvPercentage.setText(String.format(Locale.getDefault(), "%d / %d ml", currentWaterML, DAILY_TARGET));

        int glassesLeft = (DAILY_TARGET - currentWaterML) / 250;
        if (glassesLeft > 0) {
            tvTarget.setText("remaining: " + glassesLeft + " glasses");
        } else {
            tvTarget.setText("Target Achieved! 🎉");
        }
    }

    private void showCompletionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hydration Goal Met!")
                .setMessage("congrats, you have completed your drinking target.")
                .setPositiveButton("Thanks", null)
                .show();
    }
}