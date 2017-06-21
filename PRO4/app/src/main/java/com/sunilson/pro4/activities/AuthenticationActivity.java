package com.sunilson.pro4.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.sunilson.pro4.R;
import com.sunilson.pro4.fragments.LoginFragment;
import com.sunilson.pro4.interfaces.CanChangeFragment;
import com.sunilson.pro4.utilities.Constants;

import butterknife.ButterKnife;

public class AuthenticationActivity extends AppCompatActivity implements CanChangeFragment {

    private String currentFragment;

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

    /**
     * Replace the current fragment in the FrameLayout
     *
     * @param fragment Instance of new Fragment
     * @param tag Tag of new fragment
     */
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

    /**
     * Override back action if certain fragments are active
     */
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
