package com.sunilson.pro4.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import com.sunilson.pro4.R;
import com.sunilson.pro4.fragments.LivetickerFragment;
import com.sunilson.pro4.interfaces.CanChangeFragment;
import com.sunilson.pro4.utilities.Constants;

import butterknife.ButterKnife;

public class LivetickerActivity extends BaseActivity implements CanChangeFragment {

    private String livetickerString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveticker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            Intent i = getIntent();
            if (i != null) {
                livetickerString = i.getStringExtra("liveticker");
            }
            getSupportFragmentManager().beginTransaction().add(R.id.content_liveticker, LivetickerFragment.newInstance(livetickerString), Constants.FRAGMENT_LIVETICKER_TAG).commit();
        }
    }

    @Override
    void initializeAuthListener() {

    }

    @Override
    public void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.content_liveticker, fragment, tag).commit();
    }
}
