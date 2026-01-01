package com.example.nutrifit;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class DietPlansActivity extends AppCompatActivity {

    private TextView dietResultText, statusTitle;
    private GenerativeModelFutures model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet_plans);

        dietResultText = findViewById(R.id.dietResultText);
        statusTitle = findViewById(R.id.dietTitle);

        String bmi = getIntent().getStringExtra("BMI_SCORE");
        String status = getIntent().getStringExtra("STATUS");

        if (bmi == null) bmi = "22.0";
        if (status == null) status = "Normal";

        statusTitle.setText("Diet Plan for " + status + " BMI");

        // Model name check: "gemini-1.5-flash-latest" ya "gemini-pro" use karein
        GenerativeModel gm = new GenerativeModel("gemini-flash-latest", "AIzaSyD3rmG6_TcdGAC00gRzJhkik-SN735DjPA");
        model = GenerativeModelFutures.from(gm);

        generateAIDietPlan(bmi, status);
    }

    private void generateAIDietPlan(String bmi, String status) {
        dietResultText.setText("Creating your personalized diet plan...");

        // String literals ko + sign ke sath likhein taake line end error na aaye
        String prompt = "Act as a professional nutritionist. Create a 1-day meal plan for someone with a BMI of " + bmi +
                " which is '" + status + "'. " +
                "Suggest Breakfast, Lunch, and Dinner. " +
                "For each meal, provide: Meal Name, Calories, Carbs, and Protein. " +
                "Focus on the user's BMI category for food choices.";

        Content content = new Content.Builder().addText(prompt).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> {
                    String output = result.getText();
                    if (output != null) {
                        dietResultText.setText(output);
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    dietResultText.setText("Error: " + t.getLocalizedMessage());
                });
            }
        }, ContextCompat.getMainExecutor(this));
    }
}