package com.sunilson.pro4.utilities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by linus_000 on 12.04.2017.
 */

public class Utilities {

    /**
     * @param view         View to animate
     * @param toVisibility Visibility at the end of animation
     * @param toAlpha      Alpha at the end of animation
     * @param duration     Animation duration in ms
     */
    public static void animateView(final View view, final int toVisibility, float toAlpha, int duration) {
        boolean show = toVisibility == View.VISIBLE;
        if (show) {
            view.setAlpha(0);
        }
        view.setVisibility(View.VISIBLE);
        view.animate()
                .setDuration(duration)
                .alpha(show ? toAlpha : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(toVisibility);
                    }
                });
    }

    /**
     * Create file to be used for camera Activity
     *
     * @return File that can be used by Camera Activity to store Image
     * @throws IOException
     */
    public static File createImageFile(Context ctx) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File saveDir = new File(Environment.getExternalStorageDirectory(), "Pro4");
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        return File.createTempFile(imageFileName, ".jpg", saveDir);
    }

    public static String getRealPathFromURI(Uri contentUri, Context ctx) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(ctx, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static boolean requestCameraPermission(Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.REQUEST_CAMERA_PERMISSION);
            return false;
        } else {
            return true;
        }
    }

    public static void setupRoundImageViewWithPlaceholder(ImageView imageView, Context context, String image, int placeholderDrawable) {
        DrawableRequestBuilder<Integer> placeholder = Glide.with(context).load(placeholderDrawable).bitmapTransform(new CropCircleTransformation(context));
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(image);
        Glide.with(context).using(new FirebaseImageLoader()).load(storageReference).thumbnail(placeholder).bitmapTransform(new CropCircleTransformation(context)).crossFade().into(imageView);
    }

    public static void setupRoundImageViewOnlyPlaceholder(ImageView imageView, Context context, int placeholderDrawable) {
        Glide.with(context).load(placeholderDrawable).bitmapTransform(new CropCircleTransformation(context)).crossFade().into(imageView);
    }

    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
