package com.sunilson.pro4.dialogFragments;

import android.app.Dialog;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sunilson.pro4.R;

/**
 * Created by linus_000 on 10.04.2017.
 */

public class LivetickerPictureViewDialog extends ImageBaseDialog {

    private String imageURL, caption;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        //Retrieve URL of given image
        imageURL = getArguments().getString("imageURL");

        //Inflate the correct layout and set the content of the dialog to that view
        View view = inflater.inflate(R.layout.picture_view_fragment, null);
        builder.setView(view);

        TextView captionView = (TextView) view.findViewById(R.id.fragment_image_view_caption);
        final ImageView imageView = (ImageView) view.findViewById(R.id.fragment_image_view);

        caption = getArguments().getString("caption");
        if (caption == null || caption.isEmpty()) {
            caption = getString(R.string.no_caption);
        }
        captionView.setText(caption);

        //Setting up the Toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_view_image);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.view_menu_download:
                        if (imageView.getDrawable() != null) {
                            MediaStore.Images.Media.insertImage(getContext().getContentResolver(), ((BitmapDrawable)imageView.getDrawable()).getBitmap(), getString(R.string.image_title), caption);
                            Toast.makeText(getContext(), R.string.saved_image, Toast.LENGTH_SHORT).show();
                        }
                        break;
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

        //Loading and display the given image
        if (imageURL != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);
            Glide.with(this).using(new FirebaseImageLoader()).load(storageReference).asBitmap().placeholder(R.drawable.default_placeholder).animate(android.R.anim.fade_in).into(imageView);
        } else {
            getDialog().dismiss();
        }

        //Create dialog and return it
        return builder.create();
    }

    @Override
    void orientationChange(int orientation) {

    }

    @Override
    public void onStart() {
        super.onStart();

        mOrientationListener.disable();
    }

    public static LivetickerPictureViewDialog newInstance(String url, String caption) {
        LivetickerPictureViewDialog dialog = new LivetickerPictureViewDialog();
        Bundle args = new Bundle();
        args.putString("imageURL", url);
        args.putString("caption", caption);
        dialog.setArguments(args);
        return dialog;
    }
}
