package es.dit.gsi.rulesframework;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.estimote.sdk.SystemRequirementsChecker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import es.dit.gsi.rulesframework.services.RuleExecutionModule;
import es.dit.gsi.rulesframework.util.CacheMethods;
import es.dit.gsi.rulesframework.util.Tasks;

/**
 * Created by afernandez on 14/03/16.
 */
public class ChooseAppActivity extends ActionBarActivity {

    // LinearLayout rulesFrameworkLayout, beaconsLayout, empaticaLayout;
    LinearLayout beaconsLayout, empaticaLayout;
    Button editButton;
    TextView server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_app);

        //La creación de reglas rulesFrameworkLayout se ha suspendido o comentado
        //rulesFrameworkLayout = (LinearLayout) findViewById(R.id.rulesFrameworkLayout);
        beaconsLayout = (LinearLayout) findViewById(R.id.beaconsLayout);
        empaticaLayout = (LinearLayout) findViewById(R.id.empaticaLayout);
        //editButton = (Button) findViewById(R.id.editServer);
        //server = (TextView) findViewById(R.id.server);

        //TEST
        final RuleExecutionModule ruleExecutionModule = new RuleExecutionModule(getApplicationContext());
        //ruleExecutionModule.executeDoResponse("CMS","Show","Library");

        final JSONObject params= new JSONObject();
        try {
            params.put("NavbarNotification", "You should be studying");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Timer t = new Timer();
        TimerTask timerTaskObj = new TimerTask() {
            public void run() {
                ruleExecutionModule.executeDoResponse("ShowNavbarNotification", params);
            }
        };
        t.schedule(timerTaskObj, 30000L);

        //Set IP SERVER
        /*
        CacheMethods cacheMethods = CacheMethods.getInstance(getApplicationContext());
        Tasks.ipServer = cacheMethods.getFromPreferences("ipServer",Tasks.defaultGsiUrl);
        Log.i("URL",Tasks.ipServer);
        */
        //server.setText(Tasks.ipServer);

        beaconsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),BeaconActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });

        empaticaLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),RecognitionActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

    }
}
