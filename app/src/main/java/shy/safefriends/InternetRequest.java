package shy.safefriends;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by SunHee on 18/02/2016.
 */
public class InternetRequest implements Runnable {

    private static String url_p = "http://www.lyart.fr/wdm/api.php?h=k_zhveuizjfzrg76-456jfdbDEshjG1_Rvzhj786";
    private String url_ = null;

    private String result_ = null;

    private OutputStreamWriter writer = null;
    private BufferedReader reader = null;
    private HttpURLConnection connexion = null;

    public InternetRequest()
    {
        System.setProperty("http.keepAlive", "false");
    }

    public void setUrlAddDistressCall(String phone, double latitude, double longitude)
    {
        url_ =  url_p+"&t=0&up="+phone+"&lat="+latitude+"&lgt="+longitude;
        result_ = "";
    }

    public void setUrlTerminateDistressCall(int call_id)
    {
        url_ = url_p+"&t=1&i="+call_id;
        result_ = "";
    }

    public void setUrlAddCallee(int call_id, String myPhone)
    {
        url_ = url_p+"&t=2&i="+call_id+"&up="+myPhone;
        result_ = "";
    }

    public void setUrlAddResponse(int call_id, String myPhone, double latitude, double longitude)
    {
        url_ = url_p+"&t=3&i="+call_id+"&up="+myPhone+"&lat="+latitude+"&lgt="+longitude;
        result_ = "";
    }

    public void setUrlCheckDistressCall(String myPhone)
    {
        url_ = url_p+"&t=4&up="+myPhone;
        result_ = "";
    }

    public void setUrlCheckResponse(int call_id)
    {
        url_ = url_p+"&t=5&i="+call_id;
        result_ = "";
    }

    public String getResult()
    {
        return result_;
    }

    public void run() {
        try {
            // Encodage des paramètres de la requête
            //String donnees = URLEncoder.encode("identifiant1", "UTF-8") + "=" + URLEncoder.encode("valeur1", "UTF-8");
            //donnees += "&" + URLEncoder.encode("identifiant2", "UTF-8") + "=" + URLEncoder.encode("valeur2", "UTF-8");

            // On a envoyé les données à une adresse distante
            URL url = new URL(url_);
            connexion = (HttpURLConnection) url.openConnection();
            connexion.setDoOutput(true);
            connexion.setChunkedStreamingMode(0);

            // On envoie la requête ici
            writer = new OutputStreamWriter(connexion.getOutputStream());

            // On insère les données dans notre flux
            //writer.write(donnees);

            // Et on s'assure que le flux est vidé
            writer.flush();

            // On lit la réponse ici
            reader = new BufferedReader(new InputStreamReader(connexion.getInputStream()));

            String line;
            // Tant que « ligne » n'est pas null, c'est que le flux n'a pas terminé d'envoyer des informations
            while ((line = reader.readLine()) != null) {
                result_ += line;
                //System.out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
            try {
                reader.close();
            } catch (Exception e) {
            }
            try {
                connexion.disconnect();
            } catch (Exception e) {
            }
        }
    }


}
