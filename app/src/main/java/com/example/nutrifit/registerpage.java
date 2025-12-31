package com.example.nutrifit;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class registerpage extends AppCompatActivity {

    EditText name, email, password;
    Button regBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerpage);

        name = findViewById(R.id.regName);
        email = findViewById(R.id.regEmail);
        password = findViewById(R.id.regPassword);
        regBtn = findViewById(R.id.registerBtn);

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uName = name.getText().toString();
                String uEmail = email.getText().toString();
                String uPass = password.getText().toString();

                if (uName.isEmpty() || uEmail.isEmpty() || uPass.isEmpty()) {
                    Toast.makeText(registerpage.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(registerpage.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    finish(); // Wapis Login page par le jayega
                }
            }
        });
    }
}