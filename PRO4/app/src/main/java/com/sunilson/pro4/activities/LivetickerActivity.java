package com.sunilson.pro4.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.sunilson.pro4.R;
import com.sunilson.pro4.fragments.LivetickerFragment;
import com.sunilson.pro4.interfaces.CanChangeFragment;
import com.sunilson.pro4.utilities.Constants;

import java.util.List;

import butterknife.ButterKnife;

public class LivetickerActivity extends BaseActivity implements CanChangeFragment {

    private ValueEventListener livetickerListener;
    private DatabaseReference livetickerReference;
    private String livetickerID;
    private boolean started;

    /*
    @BindView(R.id.fab)
    FloatingActionButton fab;
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveticker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            Intent i = getIntent();

            if (i.getStringExtra("livetickerID") != null) {
                //Started from within app
                livetickerID = i.getStringExtra("livetickerID");
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
    public void onStart() {
        super.onStart();
        if (livetickerReference != null) {
            livetickerReference.addValueEventListener(livetickerListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (livetickerListener != null && livetickerReference != null) {
            livetickerReference.removeEventListener(livetickerListener);
        }
    }

    @Override
    public void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.content_liveticker, fragment, tag).commit();

        if (tag.equals(Constants.FRAGMENT_LIVETICKER_TAG)) {
            //fab.setVisibility(View.VISIBLE);
        } else {
            //fab.setVisibility(View.GONE);
        }
    }
}
