package com.sunilson.pro4.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

    private String currentFragment;
    private DatabaseReference mReference;
    private BaseFragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left).setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left).add(R.id.authentication_frameLayout, LoginFragment.newInstance()).commit();
        }
    }

    @Override
    public void replaceFragment(Fragment fragment, String tag) {
        currentFragment = tag;
        if (tag.equals("register")) {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left).replace(R.id.authentication_frameLayout, fragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right).replace(R.id.authentication_frameLayout, fragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFragment.equals("register")) {
            replaceFragment(LoginFragment.newInstance(), "login");
        } else {
            super.onBackPressed();
        }
    }
}
