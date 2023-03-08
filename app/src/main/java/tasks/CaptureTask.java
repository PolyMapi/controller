package tasks;

import android.util.Log;

import camera_module.CameraAPI;

public class CaptureTask extends Thread{



    public void run(){
        long start = System.currentTimeMillis();
        CameraAPI cam = new CameraAPI();

        //clearPictures();

        //begin session and get sessionId
        cam.initCamera(false);
        Log.d("task", "initCamera done");

        String[] imageRefs = new String[5];
        for (int i=0; i<5; i++) {
            imageRefs[i] = cam.takePicture();
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                Log.d("task", "Interruption");
            }
        }
        for(String ref : imageRefs){
            Log.d("task", ref);
        }

        long stop = System.currentTimeMillis();
        long elapsed = (stop - start) / 1000;
        Log.d("task", "END");
        Log.d("task","Durée d'exécution totale : " + elapsed + "s");
    }
}
