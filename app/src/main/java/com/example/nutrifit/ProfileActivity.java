package com.example.nutrifit;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private SwitchCompat hydrationSwitch;
    private Button btnSetWorkoutTime, btnLogout;
    private TextView txtWorkoutTime, userEmailText;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 1. Initialization
        hydrationSwitch = findViewById(R.id.hydrationSwitch);
        btnSetWorkoutTime = findViewById(R.id.btnSetWorkoutTime);
        btnLogout = findViewById(R.id.btnLogout);
        txtWorkoutTime = findViewById(R.id.txtWorkoutTime);
        userEmailText = findViewById(R.id.userEmailText);

        // Do alag SharedPreferences (Settings aur Health Data ke liye)
        sharedPreferences = getSharedPreferences("NutriFitPrefs", MODE_PRIVATE);

        // 2. Show Current User Email
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            userEmailText.setText("Email: " + email);
        }

        // 3. Load saved Hydration state
        boolean isHydrationOn = sharedPreferences.getBoolean("hydration_on", false);
        hydrationSwitch.setChecked(isHydrationOn);

        // Saved Workout Time load karein agar pehle se set hai
        String savedTime = sharedPreferences.getString("workout_time", "--:--");
        txtWorkoutTime.setText("Workout Time: " + savedTime);

        // 4. Hydration Switch Listener
        hydrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("hydration_on", isChecked).apply();
            String msg = isChecked ? "Hydration Reminders Enabled" : "Hydration Reminders Disabled";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // 5. Workout Time Picker
        btnSetWorkoutTime.setOnClickListener(v -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);

            TimePickerDialog mTimePicker = new TimePickerDialog(ProfileActivity.this, (view, hourOfDay, selectedMinute) -> {
                String time = String.format("%02d:%02d", hourOfDay, selectedMinute);
                txtWorkoutTime.setText("Workout Time: " + time);

                // Save this time
                sharedPreferences.edit().putString("workout_time", time).apply();

                // Call the alarm method
                setAlarm(hourOfDay, selectedMinute);

                Toast.makeText(this, "Workout reminder set for " + time, Toast.LENGTH_LONG).show();

            }, hour, minute, true); // 24 hour time
            mTimePicker.setTitle("Select Workout Time");
            mTimePicker.show();
        });

        // 6. Logout Logic (With Data Clearing)
        btnLogout.setOnClickListener(v -> {
            // A. Firebase Sign Out
            FirebaseAuth.getInstance().signOut();

            // B. Clear All SharedPreferences (BMI, Status, Settings sab khatam)
            // Taake naya user login kare toh usay purana data na dikhe
            getSharedPreferences("UserHealthData", MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("NutriFitPrefs", MODE_PRIVATE).edit().clear().apply();

            // C. Navigate to Login Page
            Intent intent = new Intent(ProfileActivity.this, Loginpage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });
    }

    private void setAlarm(int hour, int minute) {
        Intent intent = new Intent(ProfileActivity.this, NotificationReceiverActivi.class);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                this, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE | android.app.PendingIntent.FLAG_UPDATE_CURRENT);

        android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
        calendar.set(java.util.Calendar.MINUTE, minute);
        calendar.set(java.util.Calendar.SECOND, 0);

        if (calendar.before(java.util.Calendar.getInstance())) {
            calendar.add(java.util.Calendar.DATE, 1);
        }

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), pendingIntent);
        }
    }
}