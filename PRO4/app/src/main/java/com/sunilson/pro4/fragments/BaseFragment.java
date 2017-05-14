package com.sunilson.pro4.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.sunilson.pro4.activities.ChannelActivity;

import butterknife.Unbinder;

/**
 * @author Linus Weiss
 */

public abstract class BaseFragment extends Fragment {

    protected Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void openChannel(String authorID) {
        if (authorID != null) {
            Intent i = new Intent(getActivity(), ChannelActivity.class);
            i.putExtra("type", "view");
            i.putExtra("authorID", authorID);
            startActivity(i);
        }
    }

}