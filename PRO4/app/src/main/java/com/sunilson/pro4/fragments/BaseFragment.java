package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import butterknife.Unbinder;

/**
 * Created by linus_000 on 15.03.2017.
 */

public class BaseFragment extends Fragment {

    protected Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}