package com.example.nutrifit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth; // Firebase check ke liye

public class splashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 1. SharedPreferences se data lein
                SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
                boolean isLoggedIn = userSession.getBoolean("isLoggedIn", false);
                boolean hasCompletedBMI = userSession.getBoolean("hasCompletedBMI", false);

                // 2. Firebase se bhi confirm karein ke session active hai ya nahi
                boolean isFirebaseUserNull = (FirebaseAuth.getInstance().getCurrentUser() == null);

                Intent intent;

                // Case 1: Agar user logout ho chuka hai (ya pehli baar aya hai)
                if (!isLoggedIn || isFirebaseUserNull) {
                    intent = new Intent(splashscreen.this, Loginpage.class);
                }
                // Case 2: User logged in hai lekin logout ke baad dobara login kiya hai
                else if (!hasCompletedBMI) {
                    intent = new Intent(splashscreen.this, bmicalculation.class);
                }
                // Case 3: Sab set hai, direct Dashboard
                else {
                    intent = new Intent(splashscreen.this, dashboard.class);
                }

                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}