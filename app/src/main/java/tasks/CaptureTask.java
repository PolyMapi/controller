package tasks;

import android.util.Log;

import java.util.ArrayList;

import camera_module.CameraAPI;
import dbHandler.DbHandler;
import dbHandler.FeedReaderDbHelper;

public class CaptureTask extends Thread {

    private int curentCaptureId;
    private FeedReaderDbHelper dbHelper;

    public CaptureTask(int curentCaptureId, FeedReaderDbHelper dbHelper) {
        this.curentCaptureId = curentCaptureId;
        this.dbHelper = dbHelper;
    }

    public void run(){
        long start = System.currentTimeMillis();

        ArrayList<String> imageRefs = new ArrayList<>();
        while(!isInterrupted()) {
            imageRefs.add(CameraAPI.getInstance().takePicture());

            try {
                Thread.sleep(4500);
            } catch (InterruptedException e) {
                Log.d("task", "Interruption");
            }
        }

        for(String ref : imageRefs){
            DbHandler.addImgRef(dbHelper, curentCaptureId, ref);
        }

        long stop = System.currentTimeMillis();
        long elapsed = (stop - start) / 1000;
        Log.d("task", "END");
        Log.d("task","Durée des captures : " + elapsed + "s");
    }
}
