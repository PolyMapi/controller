package com.example.polymapi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;

import exif.ExifHandler;

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
            // The following part is a test of the ExifHandler class
            String path = "/storage/emulated/0/Pictures/IMG_20230303_102701.jpg";
            String res1;
            String res2;
            String res3;
            try {
                res1 = ExifHandler.readDate(path, this);
                res2 = ExifHandler.readLongitude(path, this);
                res3 = ExifHandler.readLatitude(path, this);

                Log.d("res1: ", res1);
                Log.d("res2: ", res2);
                Log.d("res3: ", res3);

                // This is an example of location given by ChatGPT, but you can replace it by a string if you wish so
                Location location = new Location("");
                location.setLatitude(37.807620);
                String latitude = Location.convert(location.getLatitude(), Location.FORMAT_SECONDS);

                ExifHandler.writeLatitude(path, this, latitude);
                ExifHandler.writeDate(path, this, "2022:01:01 01:01:10");
                ExifHandler.writeLongitude(path, this, "-122/1,15/1,54606/1000");

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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