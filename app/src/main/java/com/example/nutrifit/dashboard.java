package com.example.nutrifit;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class dashboard extends AppCompatActivity {

    private TextView welcomeText;
    private CardView cardDiet, cardWorkout, cardSteps, cardWater, cardChat;
    private ImageView profileIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Notification Permission Request (Android 13+ ke liye)
        checkNotificationPermission();

        // 2. Views Initialize
        welcomeText = findViewById(R.id.welcomeText);
        cardDiet = findViewById(R.id.cardDiet);
        cardWorkout = findViewById(R.id.cardWorkout);
        cardSteps = findViewById(R.id.cardSteps);
        cardWater = findViewById(R.id.cardWater);
        cardChat = findViewById(R.id.cardChat);
        profileIcon = findViewById(R.id.profile_icon);

        // --- SHARED PREFERENCES SE DATA LOAD KARNA ---
        SharedPreferences sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE);
        String bmi = sharedPref.getString("LAST_BMI", null);
        String status = sharedPref.getString("LAST_STATUS", null);

        if (bmi != null && status != null) {
            welcomeText.setText("Your BMI: " + bmi + " (" + status + ")");
        } else {
            welcomeText.setText("Welcome to NutriFit");
        }

        // 3. Profile Icon Listener
        if (profileIcon != null) {
            profileIcon.setOnClickListener(v -> {
                startActivity(new Intent(dashboard.this, ProfileActivity.class));
            });
        }

        // 4. Card Click Listeners
        cardDiet.setOnClickListener(v -> {
            Intent intent = new Intent(dashboard.this, DietPlansActivity.class);
            intent.putExtra("BMI_SCORE", bmi);
            intent.putExtra("STATUS", status);
            startActivity(intent);
        });

        cardWorkout.setOnClickListener(v -> {
            startActivity(new Intent(dashboard.this, WorkoutPlansActivity.class));
        });

        cardSteps.setOnClickListener(v -> {
            startActivity(new Intent(dashboard.this, StepCounterActivity.class));
        });

        cardWater.setOnClickListener(v -> {
            startActivity(new Intent(dashboard.this, HydrationActivity.class));
        });

        cardChat.setOnClickListener(v -> {
            startActivity(new Intent(dashboard.this, ChatBotActivity.class));
        });
    }

    // Naya Method: Notification Permission mangne ke liye
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}