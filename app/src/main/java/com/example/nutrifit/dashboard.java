package com.example.nutrifit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class dashboard extends AppCompatActivity {

    private TextView welcomeText;
    private CardView cardDiet, cardWorkout, cardSteps, cardWater, cardChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. TextView aur Cards ko initialize karein
        welcomeText = findViewById(R.id.welcomeText);
        cardDiet = findViewById(R.id.cardDiet);
        cardWorkout = findViewById(R.id.cardWorkout);
        cardSteps = findViewById(R.id.cardSteps);
        cardWater = findViewById(R.id.cardWater);
        cardChat = findViewById(R.id.cardChat);

        // 2. BMI Calculator se bheja gaya data receive karein
        String bmi = getIntent().getStringExtra("BMI_SCORE");
        String status = getIntent().getStringExtra("STATUS");

        // Agar data aaya hai to welcome text mein dikhayein
        if (bmi != null && status != null) {
            welcomeText.setText("Your BMI: " + bmi + " (" + status + ")");
        } else {
            welcomeText.setText("Welcome to NutriFit");
        }

        // 3. Click Listeners: Cards par click karne se aglay pages khulenge

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