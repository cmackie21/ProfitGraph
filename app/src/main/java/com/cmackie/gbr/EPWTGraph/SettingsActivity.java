package com.cmackie.gbr.EPWTGraph;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity {
    //This activity houses the settings fragment and processes if the user wants to go back to OptionActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //Change the title of the page we are on
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsActivity_Fragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If user selects back button take them back to OptionActivity
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                //Call super class to handle unknown user selection
                return super.onOptionsItemSelected(item);
        }
    }
    }





