package es.dit.gsi.rulesframework.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;
import es.dit.gsi.rulesframework.model.Rule;

import static com.estimote.sdk.EstimoteSDK.getApplicationContext;

/**
 * Created by afernandez on 25/01/16.
 */
public class Tasks {

    public static String ipServer = "http://api.ewetasker.cluster.gsi.dit.upm.es";
    //public static String ipServer = "http://ewetasker.cluster.gsi.dit.upm.es";
    public static final String defaultGsiUrl = "http://api.ewetasker.cluster.gsi.dit.upm.es";
    //public static final String defaultGsiUrl = "http://javtfg.barcolabs.com";
    private static final String urlRulesApi =ipServer +  "/mobileConnectionHelper.php";
    private static final String urlInputApi =ipServer;
    private static final String urlGetChannelApi =ipServer +  "/channels/base";
    private static final String urlBifrost = "http://mozart.gsi.dit.upm.es/";
    public static final String urlImages = ipServer + "/img/";
    public static final String urlCMS = "http://javtfg.barcolabs.com/cms/api.php";


    public static class PostRuleToServerTask extends AsyncTask<Object, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Object[] par) {
            // do above Server call here
            Rule mRule = (Rule) par[0];
            String user = (String) par[1];

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urlRulesApi);

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            //Parameters
            params.add(new BasicNameValuePair("rule_title", mRule.getRuleName()));
            params.add(new BasicNameValuePair("rule_description", mRule.getDescription()));
            params.add(new BasicNameValuePair("rule_channel_one", mRule.getIfElement()));
            params.add(new BasicNameValuePair("rule_channel_two", mRule.getDoElement()));
            params.add(new BasicNameValuePair("rule_event_title", mRule.getIfAction()));
            params.add(new BasicNameValuePair("rule_action_title",mRule.getDoAction()));
            params.add(new BasicNameValuePair("rule_place", mRule.getPlace()));
            params.add(new BasicNameValuePair("rule_creator", user));
            params.add(new BasicNameValuePair("rule", mRule.getEyeRule()));//EYE rule with prefix
            params.add(new BasicNameValuePair("command", "createRule"));

            Log.i("RULE","My ruleee"+ mRule.getEyeRule());
            String response = "";
            try {
                post.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse resp = null;
                resp = client.execute(post);

                HttpEntity ent = resp.getEntity();
                response = EntityUtils.toString(ent);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            //process message
            Log.i("SERVER", "Response create rule: " + response);
        }
    }

    public static class PostInputToServerTask extends AsyncTask<JSONObject, Void, String> {
/*
        @Override
        protected String doInBackground(String[] par) {
            // do above Server call here

            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urlInputApi);

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("inputEvent", par[0]));
            params.add(new BasicNameValuePair("user", par[1]));
            params.add(new BasicNameValuePair("command", "insertinput"));

            String response = "";
            try {
                post.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse resp = null;
                resp = client.execute(post);

                HttpEntity ent = resp.getEntity();
                response = EntityUtils.toString(ent);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
*/
        @Override
        protected String doInBackground(JSONObject... input) {
            CacheMethods cacheMethods = CacheMethods.getInstance(getApplicationContext());
            System.out.println("Entra en el DOINBACKGROUND");
            //String url=cacheMethods.getFromPreferences("serverIP","http://http.api.ewetasker.cluster.gsi.dit.upm.es");
            //String url = cacheMethods.getFromPreferences("serverIP","http://138.4.3.242:5000");
             //String url = "http://138.4.3.242:3030"; //Conectar con Fuseki, tampoco funciona
            //IP del ethernet: 138.4.3.242
            //IP del wifi gsi: 192.168.1.105
            //Al hacer el docker-compose e inicializarse el crossbar:
                //PUERTO 1883 para el WAMPMQTTServerFactory  router tranposrt 001
                //Puerto 8080 para el router transport 002
                //Puerto 8081 transport003, starting websocket, static webservice
                //Puerto 8082 path web Service transport004
                //http://api:5000/channels/base

             //String url = "http://138.4.3.242:5000"; //No tiene sentido porque esto sería comunicar con el cliente
            //String url = "http://138.4.3.242:5050"; //HAY respuesta, pero es algo tipo <DOCTYPE HTML PUBLIC ... <title> 500 internal server error...
            //String url = "http:serverIP//192.168.1.105:5050"; //Igual que arriba

            //String url = "http://127.0.0.1:5050";
            //String url = "http://127.0.0.1:5050/channels/base"; //error : failed to connect ... from port:47573. Pero en chrome me v
            //String url = "http://192.168.1.105:8082"; //la respuesta es: error...."No module named 'Validators.ActivityRecognitionSensorValidator'"...

            String url = "http://138.4.3.242:8082"; //SE CONECTA, LA BUENA!

            System.out.println("Pasa del URL");

            Log.i("SERVER", url);
            url = url  +  "/call";
            Log.i("SERVER", url);
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(url);
            HttpParams httpParameters = new BasicHttpParams();
            String response = "";
            int timeoutConnection = 3000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.
            int timeoutSocket = 5000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            JSONObject data = new JSONObject();

            try {
                data.put("procedure","com.channel.event");
                data.put("kwargs",input[0]);
                Log.i("SERVER", "user: " + input[0].getString("user"));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                System.out.println("Qué es data? " + data);
                StringEntity se = new StringEntity(data.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                post.setEntity(se);
                HttpResponse resp;
                resp = client.execute(post);
                if(resp!=null) {
                    response = EntityUtils.toString(resp.getEntity());
                    System.out.println("El response es " + response);
                    Log.i("SERVER", response);
                }
            } catch (UnsupportedEncodingException e) {
                //Should never be thrown
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            Log.i("SERVER", "Response input: " + response);

        }
    }

    public static class GetChannelsFromServerTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... par) {
            HttpClient client = new DefaultHttpClient();
            HttpGet post = new HttpGet(urlGetChannelApi);
            Log.i("GET",urlGetChannelApi);

            //List<NameValuePair> params = new ArrayList<NameValuePair>();

            //params.add(new BasicNameValuePair("command", "getChannels"));

            String response = "";
            try {
                //post.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse resp = null;
                resp = client.execute(post);

                HttpEntity ent = resp.getEntity();
                response = EntityUtils.toString(ent);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

    public static class LoginGSIServerTask extends AsyncTask<String , Void, String> {
        Context context;
        String pass,remember, url = "";
        public LoginGSIServerTask(Context context){
            this.context=context;
        }

        @Override
        protected String doInBackground(String... par) {
            HttpClient client = new DefaultHttpClient();


            //String user = par[0];
             pass = par[0];
             remember = par[1];
            url = par[2];
            //HttpPost post = new HttpPost(urlBifrost);
            HttpPost post = new HttpPost(urlBifrost);
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            //Log.i("Task",user);

            //params.add(new BasicNameValuePair("username", user));
            params.add(new BasicNameValuePair("pass", pass));

            String response = "";
            try {
                post.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse resp = null;
                resp = client.execute(post);

                HttpEntity ent = resp.getEntity();
                response = EntityUtils.toString(ent);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response.equals("1")){
                if(remember.equals("true")) {
                    //Save in local
                    CacheMethods cacheMethods = CacheMethods.getInstance(context);
                    cacheMethods.saveInPreferences("doorKey",pass);
                }
                Toast.makeText(context,"Door opened. Press back to stop listening.",Toast.LENGTH_LONG).show();
            }
            Log.i("Task", "LogIn reponse: " + response);
        }
    }

    public static class GetUrlFromCMS extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... par) {
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(urlCMS+"?location="+par[0]);

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            //params.add(new BasicNameValuePair("location", par[0]));

            String response = "";
            try {
                post.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse resp = null;
                resp = client.execute(post);

                HttpEntity ent = resp.getEntity();
                response = EntityUtils.toString(ent);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }
}
