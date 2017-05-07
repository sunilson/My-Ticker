package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sunilson.pro4.R;
import com.sunilson.pro4.adapters.FeedRecyclerViewAdapter;
import com.sunilson.pro4.baseClasses.Liveticker;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class SearchFragment extends BaseFragment {

    private ValueEventListener searchResultListener;
    private FeedRecyclerViewAdapter adapter;
    private boolean started;

    @BindView(R.id.fragment_search_recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.fragment_search_loading_container)
    LinearLayout loadingContainer;

    @BindView(R.id.fragment_search_placeholder)
    TextView placeholder;

    public void startSearch(String searchValue) {
        loading(true);
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("request/search/tasks").push();
        dRef.child("searchValue").setValue(searchValue).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loading(false);
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        dRef.addValueEventListener(searchResultListener);
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeSearchListener();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        unbinder = ButterKnife.bind(this, view);
        if (!started) {
            started = true;
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(adapter = new FeedRecyclerViewAdapter(recyclerView, getContext()));
            recyclerView.setNestedScrollingEnabled(false);

        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void initializeSearchListener() {
        searchResultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    if (dataSnapshot.child("error_state").getValue() != null) {
                        Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                    } else if (dataSnapshot.child("_state").getValue() != null
                            && dataSnapshot.child("_state").getValue().equals("success")) {

                        if (dataSnapshot.child("searchResults").getChildrenCount() == 0) {
                            placeholder.setText(getString(R.string.no_search_results));
                            placeholder.setVisibility(View.VISIBLE);
                        } else {
                            placeholder.setVisibility(View.GONE);
                            ArrayList<Liveticker> livetickerData = new ArrayList<>();
                            for (DataSnapshot childSnapshot : dataSnapshot.child("searchResults").getChildren()) {
                                Liveticker liveticker = childSnapshot.getValue(Liveticker.class);
                                livetickerData.add(liveticker);
                            }
                            loading(false);
                            adapter.setData(livetickerData);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void loading(boolean value) {
        if (value) {
            loadingContainer.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            placeholder.setVisibility(View.GONE);
        } else {
            loadingContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}