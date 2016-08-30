package com.cmackie.gbr.EPWTGraph;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class AboutActivity extends AppCompatActivity {
    //This Activity displays information about the application

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setTitle("About");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            //If user selects navigation return to Option Activity
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                //Call super class to deal with item if it is not the nav button
                return super.onOptionsItemSelected(item);
        }

    }
}
