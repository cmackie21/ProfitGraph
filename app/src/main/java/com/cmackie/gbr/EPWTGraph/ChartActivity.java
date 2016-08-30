package com.cmackie.gbr.EPWTGraph;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ChartActivity extends AppCompatActivity implements PermissionDialog.PermissionListener {
    /*This class will process the data which has been passed as a parameter from OptionActivity,
    preparing it for the LineChart object before being displayed.  The chart can also be saved if the
    user selected the button from the ActionBar once the user passes the Storage Permission check.
     */

    //Area where chart will be displayed
    protected LineChart mChart;
    //User's selected country
    private String choice;
    //Array of line colours for chart
    private int [] lineColours ={Color.BLACK,Color.GRAY,Color.BLUE,Color.GREEN,Color.RED};
    //Data bundle passed into the Activity
    private Bundle data;
    //Constant for verifying if the storage permission has been approved
    private final int WRITE_EXTERNAL_CODE=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        //Prepare the SupportActionBar for display
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle("Line Graph");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //Set values of variables required
        mChart = (LineChart) findViewById(R.id.chart);
        Intent intent = getIntent();
        data = intent.getBundleExtra("Bundle");
        choice = data.getString("country");
    }

    @Override
    public void onStart(){
        super.onStart();
        if (data!=null){
            setChart(data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Prepares menu layout
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If user selects navigational button take them back to OptionActivity
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            //If user clicks this check permissions
            case (R.id.savePic):
                checkStoragePermission();
                return true;
            default:
                //Let system deal with unknown selection
                return super.onOptionsItemSelected(item);
        }

    }

    private void checkStoragePermission() {
        //This method will check whether the user has granted the permission to write their picture to external storage
        if (Build.VERSION.SDK_INT<= Build.VERSION_CODES.LOLLIPOP_MR1){
            //Users device runs Lollipop or lower so we do not need to check permissions
            savePicture();
        }else{
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //Permission hasn't been granted - we should check if the user has denied this permission before
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //Build a dialog to explain to user why permission is required
                    PermissionDialog storageDialog = new PermissionDialog();
                    Bundle args = new Bundle();
                    Resources res = getResources();
                    args.putString("title", res.getString(R.string.storage_title));
                    args.putString("reason", res.getString(R.string.storage_message));
                    storageDialog.setArguments(args);
                    storageDialog.show(getSupportFragmentManager(),"storage_dialog");
                }else{
                    //Request permission
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_EXTERNAL_CODE);
                }
            }else if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ==PackageManager.PERMISSION_GRANTED){
                //Permission has been granted, we can save the picture.
                savePicture();
            }
        }
    }


    private void savePicture() {
        // This method will save the picture to the users device and display a message once it is done
        Calendar calendar = Calendar.getInstance();
        Date currentTime = calendar.getTime();
        //Getting the time will ensure it is a unique name each time it is saved so no crashing
        String timeString = currentTime.toString();
        String name=choice+" "+timeString;
        LineChart userChart = (LineChart) findViewById(R.id.chart);
        if (userChart!=null){
            userChart.saveToGallery(name,100);
            displayToast("Graph saved to gallery as "+name);
        }
    }

    private void setChart(Bundle data_bundle) {
        /*This method will extract the data packaged in the bundle.  It also sets turns off various settings which are as default by the MPAndroidChart
        * Library to get the desired look for the resulting graph*/
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getAxisRight().setEnabled(false);
        mChart.getXAxis().setDrawGridLines(false);
        ArrayList<ArrayList<Float>> data = (ArrayList<ArrayList<Float>>)data_bundle.getSerializable("Data");
        ArrayList<String> label = (ArrayList<String>)data_bundle.getSerializable("Label");
        ArrayList<String> year = (ArrayList<String>) data_bundle.getSerializable("year");
        boolean useLog = data_bundle.getBoolean("log");
        //The following represents a list of all the individual lines in the chart
        List<ILineDataSet> chartDataSets = new ArrayList<>();
        //Constant for the Line Colour array
        int colourCount=0;
        boolean enableDashed = false;
        for(int i=0; i<data.size(); i++){
            List<Entry> entries = new ArrayList<>();
            ArrayList<Float> current_line=data.get(i);
            for (int j=0; j<current_line.size();j++){
                if (current_line.get(j)==0){
                    //Do nothing
                }else{
                    //Add a new entry to the chart
                    entries.add(new Entry(current_line.get(j),j));
                }
            }
            //Represents a single line on the chart.  Label parameter is for the legend.
            LineDataSet linedata = new LineDataSet(entries,label.get(i));
            if (colourCount>= lineColours.length){
                //Reassign colour value and enable dotted lines if neccessary
                colourCount=0;
                if (!enableDashed){
                    enableDashed=true;
                }else{
                    enableDashed=false;
                }
            }
            linedata.setColor(lineColours[colourCount]);
            if (enableDashed){
                linedata.enableDashedLine(1,1,0);
            }
            linedata.setDrawCircleHole(false);
            linedata.setDrawCircles(false);
            chartDataSets.add(linedata);
            colourCount++;
        }
        mChart.setDescription("");
        mChart.setTouchEnabled(false);
        mChart.setDragEnabled(false);
        YAxis yAxis = mChart.getAxisLeft();
        //The following object contains all of the datasets which the user has requested plus labels for the X Axis
        LineData chartData = new LineData(year,chartDataSets);
        chartData.setDrawValues(false);
        if (useLog){
            yAxis.setValueFormatter(new LogAxisFormatter());
            mChart.setDescription("Log scale");
        }else{
            yAxis.setAxisMinValue(0);
        }
        Legend chart_legend=mChart.getLegend();
        //Set legend representation to line
        chart_legend.setForm(Legend.LegendForm.LINE);
        XAxis xAxis = mChart.getXAxis();
        //X Axis will be at the bottom of the graph
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        mChart.setData(chartData);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dFragment) {
        //User has decided to approve permission
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_EXTERNAL_CODE);

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dFragment) {
        //User has decided not to approve permission so display message
        displayToast("Permission not approved, graph has not been saved.");

    }

    public class LogAxisFormatter implements YAxisValueFormatter {
        //This class will be used for when a log scaled graph is required.  It allows for values on the Y Axis to be custom
        //formatted before being drawn onto the chart.
        private double count;
        public LogAxisFormatter(){
            count=0;
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            //This method rewrites the labels
            double result = Math.log(count);
            count++;
            return String.format("%.2f",result);
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode,
                                           String[] permissions,
                                           int[] grantResults){
        //This method processes the permission result and saves the picture if the permission is granted
        if (requestCode==WRITE_EXTERNAL_CODE){
            if (grantResults.length>0){
                if (grantResults[0]==(PackageManager.PERMISSION_GRANTED)){
                    //Permission has been granted to write to external storage
                    savePicture();
                }else{
                    //Display message to user to say function is not going to be carried out
                    displayToast("Permission not approved, graph has not been saved.");
                }
            }else{
                //Error has occured during requesting permission
                displayToast("Error during saving graph.  Please try again.");
            }
        }

    }

    private void displayToast(String message){
        //Helper method to write Toasts for the user
        Toast.makeText(ChartActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
