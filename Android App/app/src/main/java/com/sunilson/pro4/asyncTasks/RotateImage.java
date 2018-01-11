package com.sunilson.pro4.asyncTasks;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * @author Linus Weiss
 */

public class RotateImage extends AsyncTask<Void, Void, Bitmap> {

    private WeakReference<ImageView> imageView;
    private WeakReference<Bitmap> bitmap;
    private WeakReference<Boolean> right;

    public RotateImage(Boolean right, ImageView imageView, Bitmap bitmap) {
        //Use weak references to protect against memory overload
        this.imageView = new WeakReference<>(imageView);
        this.bitmap = new WeakReference<>(bitmap);
        this.right = new WeakReference<>(right);
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        //Rotate the given bitmap with a rotation matrix
        Matrix matrix = new Matrix();
        if(right.get()) {
            matrix.postRotate(90);
        } else {
            matrix.postRotate(-90);
        }
        return Bitmap.createBitmap(bitmap.get(), 0, 0, bitmap.get().getWidth(), bitmap.get().getHeight(), matrix, true);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        //Update the imageview with the rotated bitmap after task is done
        imageView.get().setImageBitmap(bitmap);
    }
}
