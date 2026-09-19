package com.example.snakegame;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OptionsActivity extends AppCompatActivity {

    private RadioGroup rgGridSize;
    private RadioGroup rgSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        rgSpeed = findViewById(R.id.rgSpeed);
        Button btnSave = findViewById(R.id.btnSaveOptions);
        Button btnAtras = findViewById(R.id.btnAtrasOptions);

        SharedPreferences prefs = getSharedPreferences("SnakePrefs", MODE_PRIVATE);

        int currentGrid = prefs.getInt("grid_size", 20);
        long currentSpeed = prefs.getLong("speed_delay", 180);


        if (currentSpeed == 250) rgSpeed.check(R.id.rbSpeedSlow);
        else if (currentSpeed == 100) rgSpeed.check(R.id.rbSpeedFast);
        else rgSpeed.check(R.id.rbSpeedNormal);

        btnSave.setOnClickListener(v -> {

            long selectedSpeed = 180;
            int checkedSpeedId = rgSpeed.getCheckedRadioButtonId();
            if (checkedSpeedId == R.id.rbSpeedSlow) selectedSpeed = 250;
            else if (checkedSpeedId == R.id.rbSpeedFast) selectedSpeed = 100;

            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("speed_delay", selectedSpeed);
            editor.apply();

            Toast.makeText(this, "Opciones guardadas", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnAtras.setOnClickListener(v -> finish());
    }
}