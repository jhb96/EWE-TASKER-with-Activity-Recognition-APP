package es.dit.gsi.rulesframework;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import es.dit.gsi.rulesframework.performers.DoorPerformer;
import es.dit.gsi.rulesframework.services.RuleExecutionModule;
import es.dit.gsi.rulesframework.util.CacheMethods;
import es.dit.gsi.rulesframework.util.Tasks;

/**
 * Created by afernandez on 14/03/16.
 */
public class BeaconActivity extends ActionBarActivity {
    ProgressBar progressBar;
    BeaconManager beaconManager;
    private Region region;
    CountDownTimer cdt;

    RuleExecutionModule ruleExecutionModule;
    CacheMethods cacheMethods;
    ImageView beaconIcon;

    String user,place;

    boolean sendEvent = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacons);


        ruleExecutionModule = new RuleExecutionModule(getApplicationContext());
        cacheMethods = CacheMethods.getInstance(getApplicationContext());

        user = cacheMethods.getFromPreferences("beaconRuleUser", "enrique");
        place = cacheMethods.getFromPreferences("beaconRulePlace","GSI lab");

        beaconManager = new BeaconManager(getApplicationContext());
        region = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                didRangeBeacons(list, region);
            }
        });


        cdt = new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Log.i("Countdown","Finished");
                sendEvent = true;
                start();
            }

        }.start();

        beaconIcon = (ImageView) findViewById(R.id.beaconIcon);
        beaconIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),ConfigureBeaconActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, BeaconManager.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    public void didRangeBeacons(List<Beacon> beacons, Region region){
        if(sendEvent) {
            for (Beacon beacon : beacons) {

                int beaconId = beacon.getMajor();
                double accuracy = Utils.computeAccuracy(beacon);
                Log.i("BEACON", String.valueOf(beaconId));
                String channel="PresenceSensor";
                String event="PresenceDetected";
                JSONObject input = new JSONObject();
                try {
                    JSONObject params = new JSONObject();
                    input.put("channel", channel);
                    input.put("user", user);
                    input.put("event", event);
                    params.put("PresenceSensorID", Integer.toString(beaconId));
                    params.put("PresenceDistance", Double.toString(accuracy));
                    input.put("params", params);
                    Log.i("BEACON", String.valueOf(input));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String response = "";
                try {
                    response = new Tasks.PostInputToServerTask().execute(input).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                Log.i("Countdown", "Beacon delivery finished");
                Log.i("BEACON",user);
                //Log.i("BEACON",inputEvent);
                Log.i("BEACON",response);
                //Send response to RuleExecutionModule
                ruleExecutionModule.handleServerResponse(response);
            }
            sendEvent =false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("BEACON","onresume");
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });

        user = cacheMethods.getFromPreferences("beaconRuleUser","public");
        place = cacheMethods.getFromPreferences("beaconRulePlace","GSI lab");
    }

    @Override
    protected void onPause() {
        Log.i("BEACON","onpause");
        super.onPause();
        cdt.cancel();
        beaconManager.stopRanging(region);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
