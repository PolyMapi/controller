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

public class CameraAPI {

    /////////////////PARAMETERS/////////////////
    private static int MAX_ATTEMPTS = 5; // Nombre maximum de tentatives pour une requête

    /////////////////TOOLS/////////////////

    private static StringBuffer query(String urlString, String data) throws IOException {

        //Le contenu de la réponse POST
        StringBuffer content = new StringBuffer();


        int attemptCount = 0; // Compteur de tentatives
        while (attemptCount < MAX_ATTEMPTS) {

            try {
                URL url = new URL(urlString);

                /* Ouvre une connection avec l'object URL */
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                //Methode POST
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json"); //

                // Encodez les données POST à envoyer
                byte[] postDataBytes = data.getBytes("UTF-8");

                /* Écrit les données dans la requête POST */
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.write(postDataBytes);
                wr.flush();
                wr.close();

                /* Utilise BufferedReader pour lire ligne par ligne */
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                //La ligne courante
                String inputLine;

                /* Pour chaque ligne dans la réponse POST */
                while ((inputLine = in.readLine()) != null) { //attend bien la fin de la requête
                    content.append(inputLine);
                }

                //Ferme BufferedReader
                in.close();

                break; // Sortir de la boucle si la requête réussit

            } catch (IOException e) {
                attemptCount++; // Augmenter le compteur de tentatives
                if (attemptCount == MAX_ATTEMPTS) {
                    // Si le nombre maximum de tentatives est atteint, arrêter la boucle
                    throw e; // Lancer l'exception pour signaler l'échec de la requête
                }
                // Attendre avant de réessayer la requête
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return content;
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

    public void initCamera() {
        try {
            //begin session and get sessionId
            StringBuffer response = query("http://192.168.1.1/osc/commands/execute", "{\"name\" : \"camera.startSession\" }");
            String sessionId = getSessionId(response.toString());
            //set options
            response = query("http://192.168.1.1/osc/commands/execute", "{\"name\": \"camera.setOptions\",\"parameters\": {\"sessionId\": \"" + sessionId + "\" ,\"options\": {\"clientVersion\": 2}}}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //A TESTER
    public String takePicture(){
        String imageRef = "";
        try {
            StringBuffer response = query("http://192.168.1.1/osc/state", "{}");
            String lastImageUrl = getURL(response.toString());

            if (!lastImageUrl.equals("")) {
                response = query("http://192.168.1.1/osc/commands/execute", "{\"name\" : \"camera.takePicture\"}");
                int nameRef = getNameRef(lastImageUrl);
                imageRef = String.format("%07d", nameRef + 1);
            } else {
                String url = "";
                response = query("http://192.168.1.1/osc/commands/execute", "{\"name\" : \"camera.takePicture\"}");
                while (url.equals("")) {
                    response = query("http://192.168.1.1/osc/state", "{}");
                    url = getURL(response.toString());
                }
                int nameRef = getNameRef(url);
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

    public void downloadPictures(String[] imageRefs){
        for (int i=0; i<imageRefs.length; i++){
            downloadPicture(imageRefs[i]);
        }
    }
}
