package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sunilson.pro4.R;
import com.sunilson.pro4.baseClasses.User;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class ChannelInfoFragment extends ChannelBaseFragment {

    @BindView(R.id.fragment_channel_info_description)
    TextView channelDescription;

    @BindView(R.id.fragment_channel_info_subscription_count)
    TextView subscriptionCount;

    private ValueEventListener channelListener;
    private User user;
    private DatabaseReference channelReference;

    public static ChannelInfoFragment newInstance(String authorID) {
        ChannelInfoFragment channelFragment = new ChannelInfoFragment();
        Bundle args = new Bundle();
        args.putString("authorID", authorID);
        channelFragment.setArguments(args);
        return channelFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        channelReference = FirebaseDatabase.getInstance().getReference("users/" + getArguments().getString("authorID"));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel_info, container, false);
        unbinder = ButterKnife.bind(this, view);

        initializeChannelListener();
        channelReference.addListenerForSingleValueEvent(channelListener);
        return view;
    }

    private void updateViews() {

        if (user.getInfo() != null) {
            channelDescription.setText(user.getInfo());
        }

        subscriptionCount.setText(String.valueOf(user.getSubscriberCount()));
    }

    @Override
    public void authChanged(FirebaseUser user) {

    }

    @Override
    public void userChanged(User user) {

    }

    private void initializeChannelListener() {
        channelListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    user = dataSnapshot.getValue(User.class);
                    updateViews();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }
}
