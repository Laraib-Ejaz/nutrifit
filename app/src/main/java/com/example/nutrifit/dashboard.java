package com.example.nutrifit;

import android.content.Intent;
import android.content.SharedPreferences; // Import SharedPreferences
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class dashboard extends AppCompatActivity {

    private TextView welcomeText;
    private CardView cardDiet, cardWorkout, cardSteps, cardWater, cardChat;
    private ImageView profileIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Views Initialize karein
        welcomeText = findViewById(R.id.welcomeText);
        cardDiet = findViewById(R.id.cardDiet);
        cardWorkout = findViewById(R.id.cardWorkout);
        cardSteps = findViewById(R.id.cardSteps);
        cardWater = findViewById(R.id.cardWater);
        cardChat = findViewById(R.id.cardChat);
        profileIcon = findViewById(R.id.profile_icon);

        // --- NAYA CODE: SHARED PREFERENCES SE DATA LOAD KARNA ---
        // Memory se latest BMI aur Status uthaein
        SharedPreferences sharedPref = getSharedPreferences("UserHealthData", MODE_PRIVATE);
        String bmi = sharedPref.getString("LAST_BMI", null);
        String status = sharedPref.getString("LAST_STATUS", null);

        // Agar memory khali hai toh Intent se check karein (Backwards compatibility)
        if (bmi == null) {
            bmi = getIntent().getStringExtra("BMI_SCORE");
            status = getIntent().getStringExtra("STATUS");
        }

        // Dashboard par latest data dikhayein
        if (bmi != null && status != null) {
            welcomeText.setText("Your BMI: " + bmi + " (" + status + ")");
        } else {
            welcomeText.setText("Welcome to NutriFit");
        }
        // -------------------------------------------------------

        // 3. Click Listeners
        if (profileIcon != null) {
            profileIcon.setOnClickListener(v -> {
                startActivity(new Intent(dashboard.this, ProfileActivity.class));
            });
        }

        // Final variables for the listener
        final String finalBmi = bmi;
        final String finalStatus = status;

        cardDiet.setOnClickListener(v -> {
            Intent intent = new Intent(dashboard.this, DietPlansActivity.class);
            // Latest data DietPlansActivity ko bhejein
            intent.putExtra("BMI_SCORE", finalBmi);
            intent.putExtra("STATUS", finalStatus);
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
}