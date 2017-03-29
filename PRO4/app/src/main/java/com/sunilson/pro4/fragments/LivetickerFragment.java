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
import com.sunilson.pro4.R;
import com.sunilson.pro4.baseClasses.Liveticker;

import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class LivetickerFragment extends BaseFragment {

    private DatabaseReference  livetickerContentReference;
    private Liveticker liveticker;
    private ChildEventListener livetickerContentListener;

    public static LivetickerFragment newInstance() {
        LivetickerFragment livetickerFragment = new LivetickerFragment();
        return livetickerFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //livetickerContentReference = ((BaseActivity)getActivity()).getReference().child(Constants.LIVETICKER_CONTENT_PATH).child(liveticker.getLivetickerID());
        //initializeContentListener();
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
}
