package es.dit.gsi.rulesframework;

/**
 * Created by evara on 30/11/2017.
 */

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import es.dit.gsi.rulesframework.services.RuleExecutionModule;
import es.dit.gsi.rulesframework.util.CacheMethods;
import es.dit.gsi.rulesframework.util.Tasks;

public class EmpaticaActivity extends AppCompatActivity implements EmpaDataDelegate, EmpaStatusDelegate{
    protected static final String TAG = "EmpaticaActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    private static final long STREAMING_TIME = 100000; // Stops streaming 10 seconds after connection

    private static final String EMPATICA_API_KEY = "d4621bbef4314668af69e392e4c35bb6"; // TODO insert your API Key here

    private EmpaDeviceManager deviceManager = null;

    private TextView accel_xLabel;
    private TextView accel_yLabel;
    private TextView accel_zLabel;
    private TextView bvpLabel;
    private TextView edaLabel;
    private TextView ibiLabel;
    private TextView temperatureLabel;
    private TextView batteryLabel;
    private TextView statusLabel;
    private TextView deviceNameLabel;
    private RelativeLayout dataCnt;

    //Ewe-Tasker connection vars
    String user,place;
    boolean sendEvent = true;
    RuleExecutionModule ruleExecutionModule;
    CacheMethods cacheMethods;
    private Region region;

    //Arousal calculation vars:
    private int arousal = 0;
    private List<Float> edavalues = new ArrayList<Float>();
    private boolean init = false;
    private Float interval = 0.0f;
    private Float max = 0.0f;
    private Float min = 0.0f;
    private int contador =0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empatica_activity);

        // Ewe-Tasker connection initialization
        ruleExecutionModule = new RuleExecutionModule(getApplicationContext());
        cacheMethods = CacheMethods.getInstance(getApplicationContext());
        user = cacheMethods.getFromPreferences("beaconRuleUser", "enrique");
        place = cacheMethods.getFromPreferences("beaconRulePlace","GSI lab");
        region = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        // Initialize vars that reference UI components
        statusLabel = (TextView) findViewById(R.id.status);
        dataCnt = (RelativeLayout) findViewById(R.id.dataArea);
        accel_xLabel = (TextView) findViewById(R.id.accel_x);
        accel_yLabel = (TextView) findViewById(R.id.accel_y);
        accel_zLabel = (TextView) findViewById(R.id.accel_z);
        bvpLabel = (TextView) findViewById(R.id.bvp);
        edaLabel = (TextView) findViewById(R.id.eda);
        ibiLabel = (TextView) findViewById(R.id.ibi);
        temperatureLabel = (TextView) findViewById(R.id.temperature);
        batteryLabel = (TextView) findViewById(R.id.battery);
        deviceNameLabel = (TextView) findViewById(R.id.deviceName);

        initEmpaticaDeviceManager();

    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted, yay!
                    initEmpaticaDeviceManager();
                } else {
                    // Permission denied, boo!
                    final boolean needRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                    new AlertDialog.Builder(this)
                            .setTitle("Permission required")
                            .setMessage("Without this permission bluetooth low energy devices cannot be found, allow it in order to connect to the device.")
                            .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // try again
                                    if (needRationale) {
                                        // the "never ask again" flash is not set, try again with permission request
                                        initEmpaticaDeviceManager();
                                    } else {
                                        // the "never ask again" flag is set so the permission requests is disabled, try open app settings to enable the permission
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButton("Exit application", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // without permission exit is the only way
                                    finish();
                                }
                            })
                            .show();
                }
                break;
        }
    }


    private void initEmpaticaDeviceManager() {
        // Android 6 (API level 23) now require ACCESS_COARSE_LOCATION permission to use BLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        } else {
            // Create a new EmpaDeviceManager. MainActivity is both its data and status delegate.
            deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);

            if (TextUtils.isEmpty(EMPATICA_API_KEY)) {
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Please insert your API KEY")
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // without permission exit is the only way
                                finish();
                            }
                        })
                        .show();
                return;
            }
            // Initialize the Device Manager using your API key. You need to have Internet access at this point.
            deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
        }
    }


    protected void onPause() {
        super.onPause();
        if (deviceManager != null) {
            deviceManager.stopScanning();
        }
    }


    protected void onDestroy() {
        super.onDestroy();
        if (deviceManager != null) {
            deviceManager.cleanUp();
        }
    }


    public void didDiscoverDevice(BluetoothDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Check if the discovered device can be used with your API key. If allowed is always false,
        // the device is not linked with your API key. Please check your developer area at
        // https://www.empatica.com/connect/developer.php
        if (allowed) {
            // Stop scanning. The first allowed device will do.
            deviceManager.stopScanning();
            try {
                // Connect to the device
                deviceManager.connectDevice(bluetoothDevice);
                updateLabel(deviceNameLabel, "To: " + deviceName);
            } catch (ConnectionNotAllowedException e) {
                // This should happen only if you try to connect when allowed == false.
                Toast.makeText(EmpaticaActivity.this, "Sorry, you can't connect to this device", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void didRequestEnableBluetooth() {
        // Request the user to enable Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // The user chose not to enable Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // You should deal with this
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void didUpdateSensorStatus(EmpaSensorStatus status, EmpaSensorType type) {
        // No need to implement this right now
    }


    public void didUpdateStatus(EmpaStatus status) {
        // Update the UI
        updateLabel(statusLabel, status.name());

        // The device manager is ready for use
        if (status == EmpaStatus.READY) {
            updateLabel(statusLabel, status.name() + " - Turn on your device");
            // Start scanning
            deviceManager.startScanning();
            // The device manager has established a connection
        } else if (status == EmpaStatus.CONNECTED) {
            // Stop streaming after STREAMING_TIME
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataCnt.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Disconnect device
                            deviceManager.disconnect();
                        }
                    }, STREAMING_TIME);
                }
            });
            // The device manager disconnected from a device
        } else if (status == EmpaStatus.DISCONNECTED) {
            updateLabel(deviceNameLabel, "");
        }
    }


    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
        updateLabel(accel_xLabel, "" + x);
        updateLabel(accel_yLabel, "" + y);
        updateLabel(accel_zLabel, "" + z);

    }


    public void didReceiveBVP(float bvp, double timestamp) {
        updateLabel(bvpLabel, "" + bvp);
        //postData(bvp,"eda",region)

    }

    private double calculateAverage(List <Float> marks) {
        if (marks == null || marks.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (Float mark : marks) {
            sum += mark;
        }
        return sum / marks.size();
    }

    public int getArousal(){
        float mean = (float) calculateAverage(edavalues);
        int arousal = 0;

        if(mean > this.min && mean <= (this.min+this.interval)){
            arousal = 1;
        }

        else if(mean >= (this.min+this.interval) &&
                mean < (this.min+2*this.interval)){
            arousal = 2;
        }

        else if(mean >= (this.min+2*this.interval) &&
                mean < (this.min+3*this.interval)){
            arousal = 3;
        }

        else if(mean >= (this.min+3*this.interval) &&
                mean < (this.min+4*this.interval)){
            arousal = 4;
        }

        else if(mean >= (this.min+4*this.interval) &&
                mean <= (this.max)){
            arousal = 5;
        }
        else {
            arousal = 0;
        }
        String mean1 = "" + mean;
        String min = "" + this.min;
        String max = "" + this.max;
        String arousal1 = ""+arousal;
        Log.i("media eda: ", mean1);
        Log.i("min eda: ", min);
        Log.i("max eda: ", max);
        Log.i("arousal: ", arousal1);

        return arousal;
    }

    public void updateMax(){
        this.max = Collections.max(edavalues);
    }

    public void updateMin() {
        this.min = Collections.min(edavalues);
    }

    public void updateInterval() {
        this.interval = (this.max - this.min)/5;
    }

    public void initInterval(){
        this.max = Collections.max(edavalues);
        this.min = Collections.min(edavalues);
        this.interval = (max - min)/5;
    }

    public void didReceiveBatteryLevel(float battery, double timestamp) {
        updateLabel(batteryLabel, String.format("%.0f %%", battery * 100));
    }


    public void didReceiveGSR(float gsr, double timestamp) {
        updateLabel(edaLabel, "" + gsr);
        //postData(gsr,"eda",region);
        if(!init){
            edavalues.add(gsr);
            if(edavalues.size()==20) {
                init = true;
                initInterval();
                this.arousal = 3;
            }
        }
        else{
            edavalues.add(gsr);
            if (Collections.max(edavalues)>this.max){
                updateMax();
                updateInterval();
            }
            else if(Collections.min(edavalues)<this.min){
                updateMin();
                updateInterval();
            }
            edavalues.remove(0);
            if(this.contador<20){
                this.contador++;
            }
            else{
                this.arousal = getArousal();
                postData(this.arousal,"arousal",region);
                this.contador = 0;
            }

        }
    }


    public void didReceiveIBI(float ibi, double timestamp) {
        updateLabel(ibiLabel, "" + ibi);
    }


    public void didReceiveTemperature(float temp, double timestamp) {
        updateLabel(temperatureLabel, "" + temp);
    }

    // Update a label with some text, making sure this is run in the UI thread
    private void updateLabel(final TextView label, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label.setText(text);
            }
        });
    }

    //Post Request to EWE-Tasker
    public void postData(int data, String campo ,Region region){
        String channel="Empatica";
        String event="ArousalLevelEmpatica";
        JSONObject input = new JSONObject();
        try {
            JSONObject params = new JSONObject();
            input.put("channel", channel);
            input.put("user", user);
            input.put("event", event);
            params.put("ArousalLevel", data);
            params.put("ArousalLevel", data);
            input.put("params", params);
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

        Log.i("EMPATICA",response);
        //Send response to RuleExecutionModule
        ruleExecutionModule.handleServerResponse(response);
/*
        Log.i("EMPATICA", campo);

        String event ="@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ." +
                " @prefix ewe: <http://gsi.dit.upm.es/ontologies/ewe/ns/#> ." +
                " @prefix string: <http://www.w3.org/2000/10/swap/string#> ."+
                " @prefix ewe-empatica: <http://gsi.dit.upm.es/ontologies/ewe-empatica/ns/#> ." +
                " ewe-empatica:Band" +
                " rdf:type ewe-empatica:ArousalLevel. ewe-empatica:Band"+
                " ewe:level \"" +
                data +
                "\".";


        String inputEvent = event;
        String[] params = {inputEvent,user};

        //Task send to sever
        String response = "";
        try {
            response = new Tasks.PostInputToServerTask().execute(params).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //Log.i("Countdown", "Beacon delivery finished");
        Log.i("EMPATICA",user);
        Log.i("EMPATICA",inputEvent);
        Log.i("EMPATICA",response);
        //Send response to RuleExecutionModule
        ruleExecutionModule.handleServerResponse(response);

        */
    }




}
