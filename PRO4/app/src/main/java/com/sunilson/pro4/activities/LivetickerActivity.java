package com.sunilson.pro4.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.R;
import com.sunilson.pro4.fragments.LivetickerFragment;
import com.sunilson.pro4.interfaces.CanChangeFragment;
import com.sunilson.pro4.utilities.Constants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LivetickerActivity extends BaseActivity implements CanChangeFragment, View.OnClickListener {

    private String livetickerID;
    private String currentFragment;
    private boolean started;
    private FirebaseUser user;

    @BindView(R.id.liveticker_appbar)
    AppBarLayout appBarLayout;

    @BindView(R.id.liveticker_status_image)
    ImageView statusImage;

    @BindView(R.id.title)
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveticker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.liveticker_toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            Intent i = getIntent();

            if (i.getStringExtra(Constants.LIVETICKER_ID) != null) {
                //Started from within app
                livetickerID = i.getStringExtra(Constants.LIVETICKER_ID);
            } else {
                //Started from external URL
                Uri data = i.getData();
                List<String> params = data.getPathSegments();
                if (params != null) {
                    if (params.get(1) != null) {
                        String id = params.get(1);
                        this.livetickerID = id;
                    }
                }
            }

            replaceFragment(LivetickerFragment.newInstance(livetickerID), Constants.FRAGMENT_LIVETICKER_TAG);
        }
    }

    @Override
    protected void authChanged(FirebaseUser user) {
        this.user = user;
        if (user != null && livetickerID != null) {
            if (!started) {
                started = true;
            }
        }
    }

    @Override
    public void replaceFragment(Fragment fragment, String tag) {
        currentFragment = tag;

        if (tag.equals(Constants.FRAGMENT_COMMENTS_TAG)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_liveticker, fragment, tag).addToBackStack(null).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_liveticker, fragment, tag).commit();
        }

        if (tag.equals(Constants.FRAGMENT_LIVETICKER_TAG)) {
            //fab.setVisibility(View.VISIBLE);
        } else {
            //fab.setVisibility(View.GONE);
        }
    }


    public void updateLivetickerState(String value) {
        if (value.equals(Constants.LIVETICKER_NOT_STARTED_STATE)) {
            statusImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.state_not_started));
        } else if (value.equals(Constants.LIVETICKER_STARTED_STATE)) {
            statusImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.state_started));
        } else {

        }
    }

    public void updateLivetickerTitle(String title) {
        this.title.setText(title);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
    }
}
