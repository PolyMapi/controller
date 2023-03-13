package tasks;

import android.content.Context;
import android.util.Log;

import com.example.polymapi.MainActivity;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import camera_module.CameraAPI;
import dbHandler.CoordinatesObj;
import dbHandler.DbHandler;
import dbHandler.FeedReaderDbHelper;
import dbHandler.ImgRefObj;
import exif.ExifHandler;

public class DownloadTask extends Thread {
    MainActivity activity;

    FeedReaderDbHelper dbHelper;

    public DownloadTask(MainActivity activity, FeedReaderDbHelper dbHelper){
        this.activity = activity;
        this.dbHelper = dbHelper;
    }

    public void run(){

        // find a capture ID in both tables
        int captureId = getCaptureId();
        if (captureId < 0) { // TODO : manage error case
            throw new RuntimeException("Capture id can't be negative");
        }

        // Download all the images from that capture ID and store the paths in an Array
        ArrayList<ImgRefObj> imgRefs = DbHandler.getImgRefsData(dbHelper, captureId);
        ArrayList<String> imgPaths = new ArrayList<>();
        for (ImgRefObj imgRef : imgRefs) {
            imgPaths.add(CameraAPI.getInstance().downloadPicture(imgRef.imgRef, activity.getApplicationContext()));
        }

        // Get all the coordinates and store them in an Array
        ArrayList<CoordinatesObj> coordinates = DbHandler.getCoordinatesData(dbHelper, captureId);

        // add geotag to all images
        for (String imgPath : imgPaths) {
            try {
                String currentImgTimestamp = ExifHandler.readDate(imgPath, activity);

                CoordinatesObj currentCoordinates = findClosestCoordinatesObj(coordinates, currentImgTimestamp);

                if (currentCoordinates != null) {
                    ExifHandler.writeLatitude(imgPath, activity, currentCoordinates.latitude);
                    ExifHandler.writeLongitude(imgPath, activity, currentCoordinates.longitude);
                } else {
                    // TODO : Error case. is it even possible ?
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // save paths into table
        for (String imgPath : imgPaths) {
            DbHandler.addImgPath(dbHelper, captureId, imgPath);
        }

        // clean up the other 2 tables
        DbHandler.deleteImgRefsByCaptureId(dbHelper, captureId);
        DbHandler.deleteCoordinatesByCaptureId(dbHelper, captureId);

    }

    private int getCaptureId() {
        int[] imgRefCaptureIds = DbHandler.getImgRefsCaptureId(dbHelper);
        int[] coordinatesCaptureIds = DbHandler.getCoordinatesCaptureId(dbHelper);

        int lowest = Integer.MAX_VALUE;

        for (int i = 0; i < imgRefCaptureIds.length; i++) {
            for (int j = 0; j < coordinatesCaptureIds.length; j++) {
                if (imgRefCaptureIds[i] == coordinatesCaptureIds[j] && imgRefCaptureIds[i] < lowest) {
                    lowest = imgRefCaptureIds[i];
                }
            }
        }

        if (lowest == Integer.MAX_VALUE) {
            return -1;
        } else {
            return lowest;
        }
    }

    public static CoordinatesObj findClosestCoordinatesObj(ArrayList<CoordinatesObj> coordinates, String referenceTimestamp) {

        // Convert reference timestamp to Date object
        Date referenceDate;
        try {
            referenceDate = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(referenceTimestamp);
        } catch (ParseException e) {
            // Handle parse error
            return null;
        }

        // Convert all timestamps to Date objects and sort the list
        ArrayList<Date> dates = new ArrayList<>();
        for (CoordinatesObj coords : coordinates) {
            try {
                dates.add(new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").parse(coords.timestamp));
            } catch (ParseException e) {
                // Handle parse error
            }
        }
        Collections.sort(dates);

        // Find the index of the reference timestamp (if present) or the index where it should be inserted
        int index = Collections.binarySearch(dates, referenceDate);
        if (index < 0) {
            index = -index - 1;
        }

        // Determine the closest timestamp to the reference timestamp
        Date closestDate;
        if (index == 0) {
            closestDate = dates.get(0);
        } else if (index == dates.size()) {
            closestDate = dates.get(dates.size() - 1);
        } else {
            Date prevDate = dates.get(index - 1);
            Date nextDate = dates.get(index);
            if (referenceDate.getTime() - prevDate.getTime() < nextDate.getTime() - referenceDate.getTime()) {
                closestDate = prevDate;
            } else {
                closestDate = nextDate;
            }
        }

        // Find the CoordinatesObj object with the closest timestamp
        for (CoordinatesObj coords : coordinates) {
            if (coords.timestamp.equals(new SimpleDateFormat("yyyy:MM:dd HH:mm:ss").format(closestDate))) {
                return coords;
            }
        }

        // Handle not finding a matching CoordinatesObj object
        return null;
    }
}
