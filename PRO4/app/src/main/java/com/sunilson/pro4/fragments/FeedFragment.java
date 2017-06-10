package com.sunilson.pro4.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sunilson.pro4.R;
import com.sunilson.pro4.activities.AuthenticationActivity;
import com.sunilson.pro4.adapters.FeedRecyclerViewAdapter;
import com.sunilson.pro4.baseClasses.Liveticker;
import com.sunilson.pro4.utilities.Constants;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.sunilson.pro4.utilities.Constants.FEED_PATH;

/**
 * @author Linus Weiss
 */

public class FeedFragment extends FeedBaseFragment{

    private FeedRecyclerViewAdapter recentlyVisitedAdapter, subscriptionLivetickersAdapter;

    @BindView(R.id.feed_fragment_recentlyVisited_layout)
    LinearLayout recentlyVisitedLayout;

    @BindView(R.id.feed_fragment_subscriptionLivetickers_layout)
    LinearLayout subscriptionLayout;

    @BindView(R.id.feed_fragment_recentlyVisited_recyclerView)
    RecyclerView recentlyVisited;

    @BindView(R.id.feed_fragment_subscriptionLivetickers_recyclerView)
    RecyclerView subscriptionLivetickers;

    @BindView(R.id.feed_fragment_content_container)
    LinearLayout content_container;

    @BindView(R.id.fragment_feed_anonymous)
    LinearLayout anon_container;

    @BindView(R.id.fragment_feed_swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.feed_fragment_recentlyVisted_placeholder)
    TextView recentPlaceholder;

    @BindView(R.id.feed_fragment_subscriptionLIvetickers_placeholder)
    TextView subPlaceholder;

    @OnClick(R.id.fragment_feed_anonymous_button)
    public void register() {
        Intent i = new Intent(getActivity(), AuthenticationActivity.class);
        startActivity(i);
    }

    public static FeedFragment newInstance() {
        return new FeedFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        path = FEED_PATH;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (currentResultReference != null && resultListener != null && recentlyVisitedAdapter.getItemCount() == 0 && subscriptionLivetickersAdapter.getItemCount() == 0) {
            loading(true);
            currentResultReference.addValueEventListener(resultListener);
        }
    }

    private void clearFeeds() {
        if (recentlyVisitedAdapter != null && subscriptionLivetickersAdapter != null) {
            recentlyVisitedAdapter.clear();
            subscriptionLivetickersAdapter.clear();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        unbinder = ButterKnife.bind(this, view);

        recentlyVisited.setLayoutManager(new LinearLayoutManager(getActivity()));
        subscriptionLivetickers.setLayoutManager(new LinearLayoutManager(getActivity()));

        recentlyVisited.setAdapter(recentlyVisitedAdapter = new FeedRecyclerViewAdapter(recentlyVisited, getContext()));
        subscriptionLivetickers.setAdapter(subscriptionLivetickersAdapter = new FeedRecyclerViewAdapter(subscriptionLivetickers, getContext()));

        recentlyVisited.setNestedScrollingEnabled(false);
        subscriptionLivetickers.setNestedScrollingEnabled(false);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorAccent, R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestFeed();
            }
        });

        return view;
    }

    void initializeResultListener() {
        resultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("state").getValue() != null) {

                    //First clear all adapters
                    clearFeeds();

                    if (dataSnapshot.child("state").getValue().toString().equals("success")) {

                        ArrayList<Liveticker> recentLivetickersData;
                        ArrayList<Liveticker> subscriptionLivetickersData;

                        recentLivetickersData = retrieveLivetickers(dataSnapshot, Constants.LIVETICKER_RESULT_RECENT);
                        subscriptionLivetickersData = retrieveLivetickers(dataSnapshot, Constants.LIVETICKER_RESULT_SUBSCRIPTIONS);

                        recentlyVisitedAdapter.setData(recentLivetickersData);
                        subscriptionLivetickersAdapter.setData(subscriptionLivetickersData);

                        recentlyVisitedAdapter.sortByDate();
                        subscriptionLivetickersAdapter.sortByDate();

                        updateRecyclerviews();
                        loading(false);
                    } else if (dataSnapshot.child("state").getValue().toString().equals("error")) {
                        Toast.makeText(getActivity(), R.string.feed_load_failure, Toast.LENGTH_SHORT).show();
                        loading(false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void updateRecyclerviews() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            if (user.isAnonymous()) {
                recentlyVisitedLayout.setVisibility(View.GONE);
                subscriptionLayout.setVisibility(View.GONE);
                recentlyVisitedLayout.setVisibility(View.VISIBLE);
                subscriptionLayout.setVisibility(View.VISIBLE);
            }
        }

        if (subscriptionLivetickersAdapter.getItemCount() == 0) {
            subPlaceholder.setVisibility(View.VISIBLE);
        } else {
            subPlaceholder.setVisibility(View.GONE);
        }

        if (recentlyVisitedAdapter.getItemCount() == 0) {
            recentPlaceholder.setVisibility(View.VISIBLE);
        } else {
            recentPlaceholder.setVisibility(View.GONE);
        }

    }

    void loading(boolean loading) {
        if (loading) {
            swipeRefreshLayout.setRefreshing(true);
            content_container.setVisibility(View.GONE);
        } else {
            content_container.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /*
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String string = adapterView.getItemAtPosition(i).toString();

       if (string.equals(getString(R.string.all))) {
            recentlyVisitedLayout.setVisibility(View.VISIBLE);
            subscriptionLayout.setVisibility(View.VISIBLE);
        } else if (string.equals(getString(R.string.recently_visited_livetickers_spinner))) {
            recentlyVisitedLayout.setVisibility(View.VISIBLE);
            subscriptionLayout.setVisibility(View.GONE);
        } else if (string.equals(getString(R.string.subscription_livetickers_spinner))) {
            recentlyVisitedLayout.setVisibility(View.GONE);
            subscriptionLayout.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    */

    void initializeAuthListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    loading(false);
                    if (!user.isAnonymous()) {
                        isAnonymous(false);
                        if (!started) {
                            loading(false);
                            requestFeed();
                            started = true;
                        } else {
                            if (currentUser != null && !currentUser.equals(user.getUid())) {
                                requestFeed();
                            }
                        }
                    } else {
                        isAnonymous(true);
                    }
                    currentUser = user.getUid();
                }
            }
        };
    }

    private void isAnonymous(boolean value) {
        if (value) {
            anon_container.setVisibility(View.VISIBLE);
            content_container.setVisibility(View.GONE);
            swipeRefreshLayout.setEnabled(false);
        } else {
            anon_container.setVisibility(View.GONE);
            content_container.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setEnabled(true);
        }
    }
}