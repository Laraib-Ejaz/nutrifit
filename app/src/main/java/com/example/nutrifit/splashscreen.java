package com.example.nutrifit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class splashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // LoginActivity tab tak error dega jab tak aap file bana nahi leti
                Intent intent = new Intent(splashscreen.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}