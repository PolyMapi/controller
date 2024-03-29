package exif;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.ExifInterface;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.polymapi.MainActivity;

import java.io.IOException;

public class ExifHandler {

    private static final int PERMISSION_REQUEST_CODE = 25565;
    public static String readDate(String fileName, MainActivity mainActivity) throws IOException {
        return checkReadingPermission(fileName, mainActivity).getAttribute(ExifInterface.TAG_DATETIME);
    }

    public static String readLongitude(String fileName, MainActivity mainActivity) throws IOException {
        return checkReadingPermission(fileName, mainActivity).getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
    }

    public static String readLatitude(String fileName, MainActivity mainActivity) throws IOException {
        // return checkReadingPermission(fileName, mainActivity).getAttribute(ExifInterface.TAG_GPS_LATITUDE);

        ExifInterface exif = checkReadingPermission(fileName, mainActivity);
        String latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        return latitude;
    }

    public static void writeDate(String fileName, MainActivity mainActivity, String value) throws IOException {
        ExifInterface exifInterface = checkWritingPermission(fileName, mainActivity);
        exifInterface.setAttribute(ExifInterface.TAG_DATETIME, value);
        exifInterface.saveAttributes();
    }

    public static void writeLongitude(String fileName, MainActivity mainActivity, double value) throws IOException {
        ExifInterface exifInterface = checkWritingPermission(fileName, mainActivity);
        String dmsLongitude = decimalToDms(value);
        Log.d("Debug", "writeLongitude: dms : " + dmsLongitude);
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, dmsLongitude);
        exifInterface.saveAttributes();
    }

    public static void writeLatitude(String fileName, MainActivity mainActivity, double value) throws IOException {
        ExifInterface exifInterface = checkWritingPermission(fileName, mainActivity);
        String dmsLatitude = decimalToDms(value);
        Log.d("Debug", "writeLatitude: dms : " + dmsLatitude);
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, dmsLatitude);
        exifInterface.saveAttributes();
    }

    private static ExifInterface checkWritingPermission (String fileName, MainActivity mainActivity) throws IOException {
        if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted

            // Load the image file into an ExifInterface object
            return new ExifInterface(fileName);
        }
        throw new RuntimeException("Permission not granted");
    }
    private static ExifInterface checkReadingPermission(String fileName, MainActivity mainActivity) throws IOException {
    if (ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(mainActivity,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    } else {
            // Permission already granted

            // Load the image file into an ExifInterface object
            return new ExifInterface(fileName);
    }
        throw new RuntimeException("Permission not granted");
    }

    private static String decimalToDms(double decimalDegrees) {
        /*int degrees = (int) decimalDegrees;
        decimalDegrees = Math.abs(decimalDegrees - degrees) * 60;
        int minutes = (int) decimalDegrees;
        decimalDegrees = (decimalDegrees - minutes) * 60;
        int seconds = (int) (decimalDegrees * 3600);
        return degrees + "/1," + minutes + "/1," + seconds + "/1000";*/

        int d = (int)decimalDegrees;
        double tmp = (decimalDegrees - d) * 60;
        int m = (int) tmp;
        double s = (tmp - m) * 60;

        return d + "/1," + m + "/1," + s + "/1000";
    }
}
