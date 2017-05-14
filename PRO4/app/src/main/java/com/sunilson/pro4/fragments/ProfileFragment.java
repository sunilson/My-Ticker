package com.sunilson.pro4.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.R;
import com.sunilson.pro4.activities.AuthenticationActivity;
import com.sunilson.pro4.activities.ChannelActivity;
import com.sunilson.pro4.interfaces.FragmentAuthInterface;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Linus Weiss
 */

public class ProfileFragment extends BaseFragment implements FragmentAuthInterface {

    @BindView(R.id.fragment_profile_view_channel)
    Button profileButton;

    @BindView(R.id.fragment_profile_anonymous)
    LinearLayout profileAnonymous;

    @OnClick(R.id.fragment_profile_anonymous_button)
    public void register() {
        Intent i = new Intent(getActivity(), AuthenticationActivity.class);
        startActivity(i);
    }

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
        checkUser();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void authChanged(FirebaseUser user) {
        checkUser();
    }

    private void checkUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.isAnonymous()) {
                profileAnonymous.setVisibility(View.VISIBLE);
            } else {
                profileAnonymous.setVisibility(View.GONE);
            }
        }
    }
}