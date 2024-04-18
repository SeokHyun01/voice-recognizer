package com.darcy.voicerecognizer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 1000;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

        Button main_btn = findViewById(R.id.main_button);
        main_btn.setOnClickListener(view -> {
            startDialogSequence();
        });
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET},
                    PERMISSIONS_REQUEST_CODE);
        } else {
            // Permissions are already granted
            Log.d(TAG, "Permissions already granted.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d(TAG, "Permissions granted.");
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied. The app may not work correctly.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startDialogSequence() {
        List<String> prompts = Arrays.asList("첫 번째 질문을 시작합니다.", "두 번째 질문입니다.", "마지막 질문이에요.");
        List<Consumer<String>> handlers = Arrays.asList(
                response -> Toast.makeText(this, "First response: " + response, Toast.LENGTH_SHORT).show(),
                response -> Toast.makeText(this, "Second response: " + response, Toast.LENGTH_SHORT).show(),
                response -> Toast.makeText(this, "Final response: " + response, Toast.LENGTH_SHORT).show()
        );

        DialogManager dialogManager = new DialogManager(this, prompts, handlers);
        dialogManager.start();
    }
}
