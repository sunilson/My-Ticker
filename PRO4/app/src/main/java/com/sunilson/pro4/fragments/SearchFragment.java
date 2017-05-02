package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sunilson.pro4.R;
import com.sunilson.pro4.adapters.FeedRecyclerViewAdapter;
import com.sunilson.pro4.baseClasses.Liveticker;
import com.sunilson.pro4.exceptions.LivetickerSetException;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Linus Weiss
 */

public class SearchFragment extends BaseFragment {

    private ValueEventListener searchResultListener;
    private FeedRecyclerViewAdapter adapter;

    @BindView(R.id.fragment_search_input)
    EditText searchInput;

    @BindView(R.id.fragment_search_recycler_view)
    RecyclerView recyclerView;

    @OnClick(R.id.fragment_search_submit)
    public void startSearch() {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("request/search/tasks").push();
        dRef.child("searchValue").setValue(searchInput.getText().toString());
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

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter = new FeedRecyclerViewAdapter(recyclerView, getContext()));
        recyclerView.setNestedScrollingEnabled(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        unbinder = ButterKnife.bind(this, view);
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
                    } else if (dataSnapshot.child("_state").getValue() != null && dataSnapshot.child("_state").getValue().equals("success")) {
                        ArrayList<Liveticker> livetickerData = new ArrayList<>();
                        for (DataSnapshot childSnapshot : dataSnapshot.child("searchResults").getChildren()) {
                            Liveticker liveticker = childSnapshot.getValue(Liveticker.class);
                            try {
                                liveticker.setLivetickerID(childSnapshot.getKey());
                                livetickerData.add(liveticker);
                            } catch (LivetickerSetException e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.setData(livetickerData);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

}