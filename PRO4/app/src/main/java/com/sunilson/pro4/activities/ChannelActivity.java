package com.sunilson.pro4.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.R;
import com.sunilson.pro4.fragments.EditChannelFragment;
import com.sunilson.pro4.fragments.LoginFragment;
import com.sunilson.pro4.interfaces.CanChangeFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChannelActivity extends BaseActivity implements CanChangeFragment {

    private String currentFragment;

    @BindView(R.id.content_channel)
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        Intent i = getIntent();
        String type = i.getStringExtra("type");

        if (type == null) {

        } else if (type.equals("editChannel")){
            replaceFragment(EditChannelFragment.newInstance(), "edit");
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void authChanged(FirebaseUser user) {
        if (user != null) {
            if (user.isAnonymous()) {
                replaceFragment(LoginFragment.newInstance(), "edit");
                Toast.makeText(this, R.string.no_access_permission, Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, MainActivity.class);
            } else {
                if (currentFragment.equals("edit")) {
                    EditChannelFragment editChannelFragment = (EditChannelFragment) getSupportFragmentManager().findFragmentByTag(currentFragment);
                    if (editChannelFragment != null && editChannelFragment.isVisible()) {
                        editChannelFragment.loadUserData(user);
                    }
                }
            }
        } else {
            Toast.makeText(this, R.string.no_access_permission, Toast.LENGTH_SHORT).show();
            signInAnonymously();
        }
    }

    @Override
    public void replaceFragment(Fragment fragment, String tag) {
        currentFragment = tag;
        getSupportFragmentManager().beginTransaction().replace(R.id.content_channel, fragment, tag).commit();
    }
}
