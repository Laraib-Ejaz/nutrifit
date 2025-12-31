package com.example.nutrifit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class loginpage extends AppCompatActivity {

    // 1. UI Components declare karein
    private EditText emailBox, passwordBox;
    private Button loginBtn;
    private TextView registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage); // Apni XML file ka sahi naam check karein

        // 2. XML Views ko Java variables se connect karein
        emailBox = findViewById(R.id.email);
        passwordBox = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        registerLink = findViewById(R.id.registerText);

        // 3. Login Button ki Logic
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailBox.getText().toString().trim();
                String pass = passwordBox.getText().toString().trim();

                // Validation: Check karein fields khali to nahi
                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(loginpage.this, "Email aur Password lazmi likhein!", Toast.LENGTH_SHORT).show();
                }
                // Abhi ke liye simple check (Baad mein isay Firebase se connect karenge)
                else if (email.equals("admin@gmail.com") && pass.equals("12345")) {
                    Toast.makeText(loginpage.this, "Welcome to Nutrifit!", Toast.LENGTH_SHORT).show();

                    // Login ke baad BMI Calculator page par bhejna
                    Intent intent = new Intent(loginpage.this, BmiCalculatorActivity.class);
                    startActivity(intent);
                    finish(); // User back kare to dobara login na dikhaye
                } else {
                    Toast.makeText(loginpage.this, "Ghalat Email ya Password!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 4. Register Text ki Logic (Naye page par janay ke liye)
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(loginpage.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}