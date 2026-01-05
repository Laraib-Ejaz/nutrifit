package com.example.nutrifit;

import android.app.AlarmManager;
import android.app.PendingIntent;
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
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private SwitchCompat hydrationSwitch;
    private Button btnSetWorkoutTime, btnLogout;
    private TextView txtWorkoutTime, userEmailText;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 1. Initialization (Using "UserSession" to match your other activities)
        hydrationSwitch = findViewById(R.id.hydrationSwitch);
        btnSetWorkoutTime = findViewById(R.id.btnSetWorkoutTime);
        btnLogout = findViewById(R.id.btnLogout);
        txtWorkoutTime = findViewById(R.id.txtWorkoutTime);
        userEmailText = findViewById(R.id.userEmailText);

        // Hum "UserSession" file use kar rahe hain taake flags sahi load hon
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // 2. User Info (Firebase se current user ki email lena)
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userEmailText.setText("Email: " + FirebaseAuth.getInstance().getCurrentUser().getEmail());
        }

        // 3. Load Saved States
        hydrationSwitch.setChecked(sharedPreferences.getBoolean("hydration_on", false));
        txtWorkoutTime.setText("Workout Time: " + sharedPreferences.getString("workout_time", "--:--"));

        // 4. Hydration Switch Logic
        hydrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("hydration_on", isChecked).apply();
            if (isChecked) {
                startHydrationReminders();
                Toast.makeText(this, "Reminders Every 2 Hours", Toast.LENGTH_SHORT).show();
            } else {
                stopHydrationReminders();
                Toast.makeText(this, "Reminders Disabled", Toast.LENGTH_SHORT).show();
            }
        });

        // 5. Workout Time Picker Logic
        btnSetWorkoutTime.setOnClickListener(v -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);

            TimePickerDialog mTimePicker = new TimePickerDialog(ProfileActivity.this, (view, hourOfDay, selectedMinute) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, selectedMinute);
                txtWorkoutTime.setText("Workout Time: " + time);
                sharedPreferences.edit().putString("workout_time", time).apply();
                setWorkoutAlarm(hourOfDay, selectedMinute);
            }, hour, minute, true);
            mTimePicker.show();
        });

        // 6. LOGOUT LOGIC (Updated for your requirements)
        btnLogout.setOnClickListener(v -> {
            // A. Firebase se Sign Out
            FirebaseAuth.getInstance().signOut();

            // B. SharedPreferences Flags Update
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", false);    // Splash ab login par bhejega
            editor.putBoolean("hasCompletedBMI", false); // Login ke baad BMI page maangega
            // Note: Hum 'userEmail' clear nahi kar rahe taake login ke baad history mil sake
            editor.apply();

            // C. Background Reminders stop karna
            stopHydrationReminders();

            Toast.makeText(ProfileActivity.this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();

            // D. Activity Stack clear karke Login page par bhejna
            Intent intent = new Intent(ProfileActivity.this, Loginpage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // --- Hydration & Alarm Methods ---

    private void startHydrationReminders() {
        // Yahan humne class ka naam HydrationReceiver kar diya hai
        Intent intent = new Intent(this, HydrationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // 2 minute = 2 * 60 * 1000 milliseconds
        long interval = 2 * 60 * 1000;
        long triggerTime = System.currentTimeMillis() + interval;

        if (alarmManager != null) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerTime, interval, pendingIntent);
        }
    }

    private void stopHydrationReminders() {
        Intent intent = new Intent(this, HydrationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    private void setWorkoutAlarm(int hour, int minute) {
        Intent intent = new Intent(this, NotificationReceiverActivi.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Agar time guzar gaya ho toh kal ke liye set karein
        if (calendar.before(Calendar.getInstance())) calendar.add(Calendar.DATE, 1);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }
}