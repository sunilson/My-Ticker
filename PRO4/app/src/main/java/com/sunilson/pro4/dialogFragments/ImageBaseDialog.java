package com.sunilson.pro4.dialogFragments;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.ViewGroup;

/**
 * @author Linus Weiss
 */

public abstract class ImageBaseDialog extends DialogFragment {

    protected LayoutInflater inflater;
    protected AlertDialog.Builder builder;
    protected Activity activity;
    protected OrientationEventListener mOrientationListener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Basic functionality of AlertDialog. Same for all dialogs
        builder = new AlertDialog.Builder(getActivity());
        activity = getActivity();
        inflater = getActivity().getLayoutInflater();

        return super.onCreateDialog(savedInstanceState);
    }

}
