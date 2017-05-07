package com.sunilson.pro4.fragments;

import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.baseClasses.User;

/**
 * @author Linus Weiss
 */

public abstract class ChannelBaseFragment extends BaseFragment {

    protected User user;

    public abstract void authChanged(FirebaseUser user);

    public abstract void userChanged(User user);
}
