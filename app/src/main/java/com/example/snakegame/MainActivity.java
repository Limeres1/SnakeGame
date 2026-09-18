package com.example.snakegame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnOptions = findViewById(R.id.btnOptions);

        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });

        btnOptions.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OptionsActivity.class);
            startActivity(intent);
        });
    }
}