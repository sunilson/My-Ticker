package com.sunilson.pro4.dialogFragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.sunilson.pro4.R;
import com.sunilson.pro4.activities.AuthenticationActivity;

/**
 * @author Linus Weiss
 */

public class RegisterDialog extends BaseDialog {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        //Set "Confirm" button
        builder.setPositiveButton(R.string.register_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Go to authentication activity when "register" is clicked
                getDialog().dismiss();
                Intent i = new Intent(getContext(), AuthenticationActivity.class);
                startActivity(i);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getDialog().dismiss();
            }
        });

        builder.setMessage(getString(R.string.register_required));

        return builder.create();
    }

    public static RegisterDialog newInstance() {
        RegisterDialog dialog = new RegisterDialog();
        return dialog;
    }

}
