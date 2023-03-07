package camera_module;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class CameraAPI {

    private static int nameRef;

    /////////////////TOOLS/////////////////

    private static void queryStart(String url, String content){
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(content, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("query", "erreur");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Traiter la réponse de la requête ici
                String responseString = response.toString();
                Log.d("query", responseString);
                String sessionId = getSessionId(responseString);
                //set options
                queryOptions("http://192.168.1.1/osc/commands/execute", "{\"name\": \"camera.setOptions\",\"parameters\": {\"sessionId\": \"" + sessionId + "\" ,\"options\": {\"clientVersion\": 2}}}");
            }
        });
    }

    private static void queryOptions(String url, String content) {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(content, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("query", "erreur");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Traiter la réponse de la requête ici
                String responseString = response.toString();
                Log.d("query", responseString);
            }
        });
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
        //begin session and get sessionId
        queryStart("http://192.168.1.1/osc/commands/execute", "{\"name\" : \"camera.startSession\" }");

        //set resolution
        if (highResolution) {
            queryOptions("http://192.168.1.1/osc/commands/execute", "{\"name\": \"camera.setOptions\",\"parameters\": {\"options\": {\"fileFormat\": {\"type\": \"jpeg\",\"width\": 5376,\"height\": 2688}}}}");
        } else {
            queryOptions("http://192.168.1.1/osc/commands/execute", "{\"name\": \"camera.setOptions\",\"parameters\": {\"options\": {\"fileFormat\": {\"type\": \"jpeg\",\"width\": 2048,\"height\": 1024}}}}");
        }
    }

    //take a picture and return the image reference
    public String takePicture(){
        String imageRef = "";
        try {
            QueryTask query = new QueryTask();
            query.execute("http://192.168.1.1/osc/state", "{}");
            String response = query.get();
            String lastImageUrl = getURL(response.toString());

            if (!lastImageUrl.equals("")) {
                query.execute("http://192.168.1.1/osc/commands/execute", "{\"name\" : \"camera.takePicture\"}");
                response = query.get();
                if (nameRef == 0) {
                    nameRef = getNameRef(lastImageUrl);
                }
                nameRef += 1;
                imageRef = String.format("%07d", nameRef);
            } else {
                String url = "";
                query.execute("http://192.168.1.1/osc/commands/execute", "{\"name\" : \"camera.takePicture\"}");
                response = query.get();
                while (url.equals("")) {
                    query.execute("http://192.168.1.1/osc/state", "{}");
                    response = query.get();
                    url = getURL(response.toString());
                }
                nameRef = getNameRef(url);
                imageRef = String.format("%07d", nameRef);
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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

    public void testFunctions(){
        long start = System.currentTimeMillis();

        //clearPictures();

        //begin session and get sessionId
        initCamera(false);

        String[] imageRefs = new String[5];
        for (int i=0; i<5; i++) {
            imageRefs[i] = takePicture();
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        downloadPictures(imageRefs);

        long stop = System.currentTimeMillis();
        long elapsed = (stop - start) / 1000;
        System.out.println("END");
        System.out.println("Durée d'exécution totale : " + elapsed + "s");
    }
}
