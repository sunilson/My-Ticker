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
import com.sunilson.pro4.utilities.Constants;

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
            replaceFragment(LoginFragment.newInstance(), Constants.FRAGMENT_LOGIN_TAG);
        }
    }

    @Override
    public void replaceFragment(Fragment fragment, String tag) {
        currentFragment = tag;
        if (tag.equals(Constants.FRAGMENT_REGISTER_TAG)) {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left).replace(R.id.authentication_frameLayout, fragment).commit();
        } else if (tag.equals(Constants.FRAGMENT_RESET_PASSWORD_TAG)) {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left).replace(R.id.authentication_frameLayout, fragment).commit();
        } else {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right).replace(R.id.authentication_frameLayout, fragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFragment.equals(Constants.FRAGMENT_REGISTER_TAG)) {
            replaceFragment(LoginFragment.newInstance(), Constants.FRAGMENT_LOGIN_TAG);
        } else if (currentFragment.equals(Constants.FRAGMENT_RESET_PASSWORD_TAG)) {
            replaceFragment(LoginFragment.newInstance(), Constants.FRAGMENT_LOGIN_TAG);
        } else {
            super.onBackPressed();
        }
    }
}
