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

import java.util.ArrayList;
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
        List<String> prompts = Arrays.asList("어떤 물건을 찾으시나요?", null, "마지막 질문이에요.");
        DialogManager dialogManager = new DialogManager(this, new ArrayList<>(prompts), null);
        List<Consumer<String>> handlers = Arrays.asList(
                response -> {
                    int currentPromptIndex = dialogManager.getCurrentPromptIndex();
                    dialogManager.updatePrompt(currentPromptIndex + 1, response + " 맞나요?");
                    dialogManager.next();
                },
                response -> {
                    Toast.makeText(this, response, Toast.LENGTH_SHORT).show();

                    if (response.equals("예")) {
                        dialogManager.next();
                    } else {
                        dialogManager.previous();
                    }
                },
                response -> {
                    Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
                    dialogManager.next();
                }
        );
        dialogManager.setResponseHandlers(handlers);
        dialogManager.start();
    }
}
