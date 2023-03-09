package tasks;

import android.content.Context;
import android.util.Log;

import camera_module.CameraAPI;

public class DownloadTask extends Thread{
    String[] imageRefs;
    Context context;

    public DownloadTask(Context context, String[] imageRefs){
        this.context = context;
        this.imageRefs = imageRefs;
    }

    public void run(){
        long start = System.currentTimeMillis();

        String[] imagePaths = CameraAPI.getInstance().downloadPictures(imageRefs, context);


        long stop = System.currentTimeMillis();
        long elapsed = (stop - start) / 1000;
        Log.d("task", "END");
        Log.d("task","Durée des downloads : " + elapsed + "s");
    }
}
