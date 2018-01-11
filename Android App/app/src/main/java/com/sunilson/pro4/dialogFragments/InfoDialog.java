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

        final TextView description = (TextView)view.findViewById(R.id.info_dialog_description);
        final TextView status = (TextView)view.findViewById(R.id.info_dialog_status);
        final TextView title = (TextView)view.findViewById(R.id.info_dialog_title);
        final TextView author = (TextView)view.findViewById(R.id.info_dialog_author);

        if (!getArguments().getString("description").isEmpty()) {
             description.setText(getArguments().getString("description"));
        }

        if (!getArguments().getString("status").isEmpty()) {
             status.setText(getArguments().getString("status"));
        }

        if (!getArguments().getString("title").isEmpty()) {
             title.setText(getArguments().getString("title"));
        }

        if (!getArguments().getString("author").isEmpty()) {
             author.setText(getString(R.string.created_by) + " " + getArguments().getString("author"));
        }

        builder.setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getDialog().dismiss();
            }
        });

        return builder.create();
    }

    public static InfoDialog newInstance(String description, String status, String title, String author) {
        InfoDialog dialog = new InfoDialog();
        Bundle args = new Bundle();
        args.putString("description", description);
        args.putString("status", status);
        args.putString("title", title);
        args.putString("author", author);
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
