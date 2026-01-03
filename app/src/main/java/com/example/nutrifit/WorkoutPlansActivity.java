package com.example.nutrifit;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class WorkoutPlansActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WorkoutAdapter adapter;
    private List<WorkoutModel> workoutList = new ArrayList<>();
    private GenerativeModelFutures model;
    private LottieAnimationView lottieLoading;
    private TextView titleMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_plans);

        titleMain = findViewById(R.id.workoutTitleMain);
        lottieLoading = findViewById(R.id.lottieLoading);
        recyclerView = findViewById(R.id.workoutRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkoutAdapter(workoutList);
        recyclerView.setAdapter(adapter);

        // Get Data from Intent
        String bmi = getIntent().getStringExtra("BMI_SCORE");
        String status = getIntent().getStringExtra("STATUS");
        titleMain.setText("Workout for " + (status != null ? status : "Health"));

        // Setup Gemini AI
        GenerativeModel gm = new GenerativeModel("gemini-flash-latest", "your api key");
        model = GenerativeModelFutures.from(gm);

        generateDynamicWorkout(bmi, status);
    }

    private void generateDynamicWorkout(String bmi, String status) {
        String prompt = "Act as a fitness trainer. Give a workout for BMI " + bmi + " (" + status + "). " +
                "Choose from: Squats, Deadlifts, Presses, Rows, Walking, Swimming, Cycling, Lunges, Jumping Jacks, Yoga, Running. " +
                "Format as JSON: {\"list\": [{\"name\": \"Yoga\", \"desc\": \"15 mins\"}]}";

        Content content = new Content.Builder().addText(prompt).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                runOnUiThread(() -> {
                    try {
                        String jsonStr = result.getText().replaceAll("```json|```", "").trim();
                        JSONObject jsonObject = new JSONObject(jsonStr);
                        JSONArray array = jsonObject.getJSONArray("list");

                        workoutList.clear();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            workoutList.add(new WorkoutModel(obj.getString("name"), obj.getString("desc")));
                        }

                        lottieLoading.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) { showFallback(); }
                });
            }
            @Override
            public void onFailure(Throwable t) { runOnUiThread(() -> showFallback()); }
        }, ContextCompat.getMainExecutor(this));
    }

    private void showFallback() {
        workoutList.add(new WorkoutModel("Walking", "20 mins"));
        lottieLoading.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    // --- Adapter & Logic ---
    class WorkoutModel {
        String name, desc;
        WorkoutModel(String name, String desc) { this.name = name; this.desc = desc; }
    }

    class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder> {
        List<WorkoutModel> list;
        WorkoutAdapter(List<WorkoutModel> list) { this.list = list; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WorkoutModel item = list.get(position);
            holder.name.setText(item.name);
            holder.desc.setText(item.desc);

            // GIF Load from local drawables
            int resId = getExerciseDrawable(item.name);
            Glide.with(WorkoutPlansActivity.this).asGif().load(resId).into(holder.gif);

            // Timer Logic
            holder.tvTimer.setOnClickListener(v -> {
                new CountDownTimer(30000, 1000) {
                    public void onTick(long ms) {
                        holder.tvTimer.setText(ms / 1000 + "s");
                        holder.tvTimer.setTextColor(android.graphics.Color.RED);
                    }
                    public void onFinish() {
                        holder.tvTimer.setText("DONE!");
                        holder.tvTimer.setTextColor(android.graphics.Color.GREEN);
                        // Vibrate mobile
                        Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        if (vib != null) vib.vibrate(500);
                    }
                }.start();
            });
        }

        private int getExerciseDrawable(String name) {
            String n = name.toLowerCase();
            if (n.contains("squat")) return R.drawable.squats;
            if (n.contains("deadlift")) return R.drawable.deadlift;
            if (n.contains("walking")) return R.drawable.walking;
            if (n.contains("running")) return R.drawable.running;
            if (n.contains("yoga")) return R.drawable.yoga_;
            if (n.contains("jack")) return R.drawable.jumping_jack;
            if (n.contains("swimming")) return R.drawable.swimming;
            if (n.contains("water arobics")) return R.drawable.water_arobics;
            if (n.contains("wallpushup")) return R.drawable.wallpushup;
            if (n.contains("row")) return R.drawable.row;
            if (n.contains("press")) return R.drawable.press;
            // Add more as per your download list
            return R.drawable.walking; // default
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, desc, tvTimer; ImageView gif;
            ViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.workoutName);
                desc = v.findViewById(R.id.workoutDesc);
                tvTimer = v.findViewById(R.id.tvTimer);
                gif = v.findViewById(R.id.workoutGif);
            }
        }
    }
}