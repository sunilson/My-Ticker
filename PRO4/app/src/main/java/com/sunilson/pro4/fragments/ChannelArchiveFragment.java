package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.R;
import com.sunilson.pro4.baseClasses.User;

import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class ChannelArchiveFragment extends ChannelBaseFragment {

    public static ChannelArchiveFragment newInstance(String authorID) {
        Bundle args = new Bundle();
        args.putString("authorID", authorID);
        ChannelArchiveFragment channelArchiveFragment = new ChannelArchiveFragment();
        channelArchiveFragment.setArguments(args);
        return channelArchiveFragment;
    }

    @Override
    public void authChanged(FirebaseUser user) {

    }

    @Override
    public void userChanged(User user) {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel_archive, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }
}
