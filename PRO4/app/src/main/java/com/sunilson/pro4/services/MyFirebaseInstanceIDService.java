package com.sunilson.pro4.services;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.sunilson.pro4.utilities.Constants;

/**
 * @author Linus Weiss
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String token = FirebaseInstanceId.getInstance().getToken();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !user.isAnonymous() && user.isEmailVerified()) {
            FirebaseDatabase.getInstance().getReference("registrationTokens/" + user.getUid() + "/" + token).setValue(true);
        }

        Log.i(Constants.LOGGING_TAG, token);
    }
}
