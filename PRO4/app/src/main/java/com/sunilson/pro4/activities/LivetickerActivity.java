package com.sunilson.pro4.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.R;
import com.sunilson.pro4.fragments.CommentsFragment;
import com.sunilson.pro4.fragments.LivetickerFragment;
import com.sunilson.pro4.interfaces.CanChangeFragment;
import com.sunilson.pro4.utilities.Constants;

import java.util.List;

import butterknife.ButterKnife;

public class LivetickerActivity extends BaseActivity implements CanChangeFragment {

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.liveticker_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_liveticker, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.liveticker_menu_comments:
                replaceFragment(CommentsFragment.newInstance(), Constants.FRAGMENT_COMMENTS_TAG);
        }
        return super.onOptionsItemSelected(item);
    }
}
