package exif;

import android.Manifest;
import android.content.pm.PackageManager;
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
        return checkReadingPermission(fileName, mainActivity).getAttribute(ExifInterface.TAG_GPS_LATITUDE);
    }

    public static void writeDate(String fileName, MainActivity mainActivity, String value) throws IOException {
        ExifInterface exifInterface = checkWritingPermission(fileName, mainActivity);
        exifInterface.setAttribute(ExifInterface.TAG_DATETIME, value);
        exifInterface.saveAttributes();
        Log.d("Date: ", readDate(fileName, mainActivity));
    }

    public static void writeLongitude(String fileName, MainActivity mainActivity, String value) throws IOException {
        ExifInterface exifInterface = checkWritingPermission(fileName, mainActivity);
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, value);
        exifInterface.saveAttributes();
        Log.d("Longitude: ", readLongitude(fileName, mainActivity));
    }

    public static void writeLatitude(String fileName, MainActivity mainActivity, String value) throws IOException {
        ExifInterface exifInterface = checkWritingPermission(fileName, mainActivity);
        exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, value);
        exifInterface.saveAttributes();
        Log.d("Latitude: ", readLatitude(fileName, mainActivity));
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
}
