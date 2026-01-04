package com.example.nutrifit;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONObject;

public class DietPlansActivity extends AppCompatActivity {

    private TextView dietTitle, txtBreakfast, txtLunch, txtSnacks, txtDinner;
    private ImageView imgBreakfast, imgLunch, imgSnacks, imgDinner;
    private GenerativeModelFutures model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet_plans);

        // UI Binding
        dietTitle = findViewById(R.id.dietTitle);
        txtBreakfast = findViewById(R.id.txtBreakfast);
        txtLunch = findViewById(R.id.txtLunch);
        txtSnacks = findViewById(R.id.txtSnacks);
        txtDinner = findViewById(R.id.txtDinner);

        imgBreakfast = findViewById(R.id.imgBreakfast);
        imgLunch = findViewById(R.id.imgLunch);
        imgSnacks = findViewById(R.id.imgSnacks);
        imgDinner = findViewById(R.id.imgDinner);

        String bmi = getIntent().getStringExtra("BMI_SCORE");
        String status = getIntent().getStringExtra("STATUS");

        if (bmi == null) bmi = "22.0";
        if (status == null) status = "Healthy";

        dietTitle.setText("Daily Diet Plan (" + status + ")");

        // Gemini Setup
        GenerativeModel gm = new GenerativeModel("gemini-flash-latest", "your api key");
        model = GenerativeModelFutures.from(gm);

        generateAIDietPlan(bmi, status);
    }

    private void generateAIDietPlan(String bmi, String status) {
        // Prompt updated for simplified meal names to help image search
        String prompt = "Act as a nutritionist. Create a 1-day diet plan for BMI " + bmi + " (" + status + "). " +
                "Use VERY SHORT meal names (max 2 words) for 'name' keys. " +
                "Respond ONLY with raw JSON: {" +
                "\"bf_name\": \"Oatmeal Fruit\", \"bf_details\": \"Oatmeal with fresh fruits (350 kcal)\", " +
                "\"ln_name\": \"Grilled Chicken\", \"ln_details\": \"Chicken breast with salad (450 kcal)\", " +
                "\"sn_name\": \"Apple Nuts\", \"sn_details\": \"One apple and handful of almonds (150 kcal)\", " +
                "\"dn_name\": \"Baked Fish\", \"dn_details\": \"Baked fish with steamed veggies (400 kcal)\"}";

        Content content = new Content.Builder().addText(prompt).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> {
                    try {
                        String jsonString = result.getText().replace("```json", "").replace("```", "").trim();
                        JSONObject json = new JSONObject(jsonString);

                        // Breakfast
                        txtBreakfast.setText(json.optString("bf_details"));
                        loadMealImage(json.optString("bf_name"), imgBreakfast);

                        // Lunch
                        txtLunch.setText(json.optString("ln_details"));
                        loadMealImage(json.optString("ln_name"), imgLunch);

                        // Snacks
                        txtSnacks.setText(json.optString("sn_details"));
                        loadMealImage(json.optString("sn_name"), imgSnacks);

                        // Dinner - Fixed key and forcing image load
                        txtDinner.setText(json.optString("dn_details"));
                        loadMealImage(json.optString("dn_name"), imgDinner);

                    } catch (Exception e) {
                        Log.e("JSON_ERROR", "Error: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> txtBreakfast.setText("API Error: " + t.getMessage()));
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void loadMealImage(String mealName, ImageView imageView) {
        // Query cleaning: Spaces ki jagah comma aur "food" keyword add kiya
        String query = mealName.toLowerCase().trim().replaceAll("\\s+", ",");

        // Stable URL: LoremFlickr 404 nahi deta aur relevant images dikhata hai
        String imageUrl = "https://loremflickr.com/800/600/food,healthy," + query;

        Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache enable kiya taake bar bar load na ho
                .centerCrop()
                .placeholder(android.R.drawable.progress_horizontal)
                .error(android.R.drawable.ic_menu_gallery)
                .into(imageView);
    }
}
