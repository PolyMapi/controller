package camera_module;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

    private static int nameRef;
    /////////////////PARAMETERS/////////////////
    private static int MAX_ATTEMPTS = 5; // Nombre maximum de tentatives pour une requête

    /////////////////TOOLS/////////////////

    private static String query(String urlString, String data) throws IOException {

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

    private static String getSessionId(String response) {
        String res = response.split("SID_")[1];
        res = res.substring(0, 4);
        return "SID_" + res;
    }

    private static String getFingerprint(String response) {
        String res = response.split("FIG_")[1];
        res = res.substring(0, 4);
        return "FIG_" + res;
    }

    private static String getURL(String response) {
        String res = response.split("_latestFileUrl\":\"")[1];
        res = res.split("\",\"_batteryState")[0];
        return res;
    }

    private static int getNameRef(String url) {
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
    public void downloadPicture(String imageRef){
        //download image in pictures directory
        try {
            URL url = new URL("http://192.168.1.1/files/035344534c303847803aea0cf9010c01/100RICOH/R" + imageRef + ".JPG");
            File download = new File("./pictures/R" + imageRef + ".JPG");
            FileUtils.copyURLToFile(url, download);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //download a group of pictures which corresponds to imageRefs in the "pictures" directory
    public void downloadPictures(String[] imageRefs){
        for (int i=0; i<imageRefs.length; i++){
            downloadPicture(imageRefs[i]);
        }
    }

    //delete every pictures downloaded in the "pictures" directory
    public void clearPictures() {
        File directory = new File("./pictures");

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
        }
    }

}
