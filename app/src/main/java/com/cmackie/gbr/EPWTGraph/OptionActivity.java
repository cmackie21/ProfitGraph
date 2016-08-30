package com.cmackie.gbr.EPWTGraph;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class OptionActivity extends AppCompatActivity {

    private final String FILE_NAME = "EPWT v4.0.csv";
    private String user_choice = "";
    private final int CONSTANT = 4;
    private int position = 0;
    private final String[] OPTION_NAMES = {"Population", "Fertility", "Birth", "Mortality", "No. Workers", "Growth of Workforce", "Capital Stock (US$ 2005)", "Constant Capital", "Variable Capital",
            "Surplus Value", "GDP", "Depreciation Rate", "Growth of Labour Productivity", "Accumulation Rate", "Profit", "EQR Profit", "Rate of Surplus Value", "Organic Composition of Capital"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
        if (savedInstanceState != null) {
            Spinner spinner = (Spinner) findViewById(R.id.country_spinner);
            if (spinner != null) {
                position = savedInstanceState.getInt("position");
                spinner.setSelection(position);
            }
        }
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings_, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Spinner spinner = (Spinner) findViewById(R.id.country_spinner);
        if (spinner != null) {
            //Save the position of the spinner
            savedInstanceState.putInt("position", spinner.getSelectedItemPosition());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.action_settings):
                //Settings button has been selected so display that
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
                return true;
            case (R.id.about_page):
                //About page has been selected
                Intent about = new Intent(this, AboutActivity.class);
                startActivity(about);
                return true;
            default:
                //Call super class to handle unknown user selection
                return super.onOptionsItemSelected(item);
        }

    }

    public void getOption(View view) {
        /* This method gets the user selected country and options as selected from the Settings page.
        Data for the corresponding country is collected and then passed to the prepare data method.*/
        Spinner spinner = (Spinner) findViewById(R.id.country_spinner);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (spinner != null) {
            user_choice = String.valueOf(spinner.getSelectedItem());
        }
        Set<String> selections = settings.getStringSet("list_option_key", null);
        if (selections != null) {
            if (selections.isEmpty()) {
                //User must select options for the graph to display.
                displayToast("No options selected, please try again.");
                return;
            } else {
                //User has selected options we we get the country data associated with user options
                boolean use_local = settings.getBoolean("use_file_key", true);
                processData(use_local, selections);
            }
        } else {
            //No options have been selected and this is the first time user is using the app so display message
            displayToast("No options selected, please try again.");
            return;
        }
    }

    private void processData(boolean option, Set<String> selections) {
        /*This method will process data from either the default file or
        the locally processed file depending on the user settings
         */
        CSVReader reader = null;
        try {
            if (!option) {
                //If the user wants to use the locally processed file
                boolean file_check = isFileExist();
                if (file_check) {
                    File user_file = getFilesDir();
                    File[] file_array = user_file.listFiles();
                    for (File current : file_array) {
                        //Find the file name of the user processed file
                        if (isValidName(current.getName())) {
                            //This will be the user processed file
                            FileInputStream fis = getApplicationContext().openFileInput(current.getName());
                            reader = new CSVReader(new InputStreamReader(fis),';');
                        }
                    }
                }else{
                    displayToast("No user selected file found, please process a file if you wish to use this option.");
                    return;
                }
            }else{
                //Use the default static file
                AssetManager manager = getAssets();
                InputStream inputStream = manager.open(FILE_NAME);
                reader = new CSVReader(new InputStreamReader(inputStream), ';');
            }
            /*
            After opening appropriate file get the data associated with the user choice
             */
            List<String[]> data = null;
            if (reader != null) {
                data = reader.readAll();
                reader.close();
            }
            List<String[]> subset = new ArrayList<>();
            //Loop to get all the data for the graph
            if (data != null) {
                for (String[] line : data) {
                    //Comparison of where we are in the file versus the user request
                    int compare_choice = line[0].compareTo(user_choice);
                    if (compare_choice == 0) {
                        //We are at the users requested data
                        subset.add(line);
                    } else if (compare_choice > 0 && (!line[0].equals("Country"))) {
                        //We have passed our country alphabetically so we can stop comparing
                        break;
                    }
                }
            }
            if (subset.size() == 0) {
                //Refresh activity and display message
                displayToast("No data found for " + user_choice);
                return;
            } else {
                prepareData(subset, selections);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }


    private boolean isFileExist() {
        //This method will query whether the user actually has processed a file
        File check_file = getFilesDir();
        File[] file_check_array = check_file.listFiles();
        for (File current : file_check_array) {
            boolean user_file = isValidName(current.getName());
            if (user_file) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidName(String guess_file) {
        //Helper method to check if filename has valid extension
        int dot = guess_file.lastIndexOf(".");
        if (dot > 0) {
            String ext = guess_file.substring(dot + 1);
            if (!ext.equals("csv")) {
                //We only want CSV files for parsing
                return false;
            }else if (ext.equals("csv")){
                return true;
            }
        }
        return false;
    }


    private void prepareData(List<String[]> subset, Set<String> selections) {
        //This method prepares a bundle for the ChartActivity and starts the activity
        Bundle chartBundle = new Bundle();
        //Get the years and user selected data for the graph
        ArrayList<String> year = new ArrayList<>();
        ArrayList<ArrayList<Float>> data = new ArrayList<>();
        ArrayList<String> options = new ArrayList<>();
        float max=Float.MIN_VALUE;
        float min=Float.MAX_VALUE;
        boolean useLog = false;
        for (String option : selections) {
            //Loop over options
            ArrayList<Float> dataSeries = new ArrayList<>();
            int numOption = Integer.parseInt(option);
            //For every option get data required for the option
            options.add(OPTION_NAMES[numOption]);
            for (String[] line : subset) {
                dataSeries.add(Float.valueOf(line[numOption + CONSTANT]));
            }
            //Keep track of maximum of minimum values user has requested
            float tempMax = Collections.max(dataSeries);
            float tempMin = Collections.min(dataSeries);
            if (tempMax>max){
                max = tempMax;
            }
            if (tempMin<min && tempMin!=0){
                min=tempMin;
            }
            //Add user option to overall data they want to see
            data.add(dataSeries);
        }
        for (String[] line : subset) {
            //Loop preparing list of labels for X Axis
            year.add(line[2]);
        }
        if ((max/min)>50){
            //If the biggest and smallest values have a range bigger than factor of 50 then convert values to log scale
            useLog=true;
            for (int i=0;i<data.size();i++){
                //Loop which removes the data entries, converts them to log and then places them back to their original position in ArrayList
                ArrayList<Float> tempEntry = data.get(i);
                data.remove(i);
                ArrayList<Float> convertedValues = convertToLog(tempEntry);
                data.add(i,convertedValues);
            }
        }
        //Putting all the values in the Bundle in preparation for ChartActivity
        chartBundle.putSerializable("Data", data);
        chartBundle.putString("country", user_choice);
        chartBundle.putSerializable("Label", options);
        chartBundle.putSerializable("year", year);
        chartBundle.putBoolean("log",useLog);
        Intent chart_intent = new Intent(this, ChartActivity.class);
        chart_intent.putExtra("Bundle", chartBundle);
        startActivity(chart_intent);
    }

    private void displayToast(String message) {
        //Helper method to display Toasts to the user
        Toast.makeText(OptionActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private ArrayList<Float> convertToLog(ArrayList<Float> list){
        //This method will take all of the values entered into the chart and convert them to the log values
        ArrayList<Float> convertedList = new ArrayList<>();
        for (Float current:list){
            double temp = Math.log(current);
            float newValue = (float) temp;
            convertedList.add(newValue);
        }
        return convertedList;

    }
}
