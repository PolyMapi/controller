package tasks;

import androidx.appcompat.app.AppCompatActivity;
import GPS_Manager.GPS_Manager;

public class GpsTask extends Thread {

    private final AppCompatActivity activity;

    public GpsTask(AppCompatActivity activity) {
        this.activity=activity;
    }
    @Override
    public void run() {
        GPS_Manager gps_manager = new GPS_Manager(activity);

        gps_manager.getLastLocation();



    }
}
