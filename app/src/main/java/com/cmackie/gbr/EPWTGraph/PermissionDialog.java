package com.cmackie.gbr.EPWTGraph;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;


public class PermissionDialog extends DialogFragment {
    /*
    This fragment represents a dialog to be displayed to the user in event of them not approving external storage permission.
     */

    //Variable representing the interface
    PermissionListener pListener;

    public PermissionDialog() {
        // Required empty public constructor

    }

    public interface PermissionListener {
        //Interface listener so that activities which have dialogs can hear outcomes of Dialog button presses
        void onDialogPositiveClick(DialogFragment dFragment);

        void onDialogNegativeClick(DialogFragment dFragment);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Set up the elements in the dialog including listeners
        AlertDialog.Builder permissDialog = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        String title = args.getString("title", "");
        String reason = args.getString("reason", "");
        permissDialog.setMessage(reason);
        permissDialog.setTitle(title);
        permissDialog.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Send the positive button event back to the host activity
                pListener.onDialogPositiveClick(PermissionDialog.this);
            }
        });
        permissDialog.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Send the negative button back to the host activity
                pListener.onDialogNegativeClick(PermissionDialog.this);
            }
        });
        return permissDialog.create();
    }

    @Override
    public void onAttach(Activity activity) {
        //This method will ensure the activity implements the interface for sending events
        super.onAttach(activity);
        try {
            pListener = (PermissionListener) activity;
        } catch (ClassCastException e) {
            Toast.makeText(getActivity(), "Error in requesting permissions", Toast.LENGTH_SHORT).show();
        }
    }

}
