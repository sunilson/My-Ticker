package com.sunilson.pro4.dialogFragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sunilson.pro4.R;

/**
 * Created by linus_000 on 10.04.2017.
 */

public class LivetickerPictureViewDialog extends ImageBaseDialog {

    private String imageURL;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        //Retrieve URL of given image
        imageURL = getArguments().getString("imageURL");

        //Inflate the correct layout and set the content of the dialog to that view
        View view = inflater.inflate(R.layout.picture_view_fragment, null);
        builder.setView(view);

        //Setting up the Toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_caption_image);
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

        //Loading and display the given image
        ImageView imageView = (ImageView) view.findViewById(R.id.fragment_image_view);
        if (imageURL != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageURL);
            Glide.with(this).using(new FirebaseImageLoader()).load(storageReference).into(imageView);
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

    public static LivetickerPictureViewDialog newInstance(String url) {
        LivetickerPictureViewDialog dialog = new LivetickerPictureViewDialog();
        Bundle args = new Bundle();
        args.putString("imageURL", url);
        dialog.setArguments(args);
        return dialog;
    }
}
