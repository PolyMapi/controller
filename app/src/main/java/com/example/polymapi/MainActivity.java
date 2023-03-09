package com.example.polymapi;

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

import androidx.appcompat.app.AppCompatActivity;

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

    private CaptureTask captureTask;
    private GpsTask gpsTask;

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

            captureTask.interrupt();
            gpsTask.interrupt();
        }
        else { // start tour

            tourButton.setText(R.string.stop_tour);

            captureTask = new CaptureTask();
            captureTask.start();

            gpsTask = new GpsTask();
            gpsTask.start();
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