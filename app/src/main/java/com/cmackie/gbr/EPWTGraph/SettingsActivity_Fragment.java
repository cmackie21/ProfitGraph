package com.cmackie.gbr.EPWTGraph;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Toast;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


public class SettingsActivity_Fragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    //This Fragment

    private final String SETTING_KEY = "url_file";
    private Context activityContext;
    private final String ALLOWED_EXT = "csv";

    public SettingsActivity_Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //Load preferences from xml file
        addPreferencesFromResource(R.xml.preferences);
        activityContext = getActivity().getApplicationContext();
        return inflater.inflate(R.layout.fragment_settings_activity_, container, false);
    }

    @Override
    public void onPause() {
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        //This listener listens for if the user inputs a new url to download a file or if the local file is to be used
        if (s.equals(SETTING_KEY)) {
            //Before proceeding with file processing we need to make sure a connection is available
            boolean networkCheck = checkNetworkState();
            if (networkCheck){
                processDownload();
            }else{
                displayToast("Internet connectivity required for downloading new EPWT file.");
            }

        }
    }

    private boolean checkNetworkState(){
        //This method will check if there is a network connection available to process a download]
        ConnectivityManager manager = (ConnectivityManager) activityContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo status = manager.getActiveNetworkInfo();
        if (status!=null){
            return status.isConnected();
        }
        return false;
    }

    private void processDownload() {
        //This method performs the downloading of a file for a user
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activityContext);
        String userUrl = settings.getString("url_file", null);
        final String guessFile = URLUtil.guessFileName(userUrl, null, null);
        boolean nameCheck = isValidName(guessFile);
        if (nameCheck){
            //Valid file name so we shall check if it is a valid URL
            boolean urlCheck = false;
            if (userUrl != null) {
                urlCheck = Patterns.WEB_URL.matcher(userUrl).matches();
            }
            if (urlCheck) {
                //If the URL passes we will check if the file already exists on internal storage
                try {
                    boolean fileExist = duplicateFileCheck(guessFile);
                    if (!fileExist){
                        //It doesn't exist on internal storage we shall process the file after deleting any previously processed file
                        String checkPrevious = doesFileExist();
                        if (!checkPrevious.equals("")){
                            displayToast("Previously processed file being deleted.");
                            //User has previously processed a file, we shall remove it before processing the new file
                            activityContext.deleteFile(checkPrevious);
                        }
                        URL fileURL = new URL(userUrl);
                        FileProcessor processTask = new FileProcessor(fileURL, activityContext, guessFile);
                        displayToast("Processing "+guessFile);
                        processTask.start();
                        displayToast("Finished processing.");
                    }else{
                        displayToast("File already exists on internal storage.");
                        return;
                    }
                } catch (MalformedURLException e) {
                    displayToast("Invalid URL.");
                    return;
                }
            } else {
                displayToast("Invalid URL.");
            }
        }else{
            displayToast("Must be a CSV file for processing.");
        }
    }

    private boolean isValidName(String guessFile) {
        //Helper method to check if filename has valid extension
        int dot = guessFile.lastIndexOf(".");
        if (dot > 0) {
            String ext = guessFile.substring(dot + 1);
            if (!ext.equals(ALLOWED_EXT)) {
                //We only want CSV files for parsing
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
    }

    private boolean duplicateFileCheck(String guessFile) {
        //Helper method to check if file exists before writing
        File file = new File(String.valueOf(activityContext.getFilesDir()),guessFile);
        return file.exists();
    }

    private void displayToast(String message){
        //Helper method to write Toasts for the user
        Toast.makeText(activityContext, message, Toast.LENGTH_SHORT).show();
    }

    @NonNull
    private String doesFileExist() {
        //This method will query whether the user has processed a file previously
        File checkFile = activityContext.getFilesDir();
        File[] fileArray = checkFile.listFiles();
        for (File current : fileArray) {
            boolean userFile = isValidName(current.getName());
            if (userFile) {
                return current.getName();
            }
        }
        return "";
    }


}





