package com.example.polymapi;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.appcompat.app.AppCompatActivity;

import camera_module.Task;

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
        tourButton.setOnClickListener(view -> toggleTourMode());

        uploadButton.setOnClickListener(view -> toggleUploadMode());

        Task task = new Task();
        task.start();
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
            tourButton.setText(R.string.start_tour);
        }
        else {
            tourButton.setText(R.string.stop_tour);
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
            uploadButton.setText(R.string.start_upload);
        }
        else {
            uploadButton.setText(R.string.stop_upload);
        }
        uploadRunning = !uploadRunning;

    }

    private void addPendingTour(View tour) {
        // Get a reference to your table layout
        TableLayout myLayout = findViewById(R.id.pending_tours);

        // Create a new table row
        TableRow row = new TableRow(this);

        // Add the View to the row
        row.addView(tour);

        // Add the row to the table layout
        myLayout.addView(row);
    }

}