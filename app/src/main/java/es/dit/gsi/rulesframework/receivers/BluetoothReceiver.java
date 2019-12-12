package es.dit.gsi.rulesframework.receivers;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import es.dit.gsi.rulesframework.services.RuleExecutionModule;
import es.dit.gsi.rulesframework.util.CacheMethods;

/**
 * Created by afernandez on 26/10/15.
 */
public class BluetoothReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        CacheMethods cacheMethods = CacheMethods.getInstance(context);
        String user=cacheMethods.getFromPreferences("beaconRuleUser","public");
        String channel="SmartPhone";
            //String input = "";
        JSONObject input = new JSONObject();
        try {
            JSONObject params = new JSONObject();
            input.put("channel", channel);
            input.put("user", user);
            input.put("params", params);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            //EYE Functions
            RuleExecutionModule ruleExecutionModule = new RuleExecutionModule(context);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    //"Bluetooth off"
                    Log.i("RULESFW", "Bluetooth OFF");
                    //input = ruleExecutionModule.generateInput(channel,"Turn OFF");
                    try {
                        input.put("event", "BluetoothTurnedOff");
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    ruleExecutionModule.sendInputToEye(input);
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    //"Turning Bluetooth off..."
                    break;
                case BluetoothAdapter.STATE_ON:
                    //"Bluetooth on"
                    Log.i("RULESFW", "Bluetooth ON");
                    try {
                        input.put("event", "BluetoothTurnedOn");
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    //Send input to EYE
                    //input = ruleExecutionModule.generateInput(channel,"Turn ON");
                    ruleExecutionModule.sendInputToEye(input);
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    //"Turning Bluetooth on..."
                    break;
            }
    }
}
