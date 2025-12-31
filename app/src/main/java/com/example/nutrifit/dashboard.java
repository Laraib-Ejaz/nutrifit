package com.example.nutrifit;

import android.content.Intent;
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

        // 1. TextView, Cards aur ImageView ko initialize karein
        welcomeText = findViewById(R.id.welcomeText);
        cardDiet = findViewById(R.id.cardDiet);
        cardWorkout = findViewById(R.id.cardWorkout);
        cardSteps = findViewById(R.id.cardSteps);
        cardWater = findViewById(R.id.cardWater);
        cardChat = findViewById(R.id.cardChat);

        // YE LINE MISSING THI: profileIcon ko XML se connect karein
        profileIcon = findViewById(R.id.profile_icon);

        // 2. BMI Calculator se bheja gaya data receive karein
        String bmi = getIntent().getStringExtra("BMI_SCORE");
        String status = getIntent().getStringExtra("STATUS");

        if (bmi != null && status != null) {
            welcomeText.setText("Your BMI: " + bmi + " (" + status + ")");
        } else {
            welcomeText.setText("Welcome to NutriFit");
        }

        // 3. Click Listeners

        // Profile Icon par click (Ab ye crash nahi karega)
        if (profileIcon != null) {
            profileIcon.setOnClickListener(v -> {
                startActivity(new Intent(dashboard.this, ProfileActivity.class));
            });
        }

        cardDiet.setOnClickListener(v -> {
            startActivity(new Intent(dashboard.this, DietPlansActivity.class));
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