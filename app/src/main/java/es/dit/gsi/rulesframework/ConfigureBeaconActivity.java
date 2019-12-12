package es.dit.gsi.rulesframework;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import es.dit.gsi.rulesframework.R;
import es.dit.gsi.rulesframework.performers.DoorPerformer;
import es.dit.gsi.rulesframework.util.CacheMethods;

/**
 * Created by afernandez on 16/03/16.
 */
public class ConfigureBeaconActivity extends ActionBarActivity {

    CacheMethods cacheMethods;
    ImageView beaconIcon;
    EditText editTextUser, editTextPlace;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_beacon);

        cacheMethods = CacheMethods.getInstance(getApplicationContext());
        editTextPlace = (EditText) findViewById(R.id.editTextPlace);
        editTextUser = (EditText) findViewById(R.id.editTextUser);

        editTextUser.setText(cacheMethods.getFromPreferences("beaconRuleUser","public"));
        //editTextPlace.setText(cacheMethods.getFromPreferences("beaconRulePlace","GSI lab"));
        editTextPlace.setText(cacheMethods.getFromPreferences("serverIP","http://api.ewetasker.cluster.gsi.dit.upm.es"));


        beaconIcon = (ImageView) findViewById(R.id.beaconIcon);
        beaconIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = editTextUser.getText().toString();
                String serverIP = editTextPlace.getText().toString();

                if(!user.equals("")){
                    cacheMethods.saveInPreferences("beaconRuleUser",user);
                }
                if(!serverIP.equals("")){
                    cacheMethods.saveInPreferences("serverIP",serverIP);
                }
                Toast.makeText(getApplicationContext(),"Beacon rules settings have been updated",Toast.LENGTH_LONG).show();
                Intent i = new Intent(getApplicationContext(),BeaconActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });
    }
}
