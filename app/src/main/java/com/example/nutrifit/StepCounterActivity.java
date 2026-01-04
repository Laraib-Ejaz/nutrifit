package com.example.nutrifit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView tvSteps, tvCalories, tvDistance;
    private Button btnShare;
    private SharedPreferences sharedPreferences;

    private boolean isSensorPresent = false;
    private String lastSavedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        // UI Binding
        tvSteps = findViewById(R.id.tvSteps);
        tvCalories = findViewById(R.id.tvCalories);
        tvDistance = findViewById(R.id.tvDistance);
        btnShare = findViewById(R.id.btnShare);

        sharedPreferences = getSharedPreferences("StepCounterPrefs", MODE_PRIVATE);
        lastSavedDate = sharedPreferences.getString("lastDate", "");

        // Permission mangna (Android 10+)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 100);
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            isSensorPresent = true;
        } else {
            Toast.makeText(this, "Aapke phone mein Step Counter sensor nahi hai!", Toast.LENGTH_LONG).show();
        }

        checkDateChange();

        btnShare.setOnClickListener(v -> shareProgress());
    }

    private void checkDateChange() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Agar din badal gaya hai
        if (!currentDate.equals(lastSavedDate) && !lastSavedDate.equals("")) {
            showDailyReport();

            // Aaj ke steps ko reset karne ke liye current hardware value ko offset bana dein
            float currentTotalHardwareSteps = sharedPreferences.getFloat("currentHardwareTotal", 0);
            sharedPreferences.edit()
                    .putFloat("stepsOnReset", currentTotalHardwareSteps)
                    .putString("lastDate", currentDate)
                    .apply();
        } else if (lastSavedDate.equals("")) {
            sharedPreferences.edit().putString("lastDate", currentDate).apply();
        }
    }

    private void showDailyReport() {
        float lastSteps = sharedPreferences.getFloat("todaySteps", 0);
        float lastCal = lastSteps * 0.04f;

        new AlertDialog.Builder(this)
                .setTitle("Daily Summary 📊")
                .setMessage("Kal aapne itna kaam kiya:\n👣 " + (int)lastSteps + " Steps\n🔥 " +
                        String.format("%.2f", lastCal) + " kcal")
                .setPositiveButton("Shuru Karein!", null)
                .show();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            float totalHardwareSteps = event.values[0]; // Ye hardware ka total hai

            // Save current hardware total for date change logic
            sharedPreferences.edit().putFloat("currentHardwareTotal", totalHardwareSteps).apply();

            // Offset logic: Total mein se purane steps minus karein
            float offset = sharedPreferences.getFloat("stepsOnReset", -1);

            if (offset == -1) {
                // Pehli baar app chalne par offset set karein
                offset = totalHardwareSteps;
                sharedPreferences.edit().putFloat("stepsOnReset", offset).apply();
            }

            float todaySteps = totalHardwareSteps - offset;
            if (todaySteps < 0) todaySteps = 0; // Reboot protection

            // Calculations
            float calories = todaySteps * 0.04f;
            float distanceKm = todaySteps / 1312f;

            // Display update
            tvSteps.setText(String.valueOf((int)todaySteps));
            tvCalories.setText(String.format("%.2f kcal", calories));
            tvDistance.setText(String.format("%.2f km", distanceKm));

            // Aaj ke steps save karein report ke liye
            sharedPreferences.edit().putFloat("todaySteps", todaySteps).apply();
        }
    }

    private void shareProgress() {
        String msg = "🔥 NutriFit: Aaj maine " + tvSteps.getText().toString() + " steps liye aur " +
                tvCalories.getText().toString() + " burn kiye!";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSensorPresent) {
            Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}