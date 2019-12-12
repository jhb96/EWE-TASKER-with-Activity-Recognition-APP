package es.dit.gsi.rulesframework;

import android.graphics.Color;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.zip.CheckedOutputStream;

public class GraphicsFunctions {

//    private static int[] colors = new int[]{Color.YELLOW, Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN};



    //Lista de colores
    private static int azul = Color.parseColor("#0094ab");
    private static int grisClaro = Color.parseColor("#DBD8D8");
    private static int negro = Color.BLACK;

    private static int[] colors = new int[]{azul,azul,azul,azul,azul,azul};

    private static String[] days = new String[]{"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
    private static String[] activities = new String[]{"Still", "Walking", "Running", "Bicycle", "Vehicle", "Unknown"};
    /*
    private void legend(Chart chart){

        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        ArrayList<LegendEntry> entries = new ArrayList<>();

        for(int i=0; i < detectedActivitiesNew.size(); i++){
            LegendEntry entry = new LegendEntry();
            entry.formColor = colors[i];
            entry.label = ActivityIntentService.getActivityString(this, detectedActivitiesNew.get(i).getType());
            System.out.println("El label es " + entry.label);
            System.out.println("El color es " + entry.formColor);
            entries.add(entry);
        }
        legend.setCustom(entries);
    }
*/


    public static void createCharts2(BarChart barChart, int[] porcentajes) {

        barChart = (BarChart) getSameChart(barChart," ", android.R.color.white, android.R.color.white,500);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(true);
        barChart.setDrawValueAboveBar(true);
        barChart.setMaxVisibleValueCount(100);
        barChart.setPinchZoom(false);
        barChart.setClickable(false);
        barChart.setData(getBarData(porcentajes));
        barChart.invalidate();

        axisX(barChart.getXAxis());
        axisLeft(barChart.getAxisLeft());
        axisRight(barChart.getAxisRight());
    }


    public static void createCharts(ArrayList<DetectedActivity> detectedActivitiesNew, PieChart pieChart) {

        pieChart = (PieChart) getSameChart(pieChart,"Porcentajes", Color.BLACK, Color.GRAY,500);
        pieChart.setHoleRadius(10);
        pieChart.setTransparentCircleRadius(12);
        pieChart.setData(getPieData(detectedActivitiesNew));
        pieChart.invalidate();
        pieChart.setDrawHoleEnabled(false);

    }

    private static void axisLeft(YAxis axis){
        axis.setEnabled(false);
        axis.setSpaceTop(30);
        axis.setAxisMinimum(0);
        axis.setAxisMaximum(100);
        axis.setDrawLabels(false);
        axis.setDrawAxisLine(true);
        axis.setDrawGridLines(false);
        axis.setGranularityEnabled(true);

    }

    private static void axisRight(YAxis axis){
        axis.setEnabled(false);
    }

    private static void axisX(XAxis axis){
        axis.setDrawGridLines(false);
        axis.setGranularityEnabled(true);
        axis.setDrawAxisLine(true);
        axis.setPosition(XAxis.XAxisPosition.BOTTOM);
        axis.setValueFormatter(new IndexAxisValueFormatter(activities));
    }


    private static Chart getSameChart(Chart chart, String description, int textColor, int background, int animateY){
        chart.getDescription().setText(description);
//        chart.getDescription().setTextSize(15);
//        chart.getDescription().setTextColor(textColor);
        //chart.setBackgroundColor(background);
        chart.animateY(animateY);
        chart.setTouchEnabled(false);
        chart.setClickable(false);
        return chart;
    }


    private static BarData getBarData(int[] porcentajes){
        BarDataSet barDataSet = (BarDataSet) getData(new BarDataSet(getBarEntries(porcentajes),""));
        barDataSet.setBarShadowColor(Color.WHITE);
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.9f);
        barData.setValueFormatter(new PercentFormatter());
        return barData;
    }


    private static PieData getPieData(ArrayList<DetectedActivity> detectedActivitiesNew){

        PieDataSet pieDataSet = (PieDataSet) getData(new PieDataSet(getPieEntries(detectedActivitiesNew),""));
        pieDataSet.setSliceSpace(1);
        pieDataSet.setValueFormatter(new PercentFormatter());
        return new PieData(pieDataSet);
    }
/*
    private ArrayList<BarEntry> getBarEntries(){
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < ; i++){
            enties.add(new)
        }

    }
*/

    private static ArrayList<BarEntry> getBarEntries(int[] porcentajes){

            ArrayList<BarEntry> entries = new ArrayList<>();
            for (int i = 0; i < porcentajes.length ; i++){
                entries.add(new BarEntry(i, porcentajes[i]));
            }
            return entries;
    }

    private static ArrayList<PieEntry> getPieEntries(ArrayList<DetectedActivity> detectedActivitiesNew) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        for (DetectedActivity da : detectedActivitiesNew){
            entries.add(new PieEntry(da.getConfidence()));
            System.out.println("Está metiendo bien las confidences?" + da.getConfidence());
        }
        return entries;
    }

    private static DataSet getData(DataSet dataSet){
        dataSet.setColor(azul);
        dataSet.setValueTextSize(10);
        return dataSet;
    }
}
