package tasks;


import android.location.Location;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import GPS_Manager.GPS_Manager;
import GPS_Manager.LocationCallback;
import dbHandler.DbHandler;
import dbHandler.FeedReaderDbHelper;

public class GpsTask extends Thread implements LocationCallback {

    private AppCompatActivity activity;
    private int captureId;
    private FeedReaderDbHelper dbHelper;

    public GpsTask(AppCompatActivity activity, int captureId, FeedReaderDbHelper dbHelper) {
        this.activity=activity;
        this.captureId = captureId;
        this.dbHelper = dbHelper;
    }

    @Override
    public void run() {

        GPS_Manager gps_manager = new GPS_Manager(activity, this);

        while(!isInterrupted()) {

            gps_manager.getLastLocation();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.d("task", "Interruption");
            }
        }
    }

    @Override
    public void onLocationReceived(Location location) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
        String dateTime = dateFormat.format(new Date(location.getTime()));

        DbHandler.addCoordinates(dbHelper, captureId, location.getLatitude(), location.getLongitude(), dateTime);
    }
}
