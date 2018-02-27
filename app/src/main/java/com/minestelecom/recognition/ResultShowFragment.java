package com.minestelecom.recognition;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Pierre on 27/02/2018.
 */
public class ResultShowFragment extends DialogFragment {

    private DialogInterface.OnClickListener positive = null;
    private DialogInterface.OnClickListener negative = null;

    private String gotResult = null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setCancelable(false)
                .setTitle(R.string.predictionResultTitle)
                .setMessage(gotResult)
                .setPositiveButton(R.string.validateResult, positive)
                .setNegativeButton(R.string.rejectResult, negative);

        // Create the AlertDialog object and return it
        return builder.create();
    }


    public DialogInterface.OnClickListener getPositive() {
        return positive;
    }

    public void setPositive(DialogInterface.OnClickListener positive) {
        this.positive = positive;
    }

    public DialogInterface.OnClickListener getNegative() {
        return negative;
    }

    public void setNegative(DialogInterface.OnClickListener negative) {
        this.negative = negative;
    }

    public void setGotResult(String gotResult) {
        this.gotResult = gotResult;
    }
}