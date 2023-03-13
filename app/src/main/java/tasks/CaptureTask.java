package tasks;

import android.util.Log;

import java.util.ArrayList;

import camera_module.CameraAPI;
import dbHandler.DbHandler;
import dbHandler.FeedReaderDbHelper;

public class CaptureTask extends Thread {

    private int currentCaptureId;
    private FeedReaderDbHelper dbHelper;

    public CaptureTask(int currentCaptureId, FeedReaderDbHelper dbHelper) {
        this.currentCaptureId = currentCaptureId;
        this.dbHelper = dbHelper;
    }

    public void run() {
        while (!interrupted()) {
            String imgRef = CameraAPI.getInstance().takePicture();
            DbHandler.addImgRef(dbHelper, currentCaptureId, imgRef);

            try {
                Thread.sleep(4500);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
