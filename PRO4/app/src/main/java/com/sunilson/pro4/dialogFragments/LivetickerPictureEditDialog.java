package com.sunilson.pro4.dialogFragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunilson.pro4.R;
import com.sunilson.pro4.utilities.Constants;

import java.io.IOException;

/**
 * Created by linus_000 on 09.04.2017.
 */

public class LivetickerPictureEditDialog extends ImageBaseDialog {

    private Uri imageURI;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        View view = inflater.inflate(R.layout.picture_edit_fragment, null);
        builder.setView(view);

        //Setting up the toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_view_image);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        //Setting up the given image
        imageURI = Uri.parse(getArguments().getString("imageURI"));
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageURI);
            ImageView imageView = (ImageView) view.findViewById(R.id.picture_dialog_image);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //Caption Edittext
        final TextView caption = (TextView) view.findViewById(R.id.picture_dialog_caption);
        caption.setHint("Enter Caption here!");

        //Return result
        Button submitButton = (Button) view.findViewById(R.id.picture_dialog_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("caption", caption.getText().toString());
                intent.putExtra("imageURI", imageURI);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Constants.PICTURE_DIALOG_RESULT_CODE_SUCCESS, intent);
                getDialog().dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Intent intent = new Intent();
        intent.putExtra("imageURI", imageURI);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Constants.PICTURE_DIALOG_RESULT_CODE_FAILURE, intent);
    }

    public static LivetickerPictureEditDialog newInstance(Uri uri) {
        LivetickerPictureEditDialog dialog = new LivetickerPictureEditDialog();
        Bundle args = new Bundle();
        args.putString("imageURI", uri.toString());
        dialog.setArguments(args);
        return dialog;
    }
}
