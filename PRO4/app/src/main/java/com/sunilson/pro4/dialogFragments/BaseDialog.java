package com.sunilson.pro4.dialogFragments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

/**
 * @author Linus Weiss
 */

public class BaseDialog extends DialogFragment {

    protected LayoutInflater inflater;
    protected AlertDialog.Builder builder;
    protected Activity activity;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
