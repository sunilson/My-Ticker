package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by linus_000 on 15.03.2017.
 */

public class BaseFragment extends Fragment {

    protected DatabaseReference mReference;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}