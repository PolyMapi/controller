package com.example.polymapi;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.os.Bundle;

import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.library.baseAdapters.BuildConfig;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.polymapi.databinding.ActivityMainBinding;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Map;

import java.io.IOException;

import tasks.DownloadTask;
import exif.ExifHandler;

import tasks.CaptureTask;


import dbHandler.FeedReaderContract;
import dbHandler.FeedReaderDbHelper;
import tasks.GpsTask;


public class MainActivity extends AppCompatActivity {
    private Button tourButton;
    private Button uploadButton;
    private Button clearDbButter;
    private boolean tourRunning = false;
    private boolean uploadRunning = false;


/*    ActivityMainBinding binding;*/
    private FusedLocationProviderClient fusedLocationClient;

    FeedReaderDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get reference to the buttons
        tourButton = findViewById(R.id.tour);
        uploadButton = findViewById(R.id.upload);
        clearDbButter = findViewById(R.id.clearDb);

        // Set the initial text of the button
        tourButton.setOnClickListener(view -> toggleTourMode());

        uploadButton.setOnClickListener(view -> toggleUploadMode());

/*        binding= ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());*/


        clearDbButter.setOnClickListener(view -> clearDb());

        // DataBase setup
        dbHelper = new FeedReaderDbHelper(getApplicationContext());

        /*
        CaptureTask cTask = new CaptureTask();
        cTask.start();
        String[] imageRefs = new String[5];
        imageRefs[0] = "00";
        imageRefs[1] = "00";
        imageRefs[2] = "00";
        imageRefs[3] = "00";
        imageRefs[4] = "00";


        DownloadTask dTask = new DownloadTask(imageRefs , getApplicationContext());
         */
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
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
        if(tourRunning) { // stop tour
            tourButton.setText(R.string.start_tour);
        }
        else { // start tour
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
            GpsTask gpsTask = new GpsTask(this);
            gpsTask.start();

            uploadButton.setText(R.string.stop_upload);
        }
        uploadRunning = !uploadRunning;

    }

    private void clearDb() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.clearDb(db);
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

    private void databaseTest() {
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Clear the table by deleting all rows
        int rowsDeleted = db.delete(FeedReaderContract.ImgRefsEntry.TABLE_NAME, null, null);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.ImgRefsEntry.COLUMN_NAME_CAPTURE_ID, 7);
        values.put(FeedReaderContract.ImgRefsEntry.COLUMN_NAME_REF, "1234567");

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.ImgRefsEntry.TABLE_NAME, null, values);

        Cursor cursor = db.query(
                FeedReaderContract.ImgRefsEntry.TABLE_NAME,   // The table to query
                null,                                         // The array of columns to return (pass null to get all)
                null,                                         // The columns for the WHERE clause
                null,                                         // The values for the WHERE clause
                null,                                         // don't group the rows
                null,                                         // don't filter by row groups
                null                                          // The sort order
        );

        cursor.moveToNext();
        int capture_Id = cursor.getInt(cursor.getColumnIndexOrThrow(FeedReaderContract.ImgRefsEntry.COLUMN_NAME_CAPTURE_ID));
        cursor.close();

    }

}