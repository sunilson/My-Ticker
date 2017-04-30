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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.flurgle.camerakit.CameraKit;
import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.sunilson.pro4.R;
import com.sunilson.pro4.utilities.Constants;
import com.sunilson.pro4.utilities.Utilities;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Linus Weiss
 */

public class LivetickerPicktureCropDialog extends ImageBaseDialog {

    private Uri imageURI, cameraURI;
    private CameraView cameraView;
    private CropImageView cropImageView;
    private RelativeLayout cameraViewLayout;
    private int orientation;
    private boolean cropping;
    private boolean fromGallery;
    private int flashState = 0;
    private int cameraState = 0;
    private ImageView flash, switchCamera;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        View view = inflater.inflate(R.layout.picture_crop_fragment, null);
        builder.setView(view);

        //Setting up the Crop View and the Camera view
        cropImageView = (CropImageView) view.findViewById(R.id.crop_image_view);
        cameraViewLayout = (RelativeLayout) view.findViewById(R.id.camera_view_layout);
        cameraView = (CameraView) view.findViewById(R.id.camera_view);
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
                if(flashState == 0) {
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
                if(cameraState == 0) {
                    cameraView.setFacing(CameraKit.Constants.FACING_FRONT);
                    cameraState = 1;
                } else {
                    cameraView.setFacing(CameraKit.Constants.FACING_BACK);
                    cameraState = 0;
                }
            }
        });

        //Get aspect arguments for cropping
        if (getArguments().getBoolean("aspectFixed")) {
            cropImageView.setAspectRatio(getArguments().getInt("x"), getArguments().getInt("y"));
        }

        //Get URI if the dialog is called with an existing image, otherwise start camera
        if (getArguments().getString("imageURI") != null) {
            imageURI = Uri.parse(getArguments().getString("imageURI"));
        }
        if (imageURI != null) {
            cropImageView.setImageUriAsync(imageURI);
            switchCameraCrop(false);
            fromGallery = true;
        } else {
            //Setting up the Camera View
            switchCameraCrop(true);
            cameraView.start();
            cameraView.setCameraListener(new CameraListener() {
                @Override
                public void onPictureTaken(byte[] jpeg) {
                    super.onPictureTaken(jpeg);
                    Bitmap result = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);

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
                        cropImageView.setImageUriAsync(cameraURI);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    switchCameraCrop(false);
                }
            });
        }

        /*
        Intent intent = new Intent();
        intent.putExtra("image", cropImageView.getCroppedImage());
        getTargetFragment().onActivityResult(getTargetRequestCode(), Constants.PICTURE_DIALOG_CROP_RESULT_CODE_SUCCESS, intent);
        getDialog().dismiss();
        */

        Dialog dialog = builder.create();

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    if (fromGallery) {
                        getDialog().dismiss();
                    }

                    if(cropping) {
                        switchCameraCrop(true);
                    } else {
                        getDialog().dismiss();
                    }
                }
                return true;
            }
        });

        return dialog;
    }

    private void switchCameraCrop(boolean camera) {
        cropping = !camera;
        if (!camera) {
            cameraViewLayout.setVisibility(View.GONE);
            cropImageView.setVisibility(View.VISIBLE);
        } else {
            if (cameraURI != null) {
                File file = new File(cameraURI.getPath());
                file.delete();
            }
            cameraViewLayout.setVisibility(View.VISIBLE);
            cropImageView.setVisibility(View.GONE);
        }
    }


    @Override
    public void onStop() {
        super.onStop();

        cameraView.stop();
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
        getTargetFragment().onActivityResult(getTargetRequestCode(), Constants.PICTURE_DIALOG__CROP_RESULT_CODE_FAILURE, intent);
    }

    public static LivetickerPicktureCropDialog newInstance(Uri uri, Boolean aspectFixed, Integer x, Integer y) {
        LivetickerPicktureCropDialog dialog = new LivetickerPicktureCropDialog();
        Bundle args = new Bundle();
        if (uri != null) {
            args.putString("imageURI", uri.toString());
        }
        if (x != null && y != null) {
            args.putInt("x", x);
            args.putInt("y", y);
        }
        args.putBoolean("aspectFixed", aspectFixed);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    void orientationChange(int orientation) {
        this.orientation = orientation;
    }
}
