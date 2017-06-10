package com.sunilson.pro4.dialogFragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurgle.camerakit.CameraKit;
import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.sunilson.pro4.R;
import com.sunilson.pro4.utilities.Constants;
import com.sunilson.pro4.utilities.Utilities;

import java.io.File;
import java.io.FileOutputStream;
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
    private ImageView flash, switchCamera;

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
        switchCamera = (ImageView) view.findViewById(R.id.camera_view_switch);

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
                ImageView imageView = (ImageView) view.findViewById(R.id.picture_dialog_image);
                imageView.setImageBitmap(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            switchCameraCaption(true);
            cameraView.start();
            cameraView.setCameraListener(new CameraListener() {
                @Override
                public void onPictureTaken(byte[] jpeg) {
                    super.onPictureTaken(jpeg);
                    result = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);

                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                        if(orientation < 315 && orientation > 225) {
                            Matrix matrix = new Matrix();
                            matrix.postRotate(-90);
                            result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
                        }else if(orientation > 45 && orientation < 135) {
                            Matrix matrix = new Matrix();
                            matrix.postRotate(90);
                            result = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);
                        }
                    }

                    try {
                        File imageFile = Utilities.createImageFile(getContext());
                        FileOutputStream out = new FileOutputStream(imageFile);
                        result.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.close();
                        cameraURI = Uri.fromFile(imageFile);
                        ImageView imageView = (ImageView) view.findViewById(R.id.picture_dialog_image);
                        imageView.setImageBitmap(result);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
                intent.putExtra("image", result);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Constants.PICTURE_DIALOG_RESULT_CODE_SUCCESS, intent);
                getDialog().dismiss();
            }
        });

        Dialog dialog = builder.create();

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
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cameraViewLayout.setVisibility(View.GONE);
                    captionView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            cameraViewLayout.setVisibility(View.VISIBLE);
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
    void orientationChange(int orientation) {
        this.orientation = orientation;
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
