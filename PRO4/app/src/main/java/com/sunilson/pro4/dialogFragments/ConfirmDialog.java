package com.sunilson.pro4.dialogFragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.sunilson.pro4.R;
import com.sunilson.pro4.utilities.Constants;

/**
 * @author Linus Weiss
 */

public class ConfirmDialog extends BaseDialog {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        //Set "Confirm" button
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Intent intent = new Intent();
                intent.putExtra("type", getArguments().getString("type"));
                getTargetFragment().onActivityResult(getTargetRequestCode(), Constants.CONFIRM_DIALOG_SUCCESS, intent);
                getDialog().dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.setMessage(getArguments().getString("text"));

        return builder.create();
    }

    public static ConfirmDialog newInstance(String type, String text) {
        ConfirmDialog dialog = new ConfirmDialog();
        Bundle args = new Bundle();
        args.putString("text", text);
        args.putString("type", type);
        dialog.setArguments(args);
        return dialog;
    }
}
