package tasks;

import android.content.Context;

import camera_module.CameraAPI;

public class UploadTask extends Thread{

    String[] imagePaths;
    Context context;

    public UploadTask(Context context){
        this.context = context;
    }
    public UploadTask(Context context, String[] imagePaths){
        this.context = context;
        this.imagePaths = imagePaths;
    }

    public void run(){
        if (imagePaths != null){
            //upload les images de imagePaths

            CameraAPI.getInstance().clearPictures(context, imagePaths);
        } else {
            //upload toutes les images

            CameraAPI.getInstance().clearAllPictures(context);
        }
    }
}
