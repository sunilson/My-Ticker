package com.sunilson.pro4.interfaces;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by linus_000 on 10.05.2017.
 */

public interface FragmentAuthInterface {
    public void authChanged(FirebaseUser user);
}
