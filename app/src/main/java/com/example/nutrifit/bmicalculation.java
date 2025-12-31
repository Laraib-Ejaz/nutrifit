package com.example.nutrifit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class bmicalculation extends AppCompatActivity {

    private EditText editAge, editWeight, editHeight;
    private RadioGroup genderGroup;
    private Button btnCalculate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmicalcultion);

        // Views ko initialize karna (Ab crash nahi hoga)
        editAge = findViewById(R.id.editAge);
        editWeight = findViewById(R.id.editWeight);
        editHeight = findViewById(R.id.editHeight);
        genderGroup = findViewById(R.id.genderGroup); // FIX: Pehle ye missing tha
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

        // 1. Validation: Check empty fields aur RadioGroup selection
        if (ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty() || genderGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please fill all details and select gender", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 2. Calculation Logic
            float weight = Float.parseFloat(weightStr);
            float heightCm = Float.parseFloat(heightStr);
            float heightM = heightCm / 100;
            float bmiValue = weight / (heightM * heightM);

            // 3. Status aur Diet Plan determine karna
            String status = getBmiStatus(bmiValue);
            String dietPlan = getRecommendedDiet(status);

            // 4. Navigation to Dashboard with Data
            Intent intent = new Intent(bmicalculation.this, dashboard.class);
            intent.putExtra("BMI_SCORE", String.format("%.1f", bmiValue));
            intent.putExtra("STATUS", status);
            intent.putExtra("DIET", dietPlan);

            startActivity(intent);
            finish();

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

    private String getRecommendedDiet(String status) {
        switch (status) {
            case "Underweight": return "Focus on calorie-dense foods and proteins.";
            case "Healthy Weight": return "Maintain a balanced diet with veggies and protein.";
            case "Overweight": return "Low carb, high fiber diet. Control portions.";
            default: return "Strict calorie deficit and increase fiber intake.";
        }
    }
}