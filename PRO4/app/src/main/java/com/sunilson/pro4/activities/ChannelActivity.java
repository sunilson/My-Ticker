package com.sunilson.pro4.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.R;
import com.sunilson.pro4.fragments.ChannelFragment;
import com.sunilson.pro4.fragments.EditChannelFragment;
import com.sunilson.pro4.interfaces.CanChangeFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChannelActivity extends BaseActivity implements CanChangeFragment {

    private String currentFragment;
    private boolean firstLogin = false;

    @BindView(R.id.content_channel)
    FrameLayout frameLayout;

    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        Intent i = getIntent();
        String type = i.getStringExtra("type");

        if (type == null) {

        } else if (type.equals("editChannel")) {
            replaceFragment(EditChannelFragment.newInstance(), "edit");
        } else if (type.equals("firstLogin")) {
            replaceFragment(EditChannelFragment.newInstance(), "edit");
        } else if (type.equals("view")) {
            String authorID = i.getStringExtra("authorID");
            if (authorID != null) {
                appBarLayout.setVisibility(View.GONE);
                replaceFragment(ChannelFragment.newInstance(authorID), "view");
            } else {
                finish();
            }
        }
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
