package com.sunilson.pro4.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.sunilson.pro4.R;
import com.sunilson.pro4.baseClasses.Liveticker;
import com.sunilson.pro4.fragments.LivetickerFragment;
import com.sunilson.pro4.interfaces.CanChangeFragment;
import com.sunilson.pro4.utilities.Constants;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;

public class LivetickerActivity extends BaseActivity implements CanChangeFragment {

    private ValueEventListener livetickerListener;
    private DatabaseReference livetickerReference;
    private final Gson gson = new Gson();
    private Liveticker liveticker;
    private boolean owner;
    private boolean started;

    /*
    @BindView(R.id.fab)
    FloatingActionButton fab;
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveticker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            Intent i = getIntent();
            String livetickerString = i.getStringExtra("liveticker");
            this.liveticker = gson.fromJson(livetickerString, Liveticker.class);
            getSupportFragmentManager().beginTransaction().add(R.id.content_liveticker, LivetickerFragment.newInstance(), Constants.FRAGMENT_LIVETICKER_TAG).commit();
            updateViews();
        }

        initializeLivetickerListener();
        livetickerReference = mReference.child(Constants.LIVETICKER_PATH).child(liveticker.getLivetickerID());
    }

    @Override
    protected void authChanged(FirebaseUser user) {
        if (user != null) {
            if (!started) {
                addToRecentlyVisited(liveticker.getLivetickerID(), user.getUid());
                started = true;
            }
            if (!user.isAnonymous()) {
                if (user.getUid().equals(liveticker.getAuthorID())) {
                    owner = true;
                    updateViews();
                } else {
                    owner = false;
                    updateViews();
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        livetickerReference.addValueEventListener(livetickerListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (livetickerListener != null) {
            livetickerReference.removeEventListener(livetickerListener);
        }
    }

    @Override
    public void replaceFragment(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction().replace(R.id.content_liveticker, fragment, tag).commit();

        if (tag.equals(Constants.FRAGMENT_LIVETICKER_TAG)) {
            //fab.setVisibility(View.VISIBLE);
        } else {
            //fab.setVisibility(View.GONE);
        }
    }

    private void initializeLivetickerListener() {
        livetickerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i(Constants.LOGGING_TAG, dataSnapshot.getValue().toString());
                liveticker = dataSnapshot.getValue(Liveticker.class);
                updateViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void addToRecentlyVisited(String livetickerID, String userID) {
        DatabaseReference dRef = mReference.child("recentlyVisited").push();
        Map<String, String> map = new HashMap<>();
        map.put("livetickerID", livetickerID);
        map.put("userID", userID);
        dRef.setValue(map);
    }

    private void updateViews() {
        setTitle(liveticker.getTitle());
    }
}
