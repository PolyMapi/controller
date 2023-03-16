package com.example.polymapi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
import android.widget.TextView;
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
import tasks.DownloadCallback;
import tasks.DownloadTask;
import tasks.UploadTask;
import tasks.GpsTask;

import exif.ExifHandler;


import dbHandler.DbHandler;
import dbHandler.FeedReaderContract;
import dbHandler.FeedReaderDbHelper;
import tasks.GpsTask;

public class MainActivity extends AppCompatActivity  implements DownloadCallback {
    private Button captureButton;
    public Button downloadButton;
    private Button uploadButton;
    private Button clearDbButton;
    private boolean captureRunning = false;
    public boolean downloadRunning = false;
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

        updateTable();

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

    private void toggleCaptureMode() {
        checkModeIntegrity();
        if(downloadRunning || uploadRunning) {
            return;
        }
        if(captureRunning) { // stop capture
            captureButton.setText(R.string.start_capture);

            captureTask.interrupt();
            gpsTask.interrupt();
            updateTable();
        }
        else { // start capture

            captureButton.setText(R.string.stop_capture);

            int newCaptureId = DbHandler.getNewCaptureId(dbHelper);

            captureTask = new CaptureTask(newCaptureId, dbHelper);
            captureTask.start();

            AskLocationPermission();
            gpsTask = new GpsTask(this, newCaptureId, dbHelper);
            gpsTask.start();
        }
        captureRunning = !captureRunning;
    }

    private void toggleDownloadMode() {
        checkModeIntegrity();
        if(captureRunning || uploadRunning) {
            return;
        }
        if(!downloadRunning) {

            // disable button while downloading
            downloadButton.setText("downloading...");
            downloadButton.setEnabled(false);

            // start download
            downloadTask = new DownloadTask(this, dbHelper, this);
            downloadTask.start();

            downloadRunning = true;
        }

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
                    Log.d("Debug", "toggleUploadMode: " + imgPath.imgPath + ", lat : " + coords[0] + ", longitude : " + coords[1]);
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

    @Override
    public void onDownloadFinished() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                downloadButton.setText(R.string.start_download);
                downloadButton.setEnabled(true);

                downloadRunning = false;

                updateTable();
            }
        });
    }

    private void clearDb() {
        DbHandler.clearDb(dbHelper);
        updateTable();
    }

    private void addRow(int captureId, String state) {
        TableLayout tableLayout = findViewById(R.id.pending_capture);

        // Create a new TableRow
        TableRow row = new TableRow(this);

        // Create two new TextViews for the cells
        TextView captureIdTextView = new TextView(this);
        TextView stateTextView = new TextView(this);

        // Set the text content for the TextViews
        captureIdTextView.setText(String.valueOf(captureId));
        stateTextView.setText(state);

        // Add some padding to the TextViews for better readability
        captureIdTextView.setPadding(5, 5, 5, 5);
        stateTextView.setPadding(5, 5, 5, 5);

        // Add the TextViews to the TableRow
        row.addView(captureIdTextView);
        row.addView(stateTextView);

        // Add the TableRow to the TableLayout
        tableLayout.addView(row);
    }

    private void clearRows() {
        TableLayout tableLayout = findViewById(R.id.pending_capture);

        // get all children to delete
        ArrayList<View> childrenToDelete = new ArrayList<>();
        for (int i = 1; i < tableLayout.getChildCount(); i++) {
            if (tableLayout.getChildAt(i) instanceof TableRow) {
                childrenToDelete.add(tableLayout.getChildAt(i));
            }
        }

        // delete children
        for (View currentChild : childrenToDelete) {
            tableLayout.removeView(currentChild);
        }
    }

    private void updateTable() {
        clearRows();

        int[] imgRefCaptureIds = DbHandler.getImgRefsCaptureId(dbHelper);
        for (int imgRefCaptureId : imgRefCaptureIds) {
            addRow(imgRefCaptureId, "Download pending");
        }

        int[] imgPathCaptureIds = DbHandler.getImgPathsCaptureId(dbHelper);
        for (int imgPathCaptureId : imgPathCaptureIds) {
            addRow(imgPathCaptureId, "Upload pending");
        }
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