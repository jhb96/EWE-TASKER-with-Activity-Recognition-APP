package es.dit.gsi.rulesframework;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cz.msebera.android.httpclient.util.EntityUtils;
import es.dit.gsi.rulesframework.adapters.MyRecyclerViewAdapter;
import es.dit.gsi.rulesframework.database.RulesSQLiteHelper;
import es.dit.gsi.rulesframework.model.Action;
import es.dit.gsi.rulesframework.model.Channel;
import es.dit.gsi.rulesframework.model.Event;
import es.dit.gsi.rulesframework.model.NamedGeofence;
import es.dit.gsi.rulesframework.model.Parameter;
import es.dit.gsi.rulesframework.model.Rule;
import es.dit.gsi.rulesframework.receivers.GeofenceIntentService;
import es.dit.gsi.rulesframework.services.RuleExecutionModule;
import es.dit.gsi.rulesframework.util.CacheMethods;
import es.dit.gsi.rulesframework.util.Constants;
import es.dit.gsi.rulesframework.util.Tasks;

public class ListRulesActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks , GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    protected static final String TAG = "ListRulesActivity";


    List<Object> items = new ArrayList<>();
    List<Rule> rules = new ArrayList<>();
    android.support.design.widget.FloatingActionButton floatingActionButton;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    TextView emptyView;

    //SQL
    RulesSQLiteHelper db;

    //Geofence
    private List<NamedGeofence> namedGeofences;
    protected ArrayList<Geofence> mGeofenceList;
    private GoogleApiClient mGoogleApiClient;
    private Gson gson;
    private SharedPreferences prefs;
    private PendingIntent mGeofencePendingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rules_activity);

        //Set IP SERVER
        CacheMethods cacheMethods = CacheMethods.getInstance(getApplicationContext());
        Tasks.ipServer = cacheMethods.getFromPreferences("ipServer",Tasks.defaultGsiUrl);

        //ActionBar
        getSupportActionBar().setTitle("List of Rules");

        RuleExecutionModule ruleExecutionModule = new RuleExecutionModule(getApplicationContext());
        //ruleExecutionModule.executeDoResponse("Gestor","","http://ww.google.es");

        //RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.listRules);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new OnFloatingButtonClickListener());

        db = new RulesSQLiteHelper(this);

        //Empty message
        emptyView = (TextView) findViewById(R.id.empty_view);
        emptyView.setTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(), "Titillium-Regular.otf"));

        addItems();

        namedGeofences = new ArrayList<>();
        mGeofenceList = new ArrayList<>();
        gson = new Gson();
        prefs = this.getApplicationContext().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        mGeofencePendingIntent = null;

        populateGeofenceList();

        buildGoogleApiClient(); //Callback addGeofences()

        try{
            getChannels();
        }catch (Exception e){
            e.printStackTrace();
        }

        //DEBUG
        //new RuleExecutionModule(getApplicationContext()).sendInputToEye("input","tono");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteAllRules:
                db.deleteAllRules();
                addItems();
                return true;
            case R.id.info:
                Intent i = new Intent(this,InfoGSIActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return true;
            /*case R.id.deleteAllGeofences:
                removeGeofences();
            case R.id.listGeofences:
                Intent i = new Intent(this,ListGeofencesActivity.class);
                startActivity(i);*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public void addItems() {
        Log.i("Rules Items", "Adding items");
        items.clear();
        rules = db.getAllRules();
        for (Rule r : rules) {
            items.add(r);
            Log.i("Rules Items", r.getRuleName());
        }
        mAdapter = new MyRecyclerViewAdapter(this, items);
        mRecyclerView.setAdapter(mAdapter);

        if(items.size()==0){
            mRecyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            mRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }




    public class OnFloatingButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(v.getContext(), NewRuleActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.exit(0);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }
    @Override
    protected void onResume() {
        super.onResume();
        addItems();
    }

    //Geofences
    public void populateGeofenceList() {
        Map<String, ?> keys = prefs.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            String jsonString = prefs.getString(entry.getKey(), null);
            NamedGeofence namedGeofence = gson.fromJson(jsonString, NamedGeofence.class);
            namedGeofences.add(namedGeofence);
            Log.i(TAG, "Geofence loaded");

        }

        // Sort namedGeofences by name
        Collections.sort(namedGeofences);

        for (NamedGeofence ng : namedGeofences) {

            Log.i("GEOFENCE Name", String.valueOf(ng.name));
            Log.i("GEOFENCE Lat", String.valueOf(ng.latitude));
            Log.i("GEOFENCE Long", String.valueOf(ng.longitude));
            Log.i("GEOFENCE Rad", String.valueOf(ng.radius));

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(ng.name)

                            // Set the circular region of this geofence.
                    .setCircularRegion(
                            ng.latitude,
                            ng.longitude,
                            ng.radius
                    )

                            // Set the expiration duration of the geofence. This geofence gets automatically
                            // removed after this period of time.
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)

                            // Set the transition types of interest. Alerts are only generated for these
                            // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                            // Create the geofence.
                    .build());
        }
    }
    public void addGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            securityException.printStackTrace();
        }
    }

    public void removeGeofences() {
        if (!mGoogleApiClient.isConnected()) {
            //Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            securityException.printStackTrace();
        }
    }
    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if(namedGeofences.size()>0){
            addGeofences();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection to GoogleApiClient Suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection to GoogleApiClient Failed");
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            Log.i(TAG,"Geofences added");
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = "Error onResult";
            Log.e(TAG, errorMessage);
        }
    }

    //Channels

    public void getChannels() throws JSONException {
        //Get Task
        //String response = "[{\"title\":\"door\",\"description\":\"This channel represents a door.\",\"events\":[{\"title\":\"Door opened event\",\"rule\":\"Rule for door opened\",\"prefix\":\"\",\"numParameters\":\"0\"}],\"actions\":[{\"title\":\"Open door action\",\"rule\":\"Rule for opening the door\",\"prefix\":\"\",\"numParameters\":\"0\"}]},{\"title\":\"tv\",\"description\":\"This channel represents a TV.\",\"events\":[{\"title\":\"If a knows b\",\"rule\":\"?a :knows ?b\",\"prefix\":\"\",\"numParameters\":\"0\"}],\"actions\":[{\"title\":\"then b knows a\",\"rule\":\"?b :knows ?a\",\"prefix\":\"\",\"numParameters\":\"0\"}]}]";
        String response = "";
        try {
            response = new Tasks.GetChannelsFromServerTask().execute().get(1000, TimeUnit.MILLISECONDS);
            Log.i("NewRuleActivity", response);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        Constants.savePreferences(getApplicationContext(), "channelsJson", response);
    }

    public static List<Channel> translateJSONtoList(String json) throws JSONException {
        List<Channel> list = new ArrayList<>();

        JSONArray channels_json = new JSONArray(json);
        JSONArray channels = channels_json.getJSONArray(1);
        Log.i("NewRuleActivity", channels.toString());
        for (int i = 0; i < channels.length(); i++) {
            JSONObject currentChannel = (JSONObject) channels.get(i);
            String id = currentChannel.getString("id");
            String label = currentChannel.getString("label");
            String comment = currentChannel.getString("comment");
            JSONArray events = currentChannel.getJSONArray("events");
            List<Event> eventsList = new ArrayList<>();
            for (int j = 0; j < events.length(); j++) {
                JSONObject currentEvent = (JSONObject) events.get(j);
                String event_id = currentEvent.getString("id");
                String event_label = currentEvent.getString("label");
                String event_comment = currentEvent.getString("comment");
                JSONArray input_params = currentEvent.getJSONArray("input_parameters");
                List<Parameter> input_params_List = new ArrayList<>();
                for (int a = 0; a < input_params.length(); a++) {
                    JSONObject currentParam = (JSONObject) input_params.get(a);
                    String param_id = currentParam.getString("id");
                    String param_label = currentParam.getString("label");
                    String param_comment = currentParam.getString("comment");
                    String param_datatype = currentParam.getString("datatype");
                    Parameter p = new Parameter(param_id, param_label, param_comment, param_datatype);
                    input_params_List.add(p);
                }
                JSONArray output_params = currentEvent.getJSONArray("input_parameters");
                List<Parameter> output_params_List = new ArrayList<>();
                for (int a = 0; a < output_params.length(); a++) {
                    JSONObject currentParam = (JSONObject) output_params.get(a);
                    String param_id = currentParam.getString("id");
                    String param_label = currentParam.getString("label");
                    String param_comment = currentParam.getString("comment");
                    String param_datatype = currentParam.getString("datatype");
                    Parameter p = new Parameter(param_id, param_label, param_comment, param_datatype);
                    output_params_List.add(p);
                }
                Event e = new Event(event_id, event_label, event_comment, input_params_List, output_params_List);
                eventsList.add(e);
            }
            JSONArray actions = currentChannel.getJSONArray("actions");
            List<Action> actionsList = new ArrayList<>();
            for (int n = 0; n < actions.length(); n++) {
                JSONObject currentAction = (JSONObject) actions.get(n);
                String action_id = currentAction.getString("id");
                String action_label = currentAction.getString("label");
                String action_comment = currentAction.getString("comment");
                JSONArray input_params = currentAction.getJSONArray("input_parameters");
                List<Parameter> input_params_List = new ArrayList<>();
                for (int a = 0; a < input_params.length(); a++) {
                    JSONObject currentParam = (JSONObject) input_params.get(a);
                    String param_id = currentParam.getString("id");
                    String param_label = currentParam.getString("label");
                    String param_comment = currentParam.getString("comment");
                    String param_datatype = currentParam.getString("datatype");
                    Parameter p = new Parameter(param_id, param_label, param_comment, param_datatype);
                    input_params_List.add(p);
                }
                JSONArray output_params = currentAction.getJSONArray("input_parameters");
                List<Parameter> output_params_List = new ArrayList<>();
                for (int a = 0; a < output_params.length(); a++) {
                    JSONObject currentParam = (JSONObject) output_params.get(a);
                    String param_id = currentParam.getString("id");
                    String param_label = currentParam.getString("label");
                    String param_comment = currentParam.getString("comment");
                    String param_datatype = currentParam.getString("datatype");
                    Parameter p = new Parameter(param_id, param_label, param_comment, param_datatype);
                    output_params_List.add(p);
                }
                Action ac = new Action(action_id, action_label, action_comment, input_params_List, output_params_List);
                actionsList.add(ac);
            }
            Channel ch = new Channel(id,label,comment, actionsList, eventsList);
            list.add(ch);
        }

        return list;
    }

}