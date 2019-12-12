package es.dit.gsi.rulesframework;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;


import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;


import com.estimote.sdk.Region;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;


import org.json.JSONException;
import org.json.JSONObject;

import es.dit.gsi.rulesframework.services.RuleExecutionModule;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import es.dit.gsi.rulesframework.util.CacheMethods;
import es.dit.gsi.rulesframework.util.Tasks;




public class RecognitionActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private String CHANNEL_ID = "Channel 1";
    private int i = 0;
    private Context mContext;
    public static final String DETECTED_ACTIVITY = ".DETECTED_ACTIVITY";
    public static final String MOST_PROBABLY_DA = ".MOST_PROBABLY_DA";
    private  ArrayList<DetectedActivity> detectedActivitiesNew = new ArrayList<>();
    private  ArrayList<DetectedActivityInfo> daInfo = new ArrayList<>();

    //Define an ActivityRecognitionClient//
    private ActivityRecognitionClient mActivityRecognitionClient;
    private ActivitiesAdapter mAdapter;
    private Task<Void> task;

    //Ewe-Tasker connection vars
    String user,place;
    boolean sendEvent = true;
    RuleExecutionModule ruleExecutionModule;
    CacheMethods cacheMethods;
    private Region region;

///////////////////////////////////////// "ON" METHODS /////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_recognition);
        mContext = this;

    // Secure creation of notification channels (Navbar)
        createNotificationChannel();


    // Ewe-Tasker connection initialization
        ruleExecutionModule = new RuleExecutionModule(getApplicationContext());
        cacheMethods = CacheMethods.getInstance(getApplicationContext());
        //user = cacheMethods.getFromPreferences("beaconRuleUser", "enrique");
        //place = cacheMethods.getFromPreferences("beaconRulePlace","GSI lab");
        region = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

    // Get view elements
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ListView detectedActivitiesListView = (ListView) findViewById(R.id.activities_listview);


    // Toolbar
        setSupportActionBar(toolbar);
        // Add back arrow to the toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


    // Bind the adapter to the ListView//
        mAdapter = new ActivitiesAdapter(this, detectedActivitiesNew);
        detectedActivitiesListView.setAdapter(mAdapter);
        mActivityRecognitionClient = new ActivityRecognitionClient(this);


    // Shared preferences Listener
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                updateDetectedActivitiesList();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);


    // Create task that will init ActivityIntentService
        // First parameter indicates the activity detection interval.
        // Trying different values, I consider that 3 seconds gets good results.
        task = mActivityRecognitionClient.requestActivityUpdates(3000,
                getActivityDetectionPendingIntent());

    //Add Listeners to the task.
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                updateDetectedActivitiesList();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        updateDetectedActivitiesList();
    }


    @Override
    protected void onPause() {

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

        super.onPause();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////




////////////////////////////////////// BUTTON METHODS //////////////////////////////////////////////
    // Reload button method
    public void requestUpdatesHandler(MenuItem item) {
        updateDetectedActivitiesList();
    }

    //Button method to start "show stats" activity
    public void sendToShowStats(View view){
        Intent intent = new Intent(this, ShowStats.class);
        startActivity(intent);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////




///////////////////////////////////////// MENU OPTIONS /////////////////////////////////////////////
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        int id = item.getItemId();

        if (id == R.id.action_informationAR){
                Intent viewIntent = new Intent("android.intent.action.VIEW",
                        Uri.parse("https://developers.google.com/location-context/activity-recognition"));
            startActivity(viewIntent);
        }

        if (id == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recognition, menu);
        return super.onCreateOptionsMenu(menu);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////



/////////////////////////////// START SERVICE ACTIVITY INTENT SERVICE //////////////////////////////

    private PendingIntent getActivityDetectionPendingIntent() {
        //Send the activity data to our DetectedActivitiesIntentService class//
        Intent intent = new Intent(this, ActivityIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////




////////////////////////////// METHOD TO UPDATE LIST VIEW /////////////////////////////////////////

    //Process the list of activities//
    protected void updateDetectedActivitiesList() {

    //SHARED PREFERENCES LOADS

        // The list of detected activities
        ArrayList<DetectedActivity> detectedActivities = ActivityIntentService.detectedActivitiesFromJson(
                PreferenceManager.getDefaultSharedPreferences(mContext)
                        .getString(DETECTED_ACTIVITY, ""));

        //The most probably detected activity
        DetectedActivity higherDa = new Gson().fromJson(PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(MOST_PROBABLY_DA, ""), DetectedActivity.class);

        //Do the post to EWE
        try {
            if (higherDa != null && higherDa.getConfidence() > 60) {
                System.out.println(ActivityIntentService.getActivityString(mContext,higherDa.getType()));
                System.out.println("Envío el data: " + ActivityIntentService.getActivityString(mContext, higherDa.getType()));

                //PostData to Ewe Server only works in local for now.
                // When the EWE Tasker server is online again, it will work everywhere.
                    //postData(ActivityIntentService.getActivityString(mContext, higherDa.getType()), "detectedActivity", region);
            }
        } catch (Exception e){}
            mAdapter.updateActivities(detectedActivities);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////




/////////////////////////////////////// MÉTODO DEL POST ////////////////////////////////////////////

    public void postData(String data, String campo , Region region){
        String channel="ActivityRecognitionSensor";
        String event="DetectedActivitySensor";
        JSONObject input = new JSONObject();
        try {
            JSONObject params = new JSONObject();
            input.put("channel", channel);
            input.put("user", "jhb96");      //Mi cuenta, pero en ningún sitio he visto contraseña.
            input.put("event", event);    // Esto lo he sacado de la petición de get channels, así que supongo que estará bien.
            params.put("DetectedActivity", data);  //Tengo dudas sobre esto, no se si se llama Emotion a secas o hay que poner un nombre diferente, no se mirar este nombre.
            input.put("params", params);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String response = "";
        Log.i("EWETASKER", "Hemos hecho el json bien");

        try {
            response = new Tasks.PostInputToServerTask().execute(input).get();
            System.out.println("LA RESPUESTA ES " + response);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("Ahora vamos a MANEJAR la RESPUESTA");
        Log.i("ActivityRecognition",response);
        //Send response to RuleExecutionModule
        ruleExecutionModule.handleServerResponse(response);

    }
////////////////////////////////////////////////////////////////////////////////////////////////////


///////////////////////////////////// CHANNEL NAVBAR METHOD  ///////////////////////////////////////

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////



///////////////////////////////// SHARED PREFERENCE CHANGED //////////////////////////////////////
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        updateDetectedActivitiesList();
    }
////////////////////////////////////////////////////////////////////////////////////////////////////


}
