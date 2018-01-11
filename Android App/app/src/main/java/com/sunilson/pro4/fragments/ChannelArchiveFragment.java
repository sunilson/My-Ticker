package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.sunilson.pro4.adapters.FeedRecyclerViewAdapter;
import com.sunilson.pro4.baseClasses.Liveticker;
import com.sunilson.pro4.baseClasses.User;
import com.sunilson.pro4.exceptions.LivetickerSetException;
import com.sunilson.pro4.utilities.Constants;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class ChannelArchiveFragment extends ChannelBaseFragment {

    private ValueEventListener livetickerListener;
    private DatabaseReference livetickerRef;
    private FeedRecyclerViewAdapter adapter;
    private String authorID;

    @BindView(R.id.fragment_channel_archive_recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.fragment_channel_archive_placeholder_text)
    TextView placeholder;

    public static ChannelArchiveFragment newInstance(String authorID) {
        Bundle args = new Bundle();
        args.putString("authorID", authorID);
        ChannelArchiveFragment channelArchiveFragment = new ChannelArchiveFragment();
        channelArchiveFragment.setArguments(args);
        return channelArchiveFragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (authorID != null) {
            livetickerRef.orderByChild("idState").equalTo(authorID + "c").addListenerForSingleValueEvent(livetickerListener);
        }
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authorID = getArguments().getString("authorID");
        livetickerRef = FirebaseDatabase.getInstance().getReference("liveticker/");
        initializeLivetickerListener();
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

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter = new FeedRecyclerViewAdapter(recyclerView, getContext()));
        recyclerView.setNestedScrollingEnabled(false);

        return view;
    }

    private void initializeLivetickerListener() {
        livetickerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(Constants.LOGGING_TAG, dataSnapshot.toString());
                ArrayList<Liveticker> ownLivetickersData = new ArrayList<>();
                if (dataSnapshot.getChildrenCount() > 0) {
                    //checkLoading();
                    for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                        Liveticker tempLiveticker = childSnapshot.getValue(Liveticker.class);
                        try {
                            tempLiveticker.setLivetickerID(childSnapshot.getKey());
                        } catch (LivetickerSetException e) {
                            e.printStackTrace();
                        }
                        if (user != null) {
                            tempLiveticker.setProfilePicture(user.getProfilePicture());
                            tempLiveticker.setUserName(user.getUserName());
                        }

                        ownLivetickersData.add(tempLiveticker);
                    }
                }
                adapter.setData(ownLivetickersData);
                adapter.sortByDate();

                if (adapter.getItemCount() != 0) {
                    placeholder.setVisibility(View.GONE);
                }
                //channelViewPager.measureCurrentView(getView());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }
}
