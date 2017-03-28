package com.sunilson.pro4.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.google.firebase.database.DatabaseReference;
import com.sunilson.pro4.R;
import com.sunilson.pro4.fragments.BaseFragment;
import com.sunilson.pro4.fragments.LoginFragment;
import com.sunilson.pro4.interfaces.CanChangeFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AuthenticationActivity extends AppCompatActivity implements CanChangeFragment {

    @BindView(R.id.authentication_frameLayout)
    FrameLayout frameLayout;

    private DatabaseReference mReference;
    private BaseFragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.authentication_frameLayout, LoginFragment.newInstance()).commit();
        }
    }

    @Override
    public void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.authentication_frameLayout, fragment).commit();
    }
}
