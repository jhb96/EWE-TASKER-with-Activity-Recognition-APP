package es.dit.gsi.rulesframework.services;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import es.dit.gsi.rulesframework.performers.DoorPerformer;
import es.dit.gsi.rulesframework.performers.GestorPerformer;
import es.dit.gsi.rulesframework.util.Constants;
import es.dit.gsi.rulesframework.ListRulesActivity;
import es.dit.gsi.rulesframework.model.Channel;
import es.dit.gsi.rulesframework.model.Event;
import es.dit.gsi.rulesframework.performers.AudioPerformer;
import es.dit.gsi.rulesframework.performers.NotificationPerformer;
import es.dit.gsi.rulesframework.performers.ToastPerformer;
import es.dit.gsi.rulesframework.performers.WifiPerformer;
import es.dit.gsi.rulesframework.util.Tasks;

/**
 * Created by afernandez on 27/10/15.
 */
public class RuleExecutionModule {
    Context context;

    public RuleExecutionModule(Context context){
        this.context=context;
    }

    public void sendInputToEye(JSONObject input){
        //TODO: Input to Server

        //String[] params = {input,user};
        String response = "";
        try {
            response = new Tasks.PostInputToServerTask().execute(input).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        handleServerResponse(response);


        //DEBUG

    }

    //Handle EYE result
    public void handleServerResponse(String respJson){
        String doAction;
        try {
            System.out.println("Consigue entrar en el HANDLER de la REGLA");
            JSONObject response = new JSONObject(respJson);
            //SUCCESS
            JSONArray jsonArray = (JSONArray) response.getJSONArray("args");
            JSONObject jsonObject = (JSONObject) jsonArray.get(0);
            int success = jsonObject.getInt("success");
            if(success == 1){
                JSONArray actions = jsonObject.getJSONArray("actions");
                for(int i = 0 ;i< actions.length(); i++){
                    JSONObject itemAction = (JSONObject) actions.get(i);
                    doAction = itemAction.getString("action");
                    JSONObject doParams = itemAction.getJSONObject("parameters");
                    executeDoResponse(doAction,doParams);
                    Log.i("DoResponse" , "parameters" + doParams);
                    Log.i("DoResponse"," action: "+ doAction );
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void executeDoResponse(String doAction,JSONObject params){
        //Create instance of manager name

        try {
            WifiPerformer wp = new WifiPerformer(context);
            AudioPerformer ap = new AudioPerformer(context);
            switch (doAction){
                //FilterManagers
                case("ShowNavbarNotification"):
                    System.out.println("Entra en Case Show Navbar?");
                    NotificationPerformer np = new NotificationPerformer(context);
                    np.show(params.getString("NavbarNotification"));
                    //np.show("EYYYYYYYYYYY");
                    break;
                case ("ShowToastNotification"):
                    ToastPerformer tp = new ToastPerformer(context);
                    tp.show(params.getString("ToastNotification"));
                    break;
                case ("TurnOnWifi"):
                    wp.turnOn();
                    break;
                case ("TurnOffWifi"):
                    System.out.println("Intenta apagar el wifi?");
                    wp.turnOff();
                    break;
                case ("AudioManager"):
                    ap.setNormalMode();
                    break;
                case ("Silence"):
                    ap.setSilentMode();
                    break;
                case ("Vibration"):
                    ap.setVibrateMode();
                    break;
                case ("OpenDoor"):
                    DoorPerformer doorPerformer = new DoorPerformer(context);
                    doorPerformer.openDoor(context, params.getString("DoorPublicIP"));
                    break;

                /*
                case "DoorLock":
                    DoorPerformer doorPerformer = new DoorPerformer(context);
                    switch (doAction) {
                        case "OpenDoor":
                            doorPerformer.openDoor(context);
                    }break;


                case "CMS":
                    switch (doAction){
                        case "ShowContent":
                            GestorPerformer gestorPerformer1 = new GestorPerformer(context);
                            gestorPerformer1.show(params);
                    }break;
*/
                default:
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String generateInput(String channel, String event) {
        String json = Constants.readPreferences(context, "channelsJson", "");
        //Log.i("Execution",json);
        List<Channel> channelList = new ArrayList<>();
        try {
            channelList = ListRulesActivity.translateJSONtoList(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String input = "";

        /*
        for (Channel ch : channelList){
            if(ch.title.equalsIgnoreCase(channel)){
                for(Event e : ch.events){
                    if (e.title.equalsIgnoreCase(event)){
                        String statement = e.rule.replace("?event","ewe-"+channel.toLowerCase()+":"+channel);
                        input = e.prefix + "\n" + statement;
                    }
                }
            }
        }
        */
        Log.i("Execution",input);
        return input;
    }

    public String getPrefixes(String channel,String event){
        String json = Constants.readPreferences(context,"channelsJson","");
        //Log.i("Execution",json);
        List<Channel> channelList = null;
        try {
            channelList = ListRulesActivity.translateJSONtoList(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String prefix = "";
        /*
        for (Channel ch : channelList){
            if(ch.title.equalsIgnoreCase(channel)){
                for(Event e : ch.events){
                    if (e.title.equalsIgnoreCase(event)){
                        prefix = e.prefix ;
                    }
                }
            }
        }
        */
        return prefix;
    }
}
