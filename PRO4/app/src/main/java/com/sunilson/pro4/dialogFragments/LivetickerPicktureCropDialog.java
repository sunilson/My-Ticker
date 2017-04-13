package com.sunilson.pro4.dialogFragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.sunilson.pro4.R;
import com.sunilson.pro4.utilities.Constants;
import com.sunilson.pro4.utilities.Utilities;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;

/**
 * @author Linus Weiss
 */

public class LivetickerPicktureCropDialog extends ImageBaseDialog {

    private Uri imageURI;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        View view = inflater.inflate(R.layout.picture_crop_fragment, null);
        builder.setView(view);

        //Setting up the Crop View with given image
        final CropImageView cropImageView = (CropImageView) view.findViewById(R.id.crop_image_view);
        imageURI = Uri.parse(getArguments().getString("imageURI"));
        if (getArguments().getBoolean("aspectFixed")) {
            cropImageView.setAspectRatio(getArguments().getInt("x"), getArguments().getInt("y"));
        }
        cropImageView.setImageUriAsync(imageURI);

        //Setting up the toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_crop_image);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.crop_menu_done) {
                    Intent intent = new Intent();
                    intent.putExtra("image", cropImageView.getCroppedImage());
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Constants.PICTURE_DIALOG_CROP_RESULT_CODE_SUCCESS, intent);
                    getDialog().dismiss();
                    return true;
                }
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


        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Intent intent = new Intent();
        getTargetFragment().onActivityResult(getTargetRequestCode(), Constants.PICTURE_DIALOG__CROP_RESULT_CODE_FAILURE, intent);
    }

    private void getAndSaveImage() {
        File tempFile = null;

        try {
            tempFile = Utilities.createImageFile(getContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri cropURI = FileProvider.getUriForFile(getContext(), "com.sunilson.pro4.fileprovider", tempFile);
    }

    public static LivetickerPicktureCropDialog newInstance(Uri uri, Boolean aspectFixed, Integer x, Integer y) {
        LivetickerPicktureCropDialog dialog = new LivetickerPicktureCropDialog();
        Bundle args = new Bundle();
        args.putString("imageURI", uri.toString());
        if (x != null && y != null) {
            args.putInt("x", x);
            args.putInt("y", y);
        }
        args.putBoolean("aspectFixed", aspectFixed);
        dialog.setArguments(args);
        return dialog;
    }

}
