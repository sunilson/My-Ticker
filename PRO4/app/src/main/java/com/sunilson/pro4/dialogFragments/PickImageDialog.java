package com.sunilson.pro4.dialogFragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sunilson.pro4.R;
import com.sunilson.pro4.adapters.SimpleDialogAdapter;
import com.sunilson.pro4.utilities.Constants;

/**
 * @author Linus Weiss
 */

public class PickImageDialog extends BaseDialog {

    private SimpleDialogAdapter simpleDialogAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        View view = inflater.inflate(R.layout.pick_image_dialog, null);
        builder.setView(view);

        final ListView listView = (ListView) view.findViewById(R.id.pick_image_dialog_listView);
        simpleDialogAdapter = new SimpleDialogAdapter(getContext(), R.layout.simple_dialog_row);
        listView.setAdapter(simpleDialogAdapter);
        simpleDialogAdapter.add(getActivity().getString(R.string.camera), R.drawable.ic_camera_black_24dp);
        simpleDialogAdapter.add(getActivity().getString(R.string.gallery), R.drawable.ic_photo_library_black_24dp);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String string = simpleDialogAdapter.getStringAtPosition(i);
                Intent intent = new Intent();
                if (string.equals(getString(R.string.camera))) {
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Constants.PICK_IMAGE_DIALOG_RESULT_CAMERA, intent);
                } else {
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Constants.PICK_IMAGE_DIALOG_RESULT_GALLERY, intent);
                }
                getDialog().dismiss();
            }
        });

        return builder.create();
    }

    public static PickImageDialog newInstance() {
        PickImageDialog pickImageDialog = new PickImageDialog();
        return pickImageDialog;
    }
}
