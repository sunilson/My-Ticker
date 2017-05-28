package com.sunilson.pro4.fragments;

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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sunilson.pro4.R;
import com.sunilson.pro4.adapters.FeedRecyclerViewAdapter;
import com.sunilson.pro4.baseClasses.Liveticker;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sunilson.pro4.utilities.Constants.LIVETICKER_RESULT_ANON;
import static com.sunilson.pro4.utilities.Constants.LIVE_PATH;

/**
 * @author Linus Weiss
 */

public class LiveFragment extends FeedBaseFragment {

    private FeedRecyclerViewAdapter adapter;
    private Spinner spinner;

    @BindView(R.id.live_fragment_recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.live_fragment_content_container)
    LinearLayout contentContainer;

    @BindView(R.id.live_fragment_placeholder)
    TextView placeholder;

    @BindView(R.id.live_fragment_swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

    public static LiveFragment newInstance() {
        return new LiveFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        path = LIVE_PATH;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live, container, false);
        unbinder = ButterKnife.bind(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter = new FeedRecyclerViewAdapter(recyclerView, getContext()));
        recyclerView.setNestedScrollingEnabled(false);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorAccent, R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestFeed();
            }
        });

        spinner = (Spinner) getActivity().findViewById(R.id.feed_bar_spinner);

        return view;
    }


    @Override
    void initializeAuthListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (!started) {
                        requestFeed();
                        started = true;
                    } else {
                        if (currentUser != null && !currentUser.equals(user.getUid())) {
                            requestFeed();
                        }
                    }
                    currentUser = user.getUid();
                }
            }
        };
    }

    @Override
    void initializeResultListener() {
        resultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("state").getValue() != null) {
                    if (dataSnapshot.child("state").getValue().toString().equals("success")) {
                        ArrayList<Liveticker> livetickerData;
                        livetickerData = retrieveLivetickers(dataSnapshot, LIVETICKER_RESULT_ANON);
                        adapter.setData(livetickerData);
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

    @Override
    void loading(boolean loading) {
        if (loading) {
            swipeRefreshLayout.setRefreshing(true);
            contentContainer.setVisibility(View.GONE);
        } else {
            contentContainer.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
