package com.example.nutrifit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class registerpage extends AppCompatActivity {
    private EditText editEmail, editPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerpage);

        mAuth = FirebaseAuth.getInstance();

        // Apni XML ki IDs check karein (email_input, password_input, register_button)
        editEmail = findViewById(R.id.regEmail);
        editPassword = findViewById(R.id.regPassword);
        btnRegister = findViewById(R.id.registerBtn);

        btnRegister.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String pass = editPassword.getText().toString().trim();

            if (email.isEmpty() || pass.length() < 6) {
                Toast.makeText(this, "Email likhein aur password 6 digits ka ho", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase User Create karna
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "User Registered Successfully!", Toast.LENGTH_SHORT).show();
                    // Registration ke baad Login page par bhejna
                    startActivity(new Intent(this, Loginpage.class));
                    finish();
                } else {
                    Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}