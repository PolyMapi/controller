package camera_module;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class QueryTask extends AsyncTask<String, Void, String> {

    /////////////////PARAMETERS/////////////////
    private static int MAX_ATTEMPTS = 5; // Nombre maximum de tentatives pour une requête
    private static int WAITING_TIME = 2000; // Temps d'attente entre chaque tentative de requête


    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0];
        String data = params[1];
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
                    return null; // Retourner null pour signaler l'échec de la requête
                }
                // Attendre avant de réessayer la requête
                try {
                    Thread.sleep(WAITING_TIME);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return content.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        // Mettre à jour l'interface utilisateur avec la réponse de la requête
        if (result != null) {
            // Le traitement de la réponse de la requête ici
        } else {
            // La requête a échoué
        }
    }
}
