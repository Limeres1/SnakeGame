package com.example.snakegame;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SkinActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin);

        RadioGroup rgSkinColor = findViewById(R.id.rgSkinColor);
        Button btnSave = findViewById(R.id.btnSaveSkinOptions);

        // Cargar el color guardado actualmente (o verde por defecto)
        SharedPreferences prefs = getSharedPreferences("SnakePrefs", Context.MODE_PRIVATE);
        int currentColor = prefs.getInt("snake_color", Color.GREEN);

        // Seleccionar la opción correspondiente
        if (currentColor == Color.GREEN) {
            rgSkinColor.check(R.id.GreenSkinColor);
        } else if (currentColor == Color.BLUE) {
            rgSkinColor.check(R.id.BlueSkinColor);
        } else if (currentColor == Color.WHITE) {
            rgSkinColor.check(R.id.WhiteSkinColor);
        } else if (currentColor == Color.RED) {
            rgSkinColor.check(R.id.RedSkinColor);
        } else {
            rgSkinColor.check(R.id.GreenSkinColor);
        }

        btnSave.setOnClickListener(v -> {
            // Determinar qué color eligió el usuario
            int selectedColor;
            int checkedId = rgSkinColor.getCheckedRadioButtonId();

            if (checkedId == R.id.GreenSkinColor) {
                selectedColor = Color.GREEN;
            } else if (checkedId == R.id.BlueSkinColor) {
                selectedColor = Color.BLUE;
            } else if (checkedId == R.id.WhiteSkinColor) {
                selectedColor = Color.WHITE;
            } else if (checkedId == R.id.RedSkinColor) {
                selectedColor = Color.RED;
            } else {
                selectedColor = Color.GREEN;
            }

            // Guardarlo en las preferencias
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("snake_color", selectedColor);
            editor.apply();

            Toast.makeText(this, "Skin guardada", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}