package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.sunilson.pro4.R;
import com.sunilson.pro4.baseClasses.Liveticker;
import com.sunilson.pro4.utilities.Constants;

import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class LivetickerFragment extends BaseFragment {

    private DatabaseReference livetickerReference, livetickerContentReference;
    private Liveticker liveticker;
    private ValueEventListener livetickerListener;
    private ChildEventListener livetickerContentListener;
    private final Gson gson = new Gson();

    public static LivetickerFragment newInstance(String livetickerString) {
        LivetickerFragment livetickerFragment = new LivetickerFragment();

        Bundle args = new Bundle();
        args.putString("liveticker", livetickerString);
        livetickerFragment.setArguments(args);

        return livetickerFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.liveticker = gson.fromJson(getArguments().getString("liveticker"), Liveticker.class);
    }

    @Override
    public void onStart() {
        super.onStart();

        livetickerReference = mReference.child(Constants.LIVETICKER_PATH).child(liveticker.getPrivacy()).child(liveticker.getLivetickerID());
        livetickerContentReference = mReference.child(Constants.LIVETICKER_CONTENT_PATH).child(liveticker.getLivetickerID());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_liveticker, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    private void initializeContentListener() {
        livetickerContentListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void initializeLivetickerListener() {
        livetickerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }
}
