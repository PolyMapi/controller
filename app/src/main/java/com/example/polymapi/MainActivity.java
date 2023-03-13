package com.example.polymapi;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.os.Bundle;

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

import java.util.ArrayList;
import java.util.Map;

import java.io.IOException;

import camera_module.CameraAPI;
import dbHandler.ImgPathObj;
import dbHandler.ImgRefObj;
import tasks.CaptureTask;
import tasks.DownloadTask;
import tasks.UploadTask;
import tasks.GpsTask;

import exif.ExifHandler;


import dbHandler.DbHandler;
import dbHandler.FeedReaderContract;
import dbHandler.FeedReaderDbHelper;
import tasks.GpsTask;

public class MainActivity extends AppCompatActivity {
    private Button captureButton;
    private Button downloadButton;
    private Button uploadButton;
    private Button clearDbButton;
    private boolean captureRunning = false;
    private boolean downloadRunning = false;
    private boolean uploadRunning = false;

    private CaptureTask captureTask;
    private GpsTask gpsTask;

    private DownloadTask downloadTask;

    FeedReaderDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get reference to the buttons
        captureButton = findViewById(R.id.capture);
        downloadButton = findViewById(R.id.download);
        uploadButton = findViewById(R.id.upload);
        clearDbButton = findViewById(R.id.clearDb);

        // Set the initial text of the button
        captureButton.setOnClickListener(view -> toggleCaptureMode());

        downloadButton.setOnClickListener(view -> toggleDownloadMode());

        uploadButton.setOnClickListener(view -> toggleUploadMode());

        clearDbButton.setOnClickListener(view -> clearDb());


        // DataBase setup
        dbHelper = new FeedReaderDbHelper(getApplicationContext());

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

            // TODO : check permissions and wifi connection to camera

            captureTask = new CaptureTask(0, dbHelper);
            captureTask.start();

            AskLocationPermission();
            gpsTask = new GpsTask(this, 0, dbHelper); // TODO : get the right capture id
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
            //TODO : check if interruption is possible
            downloadTask.interrupt();
        }
        else {
            downloadButton.setText(R.string.stop_download);

            downloadTask = new DownloadTask(this, dbHelper);
            downloadTask.start();
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

            ArrayList<ImgPathObj> imgPaths = DbHandler.getImgPathsData(dbHelper, 0);

            for (ImgPathObj imgPath : imgPaths) {
                try {
                    String latitude = ExifHandler.readLatitude(imgPath.imgPath, this);
                    String longitude = ExifHandler.readLongitude(imgPath.imgPath, this);

                    double[] coords = convert(latitude, longitude);
                    Log.d("coordinatesTest", "toggleUploadMode: " + imgPath.imgPath + ", lat : " + coords[0] + ", longitude : " + coords[1]);
                    // TODO : coordinates are wrong
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            }
        }
        uploadRunning = !uploadRunning;
    }

    public static double[] convert(String latitude, String longitude) {
        String[] latParts = latitude.split(",");
        String[] longParts = longitude.split(",");
        int[] latInts = new int[3];
        int[] longInts = new int[3];
        for (int i = 0; i < 3; i++) {
            latInts[i] = Integer.parseInt(latParts[i].split("/")[0]);
            longInts[i] = Integer.parseInt(longParts[i].split("/")[0]);
        }
        double lat = (double) latInts[0] + ((double) latInts[1] / 60.0) + ((double) latInts[2] / 3600.0);
        double longi = (double) longInts[0] + ((double) longInts[1] / 60.0) + ((double) longInts[2] / 3600.0);
        lat *= (latitude.contains("S") ? -1 : 1);
        longi *= (longitude.contains("W") ? -1 : 1);
        return new double[]{lat, longi};
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

    public void AskLocationPermission() {
        if (!hasLocationPermissions()) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showCustomDialog("Location Permission", "This app needs the location permission to track your location", "Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        multiplePermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
                    }
                }, "cancel", null);
            }else {
                multiplePermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            }
        }

    }



    public boolean hasLocationPermissions(){
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    void showCustomDialog(String title, String message,
                          String positiveBtnTitle, DialogInterface.OnClickListener positiveListener,
                          String negativeBtnTitle, DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveBtnTitle, positiveListener)
                .setNegativeButton(negativeBtnTitle, negativeListener);
        builder.create().show();
    }



    private ActivityResultLauncher<String[]> multiplePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
        @Override
        public void onActivityResult(Map<String, Boolean> result) {
            boolean finePermissionAllowed = false;
            if(result.get(Manifest.permission.ACCESS_FINE_LOCATION) != null) {
                finePermissionAllowed = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                if(!finePermissionAllowed) {
                    if(!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                        showCustomDialog("Location Permission", "Need fine location permission, allow it in the app settings", "GoTo Settings", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package" + BuildConfig.LIBRARY_PACKAGE_NAME));
                                startActivity(intent);
                            }
                        }, "cancel", null);
                    }
                }
            }
        }
    });

}