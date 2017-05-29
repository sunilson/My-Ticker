package com.sunilson.pro4.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.R;
import com.sunilson.pro4.fragments.ChannelFragment;
import com.sunilson.pro4.fragments.EditChannelFragment;
import com.sunilson.pro4.fragments.LivetickerListFragment;
import com.sunilson.pro4.interfaces.CanChangeFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChannelActivity extends BaseActivity implements CanChangeFragment {

    private String currentFragment;

    @BindView(R.id.content_channel)
    FrameLayout frameLayout;

    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;

    @BindView(R.id.channel_bar_spinner)
    Spinner spinner;

    @BindView(R.id.channel_spinner_layout)
    RelativeLayout spinnerLayout;

    @BindView(R.id.channel_spinner_title)
    TextView spinnerTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.channel));

        ButterKnife.bind(this);

        Intent i = getIntent();
        String type = i.getStringExtra("type");

        if (type == null) {
            Uri data = i.getData();
            List<String> params = data.getPathSegments();
            if (params != null) {
                if (params.get(1) != null) {
                    String id = params.get(1);
                    if (!id.isEmpty()) {
                        replaceFragment(ChannelFragment.newInstance(id), "view");
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            }
        } else if (type.equals("editChannel")) {
            setTitle(getString(R.string.edit_channel_title));
            replaceFragment(EditChannelFragment.newInstance(), "edit");
        } else if (type.equals("firstLogin")) {
            replaceFragment(EditChannelFragment.newInstance(), "edit");
        } else if (type.equals("view")) {
            String authorID = i.getStringExtra("authorID");
            if (authorID != null) {
                replaceFragment(ChannelFragment.newInstance(authorID), "view");
            } else {
                finish();
            }
        } else if (type.equals("list")) {
            spinnerLayout.setVisibility(View.VISIBLE);
            spinnerTitle.setVisibility(View.VISIBLE);
            String authorID = i.getStringExtra("authorID");
            if (authorID != null) {
                replaceFragment(LivetickerListFragment.newInstance(authorID), "view");
            } else {
                finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void authChanged(FirebaseUser user) {
        if (user != null) {
            if (currentFragment.equals("edit")) {
                if (user.isAnonymous()) {
                    Intent i = new Intent(ChannelActivity.this, MainActivity.class);
                    startActivity(i);
                    Toast.makeText(this, R.string.no_access_permission, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void replaceFragment(Fragment fragment, String tag) {
        currentFragment = tag;
        getSupportFragmentManager().beginTransaction().replace(R.id.content_channel, fragment, tag).commit();
    }
}
