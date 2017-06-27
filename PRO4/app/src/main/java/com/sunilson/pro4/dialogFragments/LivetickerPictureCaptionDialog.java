package com.sunilson.pro4.dialogFragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurgle.camerakit.CameraKit;
import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.sunilson.pro4.R;
import com.sunilson.pro4.asyncTasks.RotateImage;
import com.sunilson.pro4.utilities.Constants;

import java.io.File;
import java.io.IOException;

/**
 * @author Linus Weiss
 */

public class LivetickerPictureCaptionDialog extends ImageBaseDialog {

    private Uri imageURI, cameraURI;
    private CoordinatorLayout cameraViewLayout;
    private RelativeLayout captionView;
    private CameraView cameraView;
    private int orientation;
    private Bitmap result;
    private boolean captioning;
    private int flashState = 0;
    private int cameraState = 0;
    private ImageView flash, switchCamera, rotateRight, rotateLeft, imageView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        final View view = inflater.inflate(R.layout.picture_caption_fragment, null);
        builder.setView(view);

        //Setting up the views
        cameraView = (CameraView) view.findViewById(R.id.camera_view);
        captionView = (RelativeLayout) view.findViewById(R.id.caption_view);
        cameraViewLayout = (CoordinatorLayout) view.findViewById(R.id.camera_view_layout);
        FloatingActionButton cameraButton = (FloatingActionButton) view.findViewById(R.id.camera_view_take_picture);
        flash = (ImageView) view.findViewById(R.id.camera_view_flash);
        rotateRight = (ImageView) view.findViewById(R.id.picture_dialog_rotate_right);
        rotateLeft = (ImageView) view.findViewById(R.id.picture_dialog_rotate_left);
        switchCamera = (ImageView) view.findViewById(R.id.camera_view_switch);
        imageView = (ImageView) view.findViewById(R.id.picture_dialog_image);

        cameraView.setFlash(CameraKit.Constants.FLASH_OFF);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.captureImage();
            }
        });

        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flashState == 0) {
                    cameraView.setFlash(CameraKit.Constants.FLASH_AUTO);
                    flash.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_flash_auto_white_24dp));
                    flashState = 1;
                } else if (flashState == 1) {
                    cameraView.setFlash(CameraKit.Constants.FLASH_ON);
                    flash.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_flash_on_white_24dp));
                    flashState = 2;
                } else {
                    cameraView.setFlash(CameraKit.Constants.FLASH_OFF);
                    flash.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_flash_off_white_24dp));
                    flashState = 0;
                }
            }
        });

        rotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RotateImage rotateImage = new RotateImage(true, imageView, ((BitmapDrawable) imageView.getDrawable()).getBitmap());
                rotateImage.execute();
            }
        });

        rotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RotateImage rotateImage = new RotateImage(false, imageView, ((BitmapDrawable) imageView.getDrawable()).getBitmap());
                rotateImage.execute();
            }
        });

        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cameraState == 0) {
                    cameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                    cameraState = 1;
                } else {
                    //cameraView.setFacing(CameraKit.Constants.FACING_BACK);
                    cameraState = 0;
                }
            }
        });

        if (getArguments().getString("imageURI") != null) {
            switchCameraCaption(false);
            //Setting up the given image
            imageURI = Uri.parse(getArguments().getString("imageURI"));
            try {
                result = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageURI);
                imageView.setImageBitmap(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            switchCameraCaption(true);
            cameraView.setCameraListener(new CameraListener() {
                @Override
                public void onPictureTaken(byte[] jpeg) {
                    super.onPictureTaken(jpeg);
                    result = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
                    imageView.setImageBitmap(result);
                    switchCameraCaption(false);
                }
            });
        }

        //Caption Edittext
        final TextView caption = (TextView) view.findViewById(R.id.picture_dialog_caption);
        caption.setHint("Enter Caption here!");

        //Return result
        ImageButton submitButton = (ImageButton) view.findViewById(R.id.picture_dialog_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("caption", caption.getText().toString());
                intent.putExtra("image", ((BitmapDrawable) imageView.getDrawable()).getBitmap());
                getTargetFragment().onActivityResult(getTargetRequestCode(), Constants.PICTURE_DIALOG_RESULT_CODE_SUCCESS, intent);
                getDialog().dismiss();
            }
        });

        Dialog dialog = builder.create();

        /*
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    if (captioning) {
                        switchCameraCaption(true);
                    } else {
                        getDialog().dismiss();
                    }
                }
                return true;
            }
        });
        */

        return dialog;
    }


    @Override
    public void onStop() {
        super.onStop();

        cameraView.stop();
    }

    private void switchCameraCaption(boolean camera) {

        captioning = !camera;

        if (!camera) {
            cameraView.stop();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cameraViewLayout.setVisibility(View.GONE);
                    captionView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            cameraViewLayout.setVisibility(View.VISIBLE);
            cameraView.start();
            captionView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (cameraURI != null) {
            File file = new File(cameraURI.getPath());
            file.delete();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Intent intent = new Intent();
        intent.putExtra("imageURI", imageURI);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Constants.PICTURE_DIALOG_RESULT_CODE_FAILURE, intent);
    }

    public static LivetickerPictureCaptionDialog newInstance(Uri uri) {
        LivetickerPictureCaptionDialog dialog = new LivetickerPictureCaptionDialog();
        Bundle args = new Bundle();
        if (uri != null) {
            args.putString("imageURI", uri.toString());
        }
        dialog.setArguments(args);
        return dialog;
    }
}
