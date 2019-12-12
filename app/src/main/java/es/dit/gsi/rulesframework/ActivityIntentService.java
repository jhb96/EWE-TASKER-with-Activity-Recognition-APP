package es.dit.gsi.rulesframework;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;

import android.preference.PreferenceManager;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.gson.Gson;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */


public class ActivityIntentService extends IntentService {
    protected static final String TAG = "Activity";
    private int id = 1;

    //Call the super IntentService constructor with the name for the worker thread//
    public ActivityIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
    //Define an onHandleIntent() method, which will be called whenever an activity detection update is available.
    // It is used (for now) to get an array of DetectedActivities and most probable activity//

    @Override
    protected void onHandleIntent(Intent intent) {
    //Check whether the Intent contains activity recognition data//
        if (ActivityRecognitionResult.hasResult(intent)) {


////////////////////////////////////// MANAGING THE RESULT /////////////////////////////////////////

    //If data is available, then extract the ActivityRecognitionResult from the Intent//

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            DetectedActivity higherDa = result.getMostProbableActivity();
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();


    //DA to DAINFO

            LocalDateTime timeNow = LocalDateTime.now();

            DetectedActivityInfo higherDaInfo = new DetectedActivityInfo(higherDa.getType(), higherDa.getConfidence(), timeNow);
            ArrayList<DetectedActivityInfo> daInfoArray = new ArrayList<>();

            for(DetectedActivity da : detectedActivities){
                DetectedActivityInfo daInfo = new DetectedActivityInfo(da.getType(),da.getConfidence(), timeNow);
                daInfoArray.add(daInfo);
            }


//////////////////////////// SHARED PREFERENCES SAVES //////////////////////////////////////////////

            if (higherDa.getConfidence()>= 70) {
                PreferenceManager.getDefaultSharedPreferences(this)
                        .edit()
                        .putString(RecognitionActivity.MOST_PROBABLY_DA,
                                new Gson().toJson(higherDa))
                        .apply();
            }

            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString(RecognitionActivity.DETECTED_ACTIVITY,
                            detectedActivitiesToJson(detectedActivities))
                    .apply();





////////////////////////////////////////// FIREBASE DB /////////////////////////////////////////////

            FirebaseDatabase database = FirebaseDatabase.getInstance();

            DatabaseReference myRef = database.getReference("Detected Activities arrays");
            DatabaseReference activityActualRef = database.getReference("Registro de actividad");
            DatabaseReference daInfoRef = myRef.child("ArrayDaInfo");

            daInfoRef.push().setValue(daInfoArray);
            activityActualRef.push().setValue(higherDaInfo);


////////////////////////////////////////////////////////////////////////////////////////////////////

        }
    }



//Convert the code for the detected activity type, into the corresponding string//

    static String getActivityString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.vehicle);
            default:
                return resources.getString(R.string.unknown_activity, detectedActivityType);
        }
    }


    static final int[] POSSIBLE_ACTIVITIES = {

            DetectedActivity.STILL,
            DetectedActivity.ON_FOOT,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.TILTING,
            DetectedActivity.UNKNOWN
    };


    static String detectedActivitiesToJson(ArrayList<DetectedActivity> detectedActivitiesList) {
        Type type = new TypeToken<ArrayList<DetectedActivity>>() {}.getType();
        return new Gson().toJson(detectedActivitiesList, type);
    }


    static ArrayList<DetectedActivity> detectedActivitiesFromJson(String jsonArray) {
        Type listType = new TypeToken<ArrayList<DetectedActivity>>(){}.getType();
        ArrayList<DetectedActivity> detectedActivities = new Gson().fromJson(jsonArray, listType);
        if (detectedActivities == null) {
            detectedActivities = new ArrayList<>();
        }
        return detectedActivities;
    }

}
