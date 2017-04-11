package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.R;
import com.sunilson.pro4.interfaces.CanChangeFragment;

/**
 * @author Linus Weiss
 */

public class UpdateChannelFragment extends BaseFragment {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        initializeAuthListener();
    }

    public static UpdateChannelFragment newInstance() {
        return new UpdateChannelFragment();
    }

    private void initializeAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
               user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (user.isAnonymous()) {
                        ((CanChangeFragment)getActivity()).replaceFragment(LoginFragment.newInstance(), "egal");
                        Toast.makeText(getContext(), R.string.no_access_permission, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ((CanChangeFragment)getActivity()).replaceFragment(LoginFragment.newInstance(), "egal");
                    Toast.makeText(getContext(), R.string.no_access_permission, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void updateUser(String username, String photoURL) {

    }
}
