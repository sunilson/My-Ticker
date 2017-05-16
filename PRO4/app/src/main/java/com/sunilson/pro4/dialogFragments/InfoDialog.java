package com.sunilson.pro4.dialogFragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.sunilson.pro4.R;

/**
 * @author Linus Weiss
 */

public class InfoDialog extends BaseDialog {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        View view = inflater.inflate(R.layout.info_dialog, null);
        builder.setView(view);

        final TextView shareText = (TextView)view.findViewById(R.id.info_dialog_text);
        if (!getArguments().getString("text").isEmpty()) {
            shareText.setText(getArguments().getString("text"));
        }

        builder.setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getDialog().dismiss();
            }
        });

        return builder.create();
    }

    public static InfoDialog newInstance(String text) {
        InfoDialog dialog = new InfoDialog();
        Bundle args = new Bundle();
        args.putString("text", text);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Apply open and close animation to Dialog
        if (getDialog().getWindow() != null) {
            getDialog().getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        }
    }

}
