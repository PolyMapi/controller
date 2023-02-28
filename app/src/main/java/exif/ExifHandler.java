package exif;

import android.media.ExifInterface;

import java.io.IOException;

public class ExifHandler {

    public static String readDate(String fileName) throws IOException {


        // Load the image file into an ExifInterface object
        ExifInterface exifInterface = new ExifInterface(filePath);

        // Read an EXIF tag
        return exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
    }

    /*
    // Load the image file into an ExifInterface object
        ExifInterface exifInterface = new ExifInterface(filePath);

    // Write an EXIF tag
        exifInterface.setAttribute(ExifInterface.TAG_MAKE, "My Camera");

        // Save the changes to the image file
        exifInterface.saveAttributes();
     */
}
