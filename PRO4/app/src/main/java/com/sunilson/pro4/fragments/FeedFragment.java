package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sunilson.pro4.R;
import com.sunilson.pro4.adapters.FeedRecyclerViewAdapter;
import com.sunilson.pro4.baseClasses.Liveticker;
import com.sunilson.pro4.exceptions.LivetickerSetException;
import com.sunilson.pro4.utilities.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class FeedFragment extends BaseFragment {

    private FeedRecyclerViewAdapter ownLivetickersAdapter, recentlyVisitedAdapter, subscriptionLivetickersAdapter;
    private ValueEventListener resultListener;
    private DatabaseReference currentResultReference;
    private FirebaseAuth.AuthStateListener authStateListener;

    @BindView(R.id.feed_fragment_progress)
    ProgressBar progressBar;

    @BindView(R.id.feed_fragment_ownLivetickers_recyclerView)
    RecyclerView ownLivetickers;

    @BindView(R.id.feed_fragment_recentlyVisited_recyclerView)
    RecyclerView recentlyVisited;

    @BindView(R.id.feed_fragment_subscriptionLivetickers_recyclerView)
    RecyclerView subscriptionLivetickers;

    @BindView(R.id.feed_fragment_content_container)
    LinearLayout content_container;

    @BindView(R.id.feed_fragment_loading_container)
    LinearLayout loading_container;


    public static FeedFragment newInstance() {
        return new FeedFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeResultListener();
        initializeAuthListener();
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);

        //Set Up Liveticker RecyclerViews
        ownLivetickers.setLayoutManager(new LinearLayoutManager(getActivity()));
        recentlyVisited.setLayoutManager(new LinearLayoutManager(getActivity()));
        subscriptionLivetickers.setLayoutManager(new LinearLayoutManager(getActivity()));

        ownLivetickers.setAdapter(ownLivetickersAdapter = new FeedRecyclerViewAdapter(ownLivetickers, getContext()));
        recentlyVisited.setAdapter(recentlyVisitedAdapter = new FeedRecyclerViewAdapter(recentlyVisited, getContext()));
        subscriptionLivetickers.setAdapter(subscriptionLivetickersAdapter = new FeedRecyclerViewAdapter(subscriptionLivetickers, getContext()));

        ownLivetickers.setNestedScrollingEnabled(false);
        recentlyVisited.setNestedScrollingEnabled(false);
        subscriptionLivetickers.setNestedScrollingEnabled(false);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (currentResultReference != null && resultListener != null) {
            currentResultReference.removeEventListener(resultListener);
        }

        if (authStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        }
    }

    private void requestFeed() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), R.string.feed_load_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        //Remove current result listener
        if (currentResultReference != null && resultListener != null) {
            currentResultReference.removeEventListener(resultListener);
        }

        //First clear all adapters
        clearFeeds();

        //Request Feed from Database
        loading(true);
        Map<String, String> data = new HashMap<>();
        data.put("userID", user.getUid());
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/request/" + user.getUid() + "/feed/").push();
        ref.setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Reassign result listeners
                currentResultReference = FirebaseDatabase.getInstance().getReference("/result/" + user.getUid() + "/feed/" + ref.getKey());
                currentResultReference.addValueEventListener(resultListener);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), R.string.feed_load_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFeeds() {
        if (ownLivetickersAdapter != null && recentlyVisitedAdapter != null && subscriptionLivetickersAdapter != null) {
            ownLivetickersAdapter.clear();
            recentlyVisitedAdapter.clear();
            subscriptionLivetickersAdapter.clear();
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    private void initializeResultListener() {
        resultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("state").getValue() != null) {
                    Log.i(Constants.LOGGING_TAG, dataSnapshot.getValue().toString());
                    if (dataSnapshot.child("state").getValue().toString().equals("success")) {
                        ArrayList<Liveticker> ownLivetickersData = new ArrayList<>();
                        ArrayList<Liveticker> recentLivetickersData = new ArrayList<>();
                        ArrayList<Liveticker> subscriptionLivetickersData = new ArrayList<>();

                        //Get Own Livetickers
                        ownLivetickersData = retrieveLivetickers(dataSnapshot, Constants.LIVETICKER_RESULT_OWN);

                        //Get Recent Livetickers
                        recentLivetickersData = retrieveLivetickers(dataSnapshot, Constants.LIVETICKER_RESULT_RECENT);

                        //Get Subscription Livetickers
                        subscriptionLivetickersData = retrieveLivetickers(dataSnapshot, Constants.LIVETICKER_RESULT_SUBSCRIPTIONS);

                        recentlyVisitedAdapter.setData(recentLivetickersData);
                        ownLivetickersAdapter.setData(ownLivetickersData);
                        subscriptionLivetickersAdapter.setData(subscriptionLivetickersData);
                        loading(false);
                    } else if (dataSnapshot.child("state").getValue().toString().equals("error")) {
                        if (dataSnapshot.child("errorDetails").getValue() != null) {
                            Toast.makeText(getActivity(), dataSnapshot.child("errorDetails").getValue().toString(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), R.string.feed_load_failure, Toast.LENGTH_SHORT).show();
                        }
                        loading(false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private ArrayList<Liveticker> retrieveLivetickers(DataSnapshot snapshot, String child) {
        ArrayList<Liveticker> result = new ArrayList<>();

        for (DataSnapshot postSnapshot : snapshot.child(child).getChildren()) {
            Liveticker tempLiveticker = postSnapshot.getValue(Liveticker.class);
            if (tempLiveticker.getLivetickerID() == null) {
                try {
                    tempLiveticker.setLivetickerID(postSnapshot.getKey());
                } catch (LivetickerSetException e) {
                    e.printStackTrace();
                }
            }
            result.add(tempLiveticker);
        }

        return result;
    }

    private void loading(boolean loading) {
        if (loading_container != null) {
            if (loading) {
                loading_container.setVisibility(View.VISIBLE);
                content_container.setVisibility(View.GONE);
            } else {
                loading_container.setVisibility(View.GONE);
                content_container.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.feed_menu_refresh:
                requestFeed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void initializeAuthListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    requestFeed();
                }
            }
        };
    }
}