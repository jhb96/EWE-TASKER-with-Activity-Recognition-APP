package es.dit.gsi.rulesframework;


import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;

import android.support.v4.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;


public class ActivitiesAdapter extends ArrayAdapter<DetectedActivity> {

    ActivitiesAdapter(Context context,
                      ArrayList<DetectedActivity> detectedActivities) {
        super(context, 0, detectedActivities);
    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {

    //Retrieve the data item//
        DetectedActivity detectedActivity = getItem(position);
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(
                    R.layout.detected_activity, parent, false);
        }

    //Retrieve the TextViews where we’ll display the activity type, and percentage//

        ImageView activityIcon = (ImageView) view.findViewById(R.id.item_icon);
        TextView activityName = (TextView) view.findViewById(R.id.activity_type);
        TextView activityConfidenceLevel = (TextView) view.findViewById(R.id.confidence_percentage);

    //If an activity is detected draw it...//
        if (detectedActivity != null) {
            int activityType = detectedActivity.getType();
            String activityConfidence = getContext().getString(R.string.percentage,detectedActivity.getConfidence());

            activityName.setText( ActivityIntentService.getActivityString(getContext(),activityType));
            activityConfidenceLevel.setText(activityConfidence);
            switch(activityType){
                case DetectedActivity.ON_BICYCLE:
                    activityIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_directions_bike_grey_24dp));
                    break;
                case DetectedActivity.ON_FOOT:
                    activityIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_onfoot_grey_24dp));
                    break;
                case DetectedActivity.RUNNING:
                    activityIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_directions_run_grey_24dp));
                    break;
                case DetectedActivity.STILL:
                    activityIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_still_grey_24dp));
                    break;
                case DetectedActivity.TILTING:
                    activityIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_tilting_grey_24dp));
                    break;
                case DetectedActivity.WALKING:
                    activityIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_directions_walk_grey_24dp));
                    break;
                case DetectedActivity.IN_VEHICLE:
                    activityIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_directions_car_grey_24dp));
                    break;
                default:
                    activityIcon.setImageDrawable(ContextCompat.getDrawable(getContext(),R.drawable.ic_unknown_grey_24dp));
                    break;
            }

        }
        return view;

    }



//Process the list of detected activities//
    void updateActivities(ArrayList<DetectedActivity> detectedActivities) {
        HashMap<Integer, Integer> detectedActivitiesMap = new HashMap<>();
        for (DetectedActivity activity : detectedActivities) {
            detectedActivitiesMap.put(activity.getType(), activity.getConfidence());
        }

        ArrayList<DetectedActivity> temporaryList = new ArrayList<>();
        for (int i = 0; i < ActivityIntentService.POSSIBLE_ACTIVITIES.length; i++) {
            int confidence = detectedActivitiesMap.containsKey(ActivityIntentService.POSSIBLE_ACTIVITIES[i]) ?
                    detectedActivitiesMap.get(ActivityIntentService.POSSIBLE_ACTIVITIES[i]) : 0;

            //Add the object to a temporaryList//
            temporaryList.add(new
                    DetectedActivity(ActivityIntentService.POSSIBLE_ACTIVITIES[i],
                    confidence));
        }

    //Remove all elements from the temporaryList//
        this.clear();

    //Refresh the View//
        for (DetectedActivity detectedActivity: temporaryList) {
            this.add(detectedActivity);
        }
    }


}
