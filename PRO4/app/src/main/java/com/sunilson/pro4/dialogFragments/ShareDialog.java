package com.sunilson.pro4.dialogFragments;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sunilson.pro4.R;

/**
 * @author Linus Weiss
 */

public class ShareDialog extends BaseDialog {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        View view = inflater.inflate(R.layout.share_dialog, null);
        builder.setView(view);

        final TextView shareText = (TextView)view.findViewById(R.id.share_dialog_text);
        if (getArguments().getString("url") != null && !getArguments().getString("url").isEmpty()) {
            final String url = getArguments().getString("url");
            shareText.setText(url);

            Button button = (Button) view.findViewById(R.id.share_dialog_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("liveticker", url);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getContext(), R.string.copy_success, Toast.LENGTH_SHORT).show();
                }
            });
        }

        return builder.create();
    }

    public static ShareDialog newInstance(String url) {
        ShareDialog dialog = new ShareDialog();
        Bundle args = new Bundle();
        args.putString("url", url);
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
