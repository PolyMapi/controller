package camera_module;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class CameraAPI {

    private static CameraAPI instance;
    private static int nameRef;
    /////////////////PARAMETERS/////////////////
    private static int MAX_ATTEMPTS = 5; // Nombre maximum de tentatives pour une requête


    /////////////////CONSTRUCTOR/////////////////

    private CameraAPI(){
        initCamera(false);
    }

    public static CameraAPI getInstance(){
        if (instance == null){
            instance = new CameraAPI();
        }
        return instance;
    }

    /////////////////TOOLS/////////////////

    private String query(String urlString, String data) throws IOException {

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(data, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(urlString)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseData = response.body().string();
            return responseData;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getSessionId(String response) {
        String res = response.split("SID_")[1];
        res = res.substring(0, 4);
        return "SID_" + res;
    }

    private String getFingerprint(String response) {
        String res = response.split("FIG_")[1];
        res = res.substring(0, 4);
        return "FIG_" + res;
    }

    private String getURL(String response) {
        String res = response.split("_latestFileUrl\":\"")[1];
        res = res.split("\",\"_batteryState")[0];
        return res;
    }

    private int getNameRef(String url) {
        String name = url.split("/100RICOH/R")[1].substring(0, 7);
        int nameRef = 0;
        try {
            nameRef = Integer.parseInt(name);
        } catch (NumberFormatException e) {
            System.out.println("Impossible de convertir la chaîne en entier.");
        }
        return nameRef;
    }

    /////////////////FUNCTIONS/////////////////

    //initialize the camera
    //  with 5376x2688 resolution if highResolution = true,
    //  with 2048x1024 resolution else
    public void initCamera(boolean highResolution) {
        nameRef = 0;
        try {
            //begin session and get sessionId
            String response = query("http://192.168.1.1/osc/commands/execute", "{\"name\" : \"camera.startSession\" }");
            String sessionId = getSessionId(response);
            //set options
            response = query("http://192.168.1.1/osc/commands/execute", "{\"name\": \"camera.setOptions\",\"parameters\": {\"sessionId\": \"" + sessionId + "\" ,\"options\": {\"clientVersion\": 2}}}");
            //set resolution
            if (highResolution) {
                response = query("http://192.168.1.1/osc/commands/execute", "{\"name\": \"camera.setOptions\",\"parameters\": {\"options\": {\"fileFormat\": {\"type\": \"jpeg\",\"width\": 5376,\"height\": 2688}}}}");
            } else {
                response = query("http://192.168.1.1/osc/commands/execute", "{\"name\": \"camera.setOptions\",\"parameters\": {\"options\": {\"fileFormat\": {\"type\": \"jpeg\",\"width\": 2048,\"height\": 1024}}}}");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("task", "initCamera done");
    }

    //take a picture and return the image reference
    public String takePicture(){
        String imageRef = "";
        try {
            String response = query("http://192.168.1.1/osc/state", "{}");
            String lastImageUrl = getURL(response);

            if (!lastImageUrl.equals("")) {
                response = query("http://192.168.1.1/osc/commands/execute", "{\"name\" : \"camera.takePicture\"}");
                if (nameRef == 0) {
                    nameRef = getNameRef(lastImageUrl);
                }
                nameRef += 1;
                imageRef = String.format("%07d", nameRef);
            } else {
                String url = "";
                response = query("http://192.168.1.1/osc/commands/execute", "{\"name\" : \"camera.takePicture\"}");
                while (url.equals("")) {
                    response = query("http://192.168.1.1/osc/state", "{}");
                    url = getURL(response);
                }
                nameRef = getNameRef(url);
                imageRef = String.format("%07d", nameRef);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageRef;
    }

    //?
    public String[] takeNPictures(int n, int time_interval){
        String[] imageRefs = new String[n];

        return imageRefs;
    }

    //?
    public String[] takePicturesDuringTimer(int timer, int time_interval){
        String[] imageRefs = new String[10];

        return imageRefs;
    }

    //download the picture which corresponds to the imageRef in the "pictures" directory
    // return the path of the image
    public String downloadPicture(String imageRef, Context context){
        //download image in pictures directory
        try {
            URL url = new URL("http://192.168.1.1/files/035344534c303847803aea0cf9010c01/100RICOH/R" + imageRef + ".JPG");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            File picturesDir = new File(context.getFilesDir(), "pictures");
            if (!picturesDir.exists()) {
                picturesDir.mkdirs();
            }

            File outputFile = new File(picturesDir, "R" + imageRef + ".JPG");
            OutputStream output = new FileOutputStream(outputFile);

            InputStream input = connection.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            output.close();
            input.close();
            Log.d("task", "download " + imageRef + " completed");
            return context.getFilesDir().toString() + "/pictures/R" + imageRef + ".JPG";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //download a group of pictures which corresponds to imageRefs in the "pictures" directory
    //return all images path
    public String[] downloadPictures(String[] imageRefs, Context context){
        String[] paths = new String[imageRefs.length];
        for (int i=0; i<imageRefs.length; i++){
            paths[i] = downloadPicture(imageRefs[i], context);
        }
        return paths;
    }

    //delete every pictures downloaded in the "pictures" directory
    public void clearAllPictures(Context context) {
        File directory = new File(context.getFilesDir(), "pictures");

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }

    //delete some pictures downloaded in the "pictures" directory
    public void clearPictures(Context context, String[] imagePaths) {
        File directory = new File(context.getFilesDir(), "pictures");

        if (directory.exists() && directory.isDirectory()) {
            for (int i=0; i<imagePaths.length; i++){
                File file = new File(imagePaths[i]);
                if (file != null) {
                    file.delete();
                }
            }
        }
    }

}
