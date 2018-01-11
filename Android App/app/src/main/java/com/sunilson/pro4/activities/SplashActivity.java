package com.sunilson.pro4.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseUser;

/**
 * @author Linus Weiss
 */

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void authChanged(FirebaseUser user) {

    }
}
