package es.dit.gsi.rulesframework;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;

import com.google.android.gms.location.DetectedActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;



public class ShowStats extends AppCompatActivity {

    private ArrayList<DetectedActivity> detectedActivities;

    //GRAPHICS ATTRIBUTES
    private BarChart barChart;
    private String[] months = new String[]{"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};


    //COUNTER OF TIMES
    private int stillTime;
    private int onFootTime;
    private int walkingTime;
    private int runningTime;
    private int vehicleTime;
    private int bycicleTime;
    private int unknownTime;
    private int tiltingTime;

    private int totalTime;

    private int beforeTime; //LA HORA DEL ANTERIOR DA QUE ANALICÉ
    private LocalDateTime daTime; //LA HORA DEL DA ANALIZANDO

    //DATES ATTRIBUTES
    private int year;
    private int month;
    private int day;
    private Date actuallyDate;
    private Date firtDayOfWeek;


    private String keyInit;
    private ArrayList<String> arrayKeys;



//////////////////////////////////// "ON" ACTIVITY METHODS /////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_stats);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Button dayButton = (Button) findViewById(R.id.dia);
        Button semanaButton = (Button) findViewById(R.id.semana);
        Button mesButton = (Button) findViewById(R.id.mes);
        barChart = (BarChart) findViewById(R.id.barChart);
        final TextView textView = (TextView) findViewById(R.id.textInfoGraphics);


        //Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        //INITIALIZE TIMES
        initialVariables();
        beforeTime = 0;

        //GET THE DATE OF TODAY (DAY, YEAR, MONTH)
        final LocalDateTime localDate = LocalDateTime.now();
        day = localDate.getDayOfYear();
        year = localDate.getYear();
        month = localDate.getMonthValue();



// PRIMER INTENTO (MEJORABLE)
/*
//RECORREMOS TODOS LOS ARRAYS CON DETECTED ACTIVITIES Y LOS ORDENAMOS POR CONFIDENCE
        daInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Query query = ds.getRef().orderByChild("confidence").limitToLast(1);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {

                        //PARA CADA DETECTED ACTIVITY 0,1,2,3.. NOS QUEDAMOS CON EL DE MAYOR CONFIDENCE
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            DataSnapshot activitieActive = dataSnapshot.getChildren().iterator().next();
                            System.out.println("El array con más probabilidades es: " + activitieActive.getKey());
                            System.out.println("El type del array es " + activitieActive.child("type").getValue(int.class));
                            System.out.println("El confidence correspondiente es" + activitieActive.child("confidence").getValue());

                            //daTime = activitieActive.child("fecha").getValue(LocalDateTime.class);
                            System.out.println(activitieActive.child("fecha").child("dayOfYear").getValue(int.class));
                            System.out.println(day);
                            if(activitieActive.child("fecha").child("dayOfYear").getValue(int.class) == day) {
                                int hoursDA = activitieActive.child("fecha").child("hour").getValue(int.class);
                                int minutes = activitieActive.child("fecha").child("minute").getValue(int.class);
                                int seconds = activitieActive.child("fecha").child("second").getValue(int.class);

                                int totalInSecondsDA = hoursDA * 60 * 60 + minutes * 60 + seconds;
                                int differenceTime = 0;

                                if (beforeTime == 0) {
                                    beforeTime = totalInSecondsDA;
                                } else {
                                    differenceTime = totalInSecondsDA - beforeTime;
                                    beforeTime = totalInSecondsDA;
                                }

                                switch (activitieActive.child("type").getValue(int.class)) {
                                    case 0: //IN VEHICLE
                                        vehicleTime += differenceTime;
                                        break;
                                    case 1: //BYCICLE
                                        bycicleTime += differenceTime;
                                        break;
                                    case 2:
                                        //onFootTime += differenceTime
                                        break;
                                    case 3: //STILL
                                        stillTime += differenceTime;
                                        System.out.println("El timepo se está sumando? " + stillTime);
                                        break;
                                    case 4: //UNKNOWKN
                                        unknownTime += differenceTime;
                                        break;
                                    case 5: //TILTING
                                        tiltingTime += differenceTime;
                                        break;
                                    case 7: //UTILIZA EL MISMO QUE WALKING!!
                                        onFootTime += differenceTime;
                                        System.out.println("El timepo se está sumando? " + onFootTime);
                                        break;
                                    case 8: //RUNNING
                                        runningTime += differenceTime;
                                        break;
                                }
                                totalTime = stillTime + walkingTime + runningTime + bycicleTime + vehicleTime + unknownTime;
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });

*/




////////////////////////////////// BUTTON LISTENERS ////////////////////////////////////////////////

        dayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Porcentajes de cada actividad al día

                System.out.println("EL TIEMPO UNKNOWN HA SIDO" + unknownTime);
                System.out.println("Mientras que el tiempo TOTAL HA SIDO: " + totalTime);
                //int stillPorcentaje = Math.round(stillTime*100/86400);
                //int onfootPorcentaje = Math.round(onFootTime*100/86400);

                try {
                    dayInformation();
                    int stillPorcentaje = Math.round(stillTime * 100 / totalTime);
                    int runningPorcentaje = Math.round(runningTime * 100 / totalTime);
                    int walkingPorcentaje = Math.round(walkingTime * 100 / totalTime);
                    int vehiclePorcentaje = Math.round(vehicleTime * 100 / totalTime);
                    int bicyclePorcentaje = Math.round(bycicleTime * 100 / totalTime);
                    int unknownPorcentaje = Math.round(unknownTime * 100 / totalTime);

                    int[] porcentajes = new int[]{stillPorcentaje, walkingPorcentaje, runningPorcentaje, bicyclePorcentaje, vehiclePorcentaje, unknownPorcentaje};
                    System.out.println("La verguenza de porcentajes es " + porcentajes);

                    initialVariables();

                    GraphicsFunctions.createCharts2(barChart, porcentajes);
                    System.out.println("LLega===");
                    textView.setText("The next graphic shows the percentage of time that you have spent on each activity tracked today");

                }catch (Exception e){
                    e.getMessage();
                }
            }
        });



        semanaButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Porcentajes de cada actividad al día

                System.out.println("EL TIEMPO UNKNOWN HA SIDO" + unknownTime);
                System.out.println("Mientras que el tiempo TOTAL HA SIDO: " + totalTime);
                //int stillPorcentaje = Math.round(stillTime*100/86400);
                //int onfootPorcentaje = Math.round(onFootTime*100/86400);


                try {
                    weekInformation();

                    int stillPorcentaje = Math.round(stillTime * 100 / totalTime);
                    int runningPorcentaje = Math.round(runningTime * 100 / totalTime);
                    int walkingPorcentaje = Math.round(walkingTime * 100 / totalTime);
                    int vehiclePorcentaje = Math.round(vehicleTime * 100 / totalTime);
                    int bicyclePorcentaje = Math.round(bycicleTime * 100 / totalTime);
                    int unknowkPorcentaje = Math.round(unknownTime * 100 / totalTime);
                    int[] porcentajes = new int[]{stillPorcentaje, walkingPorcentaje, runningPorcentaje, bicyclePorcentaje, vehiclePorcentaje, unknowkPorcentaje};

                    GraphicsFunctions.createCharts2(barChart, porcentajes);

                    System.out.println(porcentajes.toString());
                    System.out.println(stillPorcentaje);

                }catch (Exception e){
                    e.getMessage();
                }

            }});




        mesButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Porcentajes de cada actividad al día

                System.out.println("EL TIEMPO UNKNOWN HA SIDO" + unknownTime);
                System.out.println("Mientras que el tiempo TOTAL HA SIDO: " + totalTime);
                //int stillPorcentaje = Math.round(stillTime*100/86400);
                //int onfootPorcentaje = Math.round(onFootTime*100/86400);


                try {
                    int stillPorcentaje = stillTime * 100 / totalTime;
                    int runningPorcentaje = runningTime * 100 / totalTime;
                    int walkingPorcentaje = walkingTime * 100 / totalTime;
                    int vehiclePorcentaje = vehicleTime * 100 / totalTime;
                    int bicyclePorcentaje = bycicleTime * 100 / totalTime;
                    int unknowkPorcentaje = unknownTime * 100 / totalTime;
                    int[] porcentajes = new int[]{stillPorcentaje, walkingPorcentaje, runningPorcentaje, bicyclePorcentaje, vehiclePorcentaje, unknowkPorcentaje};

                    GraphicsFunctions.createCharts2(barChart, porcentajes);

                    System.out.println(porcentajes.toString());
                    System.out.println(stillPorcentaje);

                }catch (Exception e){
                    e.getMessage();
                }

            }});


    }
////////////////////////////////////////////////////////////////////////////////////////////////////




//////////////////////////////////////////// BACK ARROW ////////////////////////////////////////////
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
////////////////////////////////////////////////////////////////////////////////////////////////////




//////////////////////////////////// CALCULATING STATS /////////////////////////////////////////////

    private void dayInformation(){

        //FIREBASE REFS
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference activityActualRef = database.getReference();

        activityActualRef.child("Registro de actividad").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@android.support.annotation.NonNull DataSnapshot dataSnapshot) {
                System.out.println("Entra????");
                initialVariables();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.child("fecha").child("dayOfYear").getValue(int.class) == 342) {

                            int hoursDA = ds.child("fecha").child("hour").getValue(int.class);
                            int minutes = ds.child("fecha").child("minute").getValue(int.class);
                            int seconds = ds.child("fecha").child("second").getValue(int.class);

                            int totalInSecondsDA = hoursDA * 60 * 60 + minutes * 60 + seconds;
                            int differenceTime = 0;

                            if (beforeTime == 0) {
                                beforeTime = totalInSecondsDA;
                            } else {
                                differenceTime = totalInSecondsDA - beforeTime;
                                beforeTime = totalInSecondsDA;
                            }

                            switch (ds.child("type").getValue(int.class)) {
                                case 0: //IN VEHICLE
                                    vehicleTime += differenceTime;
                                    break;
                                case 1: //BYCICLE
                                    bycicleTime += differenceTime;
                                    break;
                                case 2: //ON FOOT
                                    runningTime += differenceTime;
                                    break;
                                case 3: //STILL
                                    stillTime += differenceTime;
                                    System.out.println("El timepo se está sumando? " + stillTime);
                                    break;
                                case 4: //UNKNOWKN
                                    unknownTime += differenceTime;
                                    break;
                                case 5: //TILTING
                                    tiltingTime += differenceTime;
                                    break;
                                case 7: // WALKING
                                    walkingTime += differenceTime;
                                    System.out.println("El timepo se está sumando? " + walkingTime);
                                    break;
                                case 8: //RUNNING
                                    runningTime += differenceTime;
                                    break;
                            }
                        }
                    }
                    totalTime = stillTime + walkingTime + runningTime + bycicleTime + vehicleTime + unknownTime;
                    return;
                }
            }

            @Override
            public void onCancelled(@android.support.annotation.NonNull DatabaseError databaseError) {

            }
        });
    }

    private void weekInformation(){

        //FIREBASE REFS
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference activityActualRef = database.getReference();

        activityActualRef.child("Registro de actividad").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@android.support.annotation.NonNull DataSnapshot dataSnapshot) {
                System.out.println("Entra????");
                initialVariables();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.child("fecha").child("monthValue").getValue(int.class) == 12) {

                            int hoursDA = ds.child("fecha").child("hour").getValue(int.class);
                            int minutes = ds.child("fecha").child("minute").getValue(int.class);
                            int seconds = ds.child("fecha").child("second").getValue(int.class);

                            int totalInSecondsDA = hoursDA * 60 * 60 + minutes * 60 + seconds;
                            int differenceTime = 0;

                            if (beforeTime == 0) {
                                beforeTime = totalInSecondsDA;
                            } else {
                                differenceTime = totalInSecondsDA - beforeTime;
                                beforeTime = totalInSecondsDA;
                            }

                            switch (ds.child("type").getValue(int.class)) {
                                case 0: //IN VEHICLE
                                    vehicleTime += differenceTime;
                                    break;
                                case 1: //BYCICLE
                                    bycicleTime += differenceTime;
                                    break;
                                case 2: //ON FOOT
                                    runningTime += differenceTime;
                                    break;
                                case 3: //STILL
                                    stillTime += differenceTime;
                                    System.out.println("El timepo se está sumando? " + stillTime);
                                    break;
                                case 4: //UNKNOWKN
                                    unknownTime += differenceTime;
                                    break;
                                case 5: //TILTING
                                    tiltingTime += differenceTime;
                                    break;
                                case 7: // WALKING
                                    walkingTime += differenceTime;
                                    System.out.println("El timepo se está sumando? " + walkingTime);
                                    break;
                                case 8: //RUNNING
                                    runningTime += differenceTime;
                                    break;
                            }
                        }
                    }
                    totalTime = stillTime + walkingTime + runningTime + bycicleTime + vehicleTime + unknownTime;
                    return;
                }
            }

            @Override
            public void onCancelled(@android.support.annotation.NonNull DatabaseError databaseError) {

            }
        });

    }

    private void monthInformation(){}

////////////////////////////////////////////////////////////////////////////////////////////////////


/////////////////////////////////////////////// INIT VARIABLES /////////////////////////////////////
    private void initialVariables(){
        stillTime = 0;
        onFootTime = 0;
        //beforeTime= 0;
        vehicleTime = 0;
        totalTime = 0;
        walkingTime = 0;
        tiltingTime = 0;
        runningTime = 0;
        unknownTime = 0;
        bycicleTime = 0;
        tiltingTime = 0;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////


// Fast test of this
/*
    private void pruebas() throws InterruptedException{

        //FIREBASE REFS
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference activityActualRef = database.getReference().child("Registro de actividad");

        int n = 0;

        DetectedActivityInfo da20 = new DetectedActivityInfo(DetectedActivity.WALKING, 96, LocalDateTime.now().minusHours(n++));
        DetectedActivityInfo da40 = new DetectedActivityInfo(DetectedActivity.IN_VEHICLE, 90, LocalDateTime.now().minusHours(n++));
        DetectedActivityInfo da41 = new DetectedActivityInfo(DetectedActivity.IN_VEHICLE, 90, LocalDateTime.now().minusHours(n++));


        DetectedActivityInfo da99 = new DetectedActivityInfo(DetectedActivity.STILL, 96, LocalDateTime.now().minusHours(n++));

//        DetectedActivityInfo da50 = new DetectedActivityInfo(DetectedActivity.RUNNING, 70, LocalDateTime.now().minusMinutes(5));

        DetectedActivityInfo da11 = new DetectedActivityInfo(DetectedActivity.UNKNOWN, 70, LocalDateTime.now().minusHours(n++));
        DetectedActivityInfo da12 = new DetectedActivityInfo(DetectedActivity.STILL, 80, LocalDateTime.now().minusHours(n++));

        DetectedActivityInfo da13 = new DetectedActivityInfo(DetectedActivity.STILL, 60, LocalDateTime.now().minusHours(n++));
        DetectedActivityInfo da28 = new DetectedActivityInfo(DetectedActivity.STILL, 100, LocalDateTime.now().minusHours(n++));
        DetectedActivityInfo da21 = new DetectedActivityInfo(DetectedActivity.STILL, 70, LocalDateTime.now().minusHours(n++));
        DetectedActivityInfo da22 = new DetectedActivityInfo(DetectedActivity.STILL, 80, LocalDateTime.now().minusHours(n++));
        DetectedActivityInfo da10 = new DetectedActivityInfo(DetectedActivity.STILL, 100, LocalDateTime.now().minusHours(n++));
        DetectedActivityInfo da14 = new DetectedActivityInfo(DetectedActivity.STILL, 100, LocalDateTime.now().minusHours(n++));
        DetectedActivityInfo da15 = new DetectedActivityInfo(DetectedActivity.STILL, 100, LocalDateTime.now().minusHours(n++));
        DetectedActivityInfo da16 = new DetectedActivityInfo(DetectedActivity.STILL, 100, LocalDateTime.now().minusHours(n++));
        DetectedActivityInfo da17 = new DetectedActivityInfo(DetectedActivity.STILL, 100, LocalDateTime.now().minusHours(n++));



//        DetectedActivityInfo da20 = new DetectedActivityInfo(DetectedActivity.WALKING, 100, LocalDateTime.now().minusHours(n++));
//        DetectedActivityInfo da21 = new DetectedActivityInfo(DetectedActivity.WALKING, 98, LocalDateTime.now().minusHours(n++));
//        DetectedActivityInfo da22 = new DetectedActivityInfo(DetectedActivity.WALKING, 98, LocalDateTime.now().minusHours(n++));
//        DetectedActivityInfo da23 = new DetectedActivityInfo(DetectedActivity.WALKING, 100, LocalDateTime.now().minusHours(n++));
//        DetectedActivityInfo da24 = new DetectedActivityInfo(DetectedActivity.WALKING, 50, LocalDateTime.now().minusHours(n++));

//        DetectedActivityInfo da40 = new DetectedActivityInfo(DetectedActivity.IN_VEHICLE, 70, LocalDateTime.now().minusHours(n++));
//        DetectedActivityInfo da41 = new DetectedActivityInfo(DetectedActivity.IN_VEHICLE, 70, LocalDateTime.now().minusHours(n++));
//        DetectedActivityInfo da42 = new DetectedActivityInfo(DetectedActivity.IN_VEHICLE, 70, LocalDateTime.now().minusHours(n++));
//        DetectedActivityInfo da43 = new DetectedActivityInfo(DetectedActivity.IN_VEHICLE, 70, LocalDateTime.now().minusHours(n++));
//        DetectedActivityInfo da44 = new DetectedActivityInfo(DetectedActivity.IN_VEHICLE, 40, LocalDateTime.now().minusHours(n++));

//        DetectedActivityInfo da50 = new DetectedActivityInfo(DetectedActivity.RUNNING, 70, LocalDateTime.now().minusHours(n++));
//        DetectedActivityInfo da51 = new DetectedActivityInfo(DetectedActivity.RUNNING, 70, LocalDateTime.now().minusHours(n++));
//        DetectedActivityInfo da52 = new DetectedActivityInfo(DetectedActivity.RUNNING, 70, LocalDateTime.now().minusHours(n++));
//        DetectedActivityInfo da53 = new DetectedActivityInfo(DetectedActivity.RUNNING, 70, LocalDateTime.now().minusHours(n++));


            activityActualRef.push().setValue(da17);
            activityActualRef.push().setValue(da16);

            activityActualRef.push().setValue(da15);

            activityActualRef.push().setValue(da14);

            activityActualRef.push().setValue(da10);

            activityActualRef.push().setValue(da22);

            activityActualRef.push().setValue(da21);

            activityActualRef.push().setValue(da28);

            activityActualRef.push().setValue(da13);

            activityActualRef.push().setValue(da12);
            activityActualRef.push().setValue(da11);
//            activityActualRef.push().setValue(da50);
            activityActualRef.push().setValue(da99);
            activityActualRef.push().setValue(da41);
            activityActualRef.push().setValue(da40);
            activityActualRef.push().setValue(da20);


//
//




//            activityActualRef.push().setValue(da17);
//            activityActualRef.push().setValue(da16);

//            activityActualRef.push().setValue(da15);
//            activityActualRef.push().setValue(da14);
//            activityActualRef.push().setValue(da10);


    }
    */

}
