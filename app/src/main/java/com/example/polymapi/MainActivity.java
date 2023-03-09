package com.example.polymapi;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import tasks.CaptureTask;
import tasks.DownloadTask;
import tasks.UploadTask;
import tasks.GpsTask;

import exif.ExifHandler;

import dbHandler.DbHandler;
import dbHandler.FeedReaderContract;
import dbHandler.FeedReaderDbHelper;




public class MainActivity extends AppCompatActivity {
    private Button captureButton;
    private Button downloadButton;
    private Button uploadButton;
    private Button clearDbButter;
    private boolean captureRunning = false;
    private boolean downloadRunning = false;
    private boolean uploadRunning = false;

    private CaptureTask captureTask;
    private GpsTask gpsTask;

    FeedReaderDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get reference to the buttons
        captureButton = findViewById(R.id.capture);
        downloadButton = findViewById(R.id.download);
        uploadButton = findViewById(R.id.upload);
        clearDbButter = findViewById(R.id.clearDb);

        // Set the initial text of the button
        captureButton.setOnClickListener(view -> toggleCaptureMode());

        downloadButton.setOnClickListener(view -> toggleDownloadMode());

        uploadButton.setOnClickListener(view -> toggleUploadMode());

        clearDbButter.setOnClickListener(view -> clearDb());

        // DataBase setup
        dbHelper = new FeedReaderDbHelper(getApplicationContext());

        /////TASK TESTS/////

        //CaptureTask cTask = new CaptureTask();
        //cTask.start();

       /* String[] imageRefs = new String[5];
        imageRefs[0] = "0011012";
        imageRefs[1] = "0011013";
        imageRefs[2] = "0011014";
        imageRefs[3] = "0011015";
        imageRefs[4] = "0011016";

        DownloadTask dTask = new DownloadTask(getApplicationContext(), imageRefs);
        dTask.start();*/

        /*String[] imagePaths = new String[5];
        imagePaths[0] = "/data/user/0/com.example.polymapi/files/pictures/R0011012.JPG";
        imagePaths[1] = "/data/user/0/com.example.polymapi/files/pictures/R0011013.JPG";
        imagePaths[2] = "/data/user/0/com.example.polymapi/files/pictures/R0011014.JPG";
        imagePaths[3] = "/data/user/0/com.example.polymapi/files/pictures/R0011015.JPG";
        imagePaths[4] = "/data/user/0/com.example.polymapi/files/pictures/R0011016.JPG";
        UploadTask uTask = new UploadTask(getApplicationContext()); //
        uTask.start();*/

    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }


    private void checkModeIntegrity() {
        if ((captureRunning ? 1 : 0) + (downloadRunning ? 1 : 0) + (uploadRunning ? 1 : 0) > 1) {
            throw new RuntimeException("Should never happen");
        }
    }
    /**
     * Updates the text of the Hello button.
     *
     */
    private void toggleCaptureMode() {
        checkModeIntegrity();
        if(downloadRunning || uploadRunning) {
            return;
        }
        if(captureRunning) { // stop capture
            captureButton.setText(R.string.start_capture);

            captureTask.interrupt();
            gpsTask.interrupt();
        }
        else { // start capture

            captureButton.setText(R.string.stop_capture);

            captureTask = new CaptureTask();
            captureTask.start();

            gpsTask = new GpsTask();
            gpsTask.start();
        }
        captureRunning = !captureRunning;
    }

    private void toggleDownloadMode() {
        checkModeIntegrity();
        if(captureRunning || uploadRunning) {
            return;
        }
        if(downloadRunning) {
            downloadButton.setText(R.string.start_download);
        }
        else {
            downloadButton.setText(R.string.stop_download);
        }
        downloadRunning = !downloadRunning;
    }

    private void toggleUploadMode() {
        checkModeIntegrity();
        if(captureRunning || downloadRunning) {
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
        DbHandler.clearDb(dbHelper);
    }

    private void addPendingCapture(View capture) {
        // Get a reference to your table layout
        TableLayout myLayout = findViewById(R.id.pending_capture);

        // Create a new table row
        TableRow row = new TableRow(this);

        // Add the View to the row
        row.addView(capture);

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