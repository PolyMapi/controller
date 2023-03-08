package camera_module;

import android.content.Context;

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
