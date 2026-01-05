package com.example.nutrifit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class Loginpage extends AppCompatActivity {
    private EditText loginEmail, loginPassword;
    private TextView btnLogin, btnRegisterNow;
    private FirebaseAuth mAuth;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage);

        mAuth = FirebaseAuth.getInstance();

        loginEmail = findViewById(R.id.editEmail);
        loginPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegisterNow = findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            String pass = loginPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Details fill karein", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase Login
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    // --- SESSION SAVING ---
                    SharedPreferences userSession = getSharedPreferences("UserSession", MODE_PRIVATE);
                    SharedPreferences.Editor editor = userSession.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("userEmail", email);

                    // Note: Yahan hum hasCompletedBMI ko true nahi kar rahe,
                    // kyunke agar naya user hai toh use BMI page par jana chahiye.
                    // Splash screen khud hi check kar legi ke purane user ka data hai ya nahi.
                    editor.apply();

                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();

                    // Yahan se Splash screen wali logic repeat hogi
                    boolean hasCompletedBMI = userSession.getBoolean("hasCompletedBMI", false);
                    if (hasCompletedBMI) {
                        startActivity(new Intent(this, dashboard.class));
                    } else {
                        startActivity(new Intent(this, bmicalculation.class));
                    }

                    finish();
                } else {
                    Toast.makeText(this, "Login Failed! Email/Password check karein", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnRegisterNow.setOnClickListener(v -> {
            startActivity(new Intent(this, registerpage.class));
        });
    }
}