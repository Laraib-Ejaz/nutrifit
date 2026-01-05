package com.example.nutrifit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class bmicalculation extends AppCompatActivity {

    private EditText editAge, editWeight, editHeight;
    private RadioGroup genderGroup;
    private Button btnCalculate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmicalcultion);

        editAge = findViewById(R.id.editAge);
        editWeight = findViewById(R.id.editWeight);
        editHeight = findViewById(R.id.editHeight);
        genderGroup = findViewById(R.id.genderGroup);
        btnCalculate = findViewById(R.id.btnCalculate);

        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processBmiAndNavigate();
            }
        });
    }

    private void processBmiAndNavigate() {
        String ageStr = editAge.getText().toString().trim();
        String weightStr = editWeight.getText().toString().trim();
        String heightStr = editHeight.getText().toString().trim();

        if (ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty() || genderGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please fill all details and select gender", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float weight = Float.parseFloat(weightStr);
            float heightCm = Float.parseFloat(heightStr);
            float heightM = heightCm / 100;
            float bmiValue = weight / (heightM * heightM);

            String status = getBmiStatus(bmiValue);
            String bmiFormatted = String.format(Locale.getDefault(), "%.1f", bmiValue);

            // --- SESSION MANAGEMENT & DATA SAVING ---
            // Yahan hum save kar rahe hain ke user ne BMI process complete kar liya hai
            SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
            SharedPreferences.Editor editor = userSession.edit();

            editor.putBoolean("hasCompletedBMI", true); // Splash screen ab direct Dashboard bhejegi
            editor.putString("LAST_BMI", bmiFormatted);
            editor.putString("LAST_STATUS", status);
            editor.apply();
            // -----------------------------------------

            // Dashboard par data bhejte waqt Intent ka istemal
            Intent intent = new Intent(bmicalculation.this, dashboard.class);
            intent.putExtra("BMI_SCORE", bmiFormatted);
            intent.putExtra("STATUS", status);

            startActivity(intent);
            finish(); // Activity finish karna zaroori hai taake user back karke yahan na aa sake

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private String getBmiStatus(float bmi) {
        if (bmi < 18.5) return "Underweight";
        else if (bmi < 25) return "Healthy Weight";
        else if (bmi < 30) return "Overweight";
        else return "Obese";
    }
}