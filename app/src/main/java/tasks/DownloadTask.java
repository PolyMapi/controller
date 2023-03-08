package tasks;

import android.content.Context;
import android.util.Log;

import camera_module.CameraAPI;

public class DownloadTask extends Thread{
    String[] imageRefs;
    Context context;

    public DownloadTask(String[] imageRefs, Context context){
        this.imageRefs = imageRefs;
        this.context = context;
    }

    public void run(){
        long start = System.currentTimeMillis();

        CameraAPI.getInstance().downloadPictures(imageRefs, context);

        long stop = System.currentTimeMillis();
        long elapsed = (stop - start) / 1000;
        Log.d("task", "END");
        Log.d("task","Durée des downloads : " + elapsed + "s");
    }
}
