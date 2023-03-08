package tasks;

import android.content.Context;

import camera_module.CameraAPI;

public class DownloadTask extends Thread{
    String[] imageRefs;
    Context context;

    public DownloadTask(String[] imageRefs, Context context){
        this.imageRefs = imageRefs;
        this.context = context;
    }

    public void run(){
        CameraAPI cam = new CameraAPI();
        cam.downloadPictures(imageRefs, context);
    }
}
