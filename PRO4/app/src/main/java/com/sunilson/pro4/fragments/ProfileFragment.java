package com.sunilson.pro4.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.sunilson.pro4.R;
import com.sunilson.pro4.activities.ChannelActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Linus Weiss
 */

public class ProfileFragment extends BaseFragment {

    @OnClick(R.id.fragment_profile_edit_channel)
    public void editProfile() {
        Intent i = new Intent(getActivity(), ChannelActivity.class);
        i.putExtra("type", "editChannel");
        startActivity(i);
    }

    @OnClick(R.id.fragment_profile_view_channel)
    public void viewProfile() {
        Intent i = new Intent(getActivity(), ChannelActivity.class);
        i.putExtra("type", "view");
        i.putExtra("authorID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        startActivity(i);
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}