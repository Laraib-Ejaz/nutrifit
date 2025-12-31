package com.example.nutrifit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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

        // Views ko initialize karna
        editAge = findViewById(R.id.editAge);
        editWeight = findViewById(R.id.editWeight);
        editHeight = findViewById(R.id.editHeight);
        btnCalculate = findViewById(R.id.btnCalculate);

        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processBmiAndNavigate();
            }
        });
    }

    private void processBmiAndNavigate() {
        String ageStr = editAge.getText().toString();
        String weightStr = editWeight.getText().toString();
        String heightStr = editHeight.getText().toString();

        // 1. Validation: Check empty fields
        if (ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty() || genderGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please fill all details and select gender", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Calculation
        float weight = Float.parseFloat(weightStr);
        float heightCm = Float.parseFloat(heightStr);
        float heightM = heightCm / 100;
        float bmiValue = weight / (heightM * heightM);

        // 3. Status aur Diet Plan determine karna
        String status = getBmiStatus(bmiValue);
        String dietPlan = getRecommendedDiet(status);

        // 4. Automatic Navigation to Dashboard
        Intent intent = new Intent(bmicalculation.this, DashboardActivity.class);

        // Data aglay page ke liye pack karna
        intent.putExtra("BMI_SCORE", String.format("%.1f", bmiValue));
        intent.putExtra("STATUS", status);
        intent.putExtra("DIET", dietPlan);
        intent.putExtra("AGE", ageStr);

        startActivity(intent);
        finish(); // Wapis calculator par na aanay ke liye
    }

    private String getBmiStatus(float bmi) {
        if (bmi < 18.5) return "Underweight";
        else if (bmi >= 18.5 && bmi <= 24.9) return "Healthy Weight";
        else if (bmi >= 25 && bmi <= 29.9) return "Overweight";
        else return "Obese";
    }

    private String getRecommendedDiet(String status) {
        switch (status) {
            case "Underweight": return "Focus on calorie-dense foods, proteins, and healthy fats.";
            case "Healthy Weight": return "Maintain a balanced diet with fruits, veggies, and lean protein.";
            case "Overweight": return "Low carb, high fiber diet. Control portion sizes.";
            default: return "Strict calorie deficit, avoid sugar, and increase fiber intake.";
        }
    }
}