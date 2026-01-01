package com.example.nutrifit;

import android.os.Bundle;
import android.util.Log; // Logcat ke liye
import android.widget.ImageView;
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

import org.json.JSONException;
import org.json.JSONObject; // JSON parsing ke liye

// Image loading library (Glide)
import com.bumptech.glide.Glide;

public class DietPlansActivity extends AppCompatActivity {

    private TextView dietTitle, txtBreakfast, txtLunch, txtSnacks, txtDinner;
    private ImageView imgBreakfast, imgLunch, imgSnacks, imgDinner; // Images ke liye
    private GenerativeModelFutures model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet_plans);

        // UI elements initialize karein
        dietTitle = findViewById(R.id.dietTitle);
        txtBreakfast = findViewById(R.id.txtBreakfast);
        imgBreakfast = findViewById(R.id.imgBreakfast);
        txtLunch = findViewById(R.id.txtLunch);
        imgLunch = findViewById(R.id.imgLunch);
        txtSnacks = findViewById(R.id.txtSnacks);
        imgSnacks = findViewById(R.id.imgSnacks);
        txtDinner = findViewById(R.id.txtDinner);
        imgDinner = findViewById(R.id.imgDinner);

        String bmi = getIntent().getStringExtra("BMI_SCORE");
        String status = getIntent().getStringExtra("STATUS");

        if (bmi == null) bmi = "22.0";
        if (status == null) status = "Normal";

        dietTitle.setText("Daily Diet for " + status + " BMI");

        // Gemini Model Setup
        GenerativeModel gm = new GenerativeModel("gemini-flash-latest", "AIzaSyBCKujOwgBK1bovPeknXZnEllkIPtYqrdU"); // Apni key yahan lagayein
        model = GenerativeModelFutures.from(gm);

        generateAIDietPlan(bmi, status);
    }

    private void generateAIDietPlan(String bmi, String status) {
        // Placeholder text set karein jab tak AI jawab na de
        txtBreakfast.setText("Generating...");
        txtLunch.setText("Generating...");
        txtSnacks.setText("Generating...");
        txtDinner.setText("Generating...");

        String prompt = "Act as a professional nutritionist for the 'NutriFit' app. " +
                "Generate a 1-day meal plan for an adult with a BMI of " + bmi + " (" + status + "). " +
                "Provide meal details for Breakfast, Lunch, Snacks, and Dinner. " +
                "For each meal, include: Meal Name, a brief Description, Calories, Carbs, Protein. " +
                "Ensure the plan is suitable for their BMI status (e.g., higher calories for underweight, balanced for healthy, lower calories for overweight). " +
                "Format the output as a JSON object with keys: 'breakfast', 'lunch', 'snacks', 'dinner'. " +
                "Each meal should be a JSON object with keys: 'name', 'description', 'calories', 'carbs', 'protein'. " +
                "DO NOT include any introductory or concluding remarks, only the JSON.";

        Content content = new Content.Builder().addText(prompt).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> {
                    String jsonResponse = result.getText();
                    Log.d("DIET_PLAN_RESPONSE", jsonResponse); // Console mein response dekhein

                    try {
                        JSONObject entirePlan = new JSONObject(jsonResponse);

                        // Breakfast
                        displayMeal(entirePlan.optJSONObject("breakfast"), txtBreakfast, imgBreakfast);
                        // Lunch
                        displayMeal(entirePlan.optJSONObject("lunch"), txtLunch, imgLunch);
                        // Snacks
                        displayMeal(entirePlan.optJSONObject("snacks"), txtSnacks, imgSnacks);
                        // Dinner
                        displayMeal(entirePlan.optJSONObject("dinner"), txtDinner, imgDinner);

                    } catch (JSONException e) {
                        Log.e("DIET_PLAN_PARSE_ERROR", "JSON parsing error: " + e.getMessage());
                        dietTitle.setText("Error: Couldn't parse plan.");
                        txtBreakfast.setText("Failed to load plan. (JSON Error)");
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    Log.e("DIET_PLAN_API_ERROR", "API call failed: " + t.getMessage());
                    dietTitle.setText("Error: Check internet/API key.");
                    txtBreakfast.setText("Failed to load diet plan: " + t.getMessage());
                });
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // Har meal ke data ko display karne aur image generate karne ka method
    private void displayMeal(JSONObject mealObject, TextView textView, ImageView imageView) {
        if (mealObject == null) {
            textView.setText("No plan available.");
            return;
        }

        String name = mealObject.optString("name", "N/A");
        String description = mealObject.optString("description", "");
        String calories = mealObject.optString("calories", "0 kcal");
        String carbs = mealObject.optString("carbs", "0g");
        String protein = mealObject.optString("protein", "0g");

        String mealDetails = name + "\n" +
                description + "\n" +
                "Calories: " + calories + " | Carbs: " + carbs + " | Protein: " + protein;
        textView.setText(mealDetails);

        // --- NAYA CODE: IMAGE GENERATION ---
        // Image generation ke liye ek alag AI model use karein ya fixed images
        // Agar aap Gemini se image generate karwana chahti hain, toh ye portion complicated hoga
        // Kyunke Gemini Image API alag se use karna padega.
        // Filhal, hum Google Image Search se link utha sakte hain ya fixed images use kar sakte hain.
        // For dynamic image from Gemini, you'd need a separate call to Gemini's vision model or an image generation API.
        // For simplicity, let's use a dummy image load or a placeholder.

        // Example with Glide (real image URL will come from an actual image API or search)
        String imageUrl = getImageUrlForMeal(name); // Ye method aapko banani hogi
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.dinner) // Loading image
                    .error(R.drawable.lunch)      // Error image
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.breakfast); // Default placeholder
        }
        // ------------------------------------
    }

    // Dummy method to get image URL (you need to implement real logic here)
    private String getImageUrlForMeal(String mealName) {
        // Yahan aap ek API call kar sakti hain jo food ki image search kare
        // Ya kuch predefined URLs return karein.
        // Example: if (mealName.contains("Oatmeal")) return "https://example.com/oatmeal.jpg";
        // Agar aap AI se image generate karwana chahti hain, toh Google ke Image Generation API ko alag se integrate karna hoga.

        // For now, returning null to show placeholder
        return null;
    }
}