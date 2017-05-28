package com.sunilson.pro4.dialogFragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.sunilson.pro4.R;
import com.sunilson.pro4.utilities.Constants;

/**
 * @author Linus Weiss
 */

public class SettingsDialog extends BaseDialog {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Constants.SHARED_PREF_TAG, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        View view = inflater.inflate(R.layout.settings_dialog, null);

        Switch notifications = (Switch) view.findViewById(R.id.fragment_settings_notification_switch);
        notifications.setChecked(sharedPreferences.getBoolean(Constants.SHARED_PREF_KEY_NOTIFICATIONS, true));
        notifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(Constants.SHARED_PREF_KEY_NOTIFICATIONS, b);
            }
        });

        Switch vibrations = (Switch) view.findViewById(R.id.fragment_settings_vibration_switch);
        vibrations.setChecked(sharedPreferences.getBoolean(Constants.SHARED_PREF_KEY_NOTIFICATIONS_VIBRATION, true));
        vibrations.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                editor.putBoolean(Constants.SHARED_PREF_KEY_NOTIFICATIONS_VIBRATION, b);
            }
        });

        builder.setView(view);

        //Set "Confirm" button
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                editor.commit();
                getDialog().dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                editor.clear();
                getDialog().dismiss();
            }
        });

        Dialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public static SettingsDialog newInstance() {
        SettingsDialog dialog = new SettingsDialog();
        return dialog;
    }

}
