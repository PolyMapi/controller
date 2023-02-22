package com.example.polymapi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button tourButton;
    private Button uploadButton;
    private boolean tourRunning = false;
    private boolean uploadRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get reference to the buttons
        tourButton = findViewById(R.id.tour);
        uploadButton = findViewById(R.id.upload);

        // Set the initial text of the button
        tourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleTourMode();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleUploadMode();
            }
        });
    }

    /**
     * Updates the text of the Hello button.
     *
     */
    private void toggleTourMode() {
        if(tourRunning && uploadRunning) {
            throw new RuntimeException("Should never happen");
        }
        if(uploadRunning) {
            return;
        }
        if(tourRunning) {
            tourButton.setText("Start tour");
        }
        else {
            tourButton.setText("Stop tour");
        }
        tourRunning = !tourRunning;
    }

    private void toggleUploadMode() {
        if(tourRunning && uploadRunning) {
            throw new RuntimeException("Should never happen");
        }
        if(tourRunning) {
            return;
        }
        if(uploadRunning) {
            uploadButton.setText("Start tour");
        }
        else {
            uploadButton.setText("Stop tour");
        }
        uploadRunning = !uploadRunning;

    }

}