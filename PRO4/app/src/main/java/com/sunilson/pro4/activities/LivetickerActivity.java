package com.sunilson.pro4.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.R;
import com.sunilson.pro4.fragments.LivetickerFragment;
import com.sunilson.pro4.interfaces.CanChangeFragment;
import com.sunilson.pro4.utilities.Constants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LivetickerActivity extends BaseActivity implements CanChangeFragment {

    private String livetickerID;
    private String currentFragment;
    private boolean started;

    @BindView(R.id.liveticker_appbar)
    AppBarLayout appBarLayout;

    @BindView(R.id.subscribe_button)
    Button subscribeButton;

    @BindView(R.id.liveticker_status_image)
    ImageView statusImage;
    /*
    @BindView(R.id.fab)
    FloatingActionButton fab;
    */

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

            getSupportFragmentManager().beginTransaction().add(R.id.content_liveticker, LivetickerFragment.newInstance(livetickerID), Constants.FRAGMENT_LIVETICKER_TAG).commit();
        }

    }

    @Override
    protected void authChanged(FirebaseUser user) {
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

    public void collapseToolbar(boolean collapse) {
        if(collapse) {
            appBarLayout.setExpanded(false);
        } else {
            appBarLayout.setExpanded(true);
        }
    }

    public void updateSubscriptionStatus(Boolean value) {
        subscribeButton.setEnabled(true);

        if (value) {
            subscribeButton.setVisibility(View.VISIBLE);
            subscribeButton.setText(getString(R.string.subscribed));
        } else {
            subscribeButton.setVisibility(View.VISIBLE);
            subscribeButton.setText(getString(R.string.subscribe));
        }
    }

    public void updateLivetickerStatus(String value) {
        if (value.equals(Constants.LIVETICKER_NOT_STARTED_STATE)) {
            statusImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.state_not_started));
        } else if(value.equals(Constants.LIVETICKER_STARTED_STATE)){
            statusImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.state_started));
        } else {

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
