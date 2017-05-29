package com.sunilson.pro4.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sunilson.pro4.R;
import com.sunilson.pro4.adapters.FeedRecyclerViewAdapter;
import com.sunilson.pro4.baseClasses.Liveticker;
import com.sunilson.pro4.utilities.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class LivetickerListFragment extends BaseFragment implements AdapterView.OnItemSelectedListener {

    @BindView(R.id.fragment_liveticker_list_recycler_view)
    RecyclerView recyclerView;

    private String authorID, sorting;
    private FeedRecyclerViewAdapter adapter;
    private DatabaseReference livetickerReference;
    private ChildEventListener livetickerListener;
    private SharedPreferences sharedPreferences;
    private Spinner spinner;
    private ArrayAdapter<CharSequence> arrayAdapter;

    public static LivetickerListFragment newInstance(String authorID) {
        Bundle bundle = new Bundle();
        bundle.putString("authorID", authorID);
        LivetickerListFragment livetickerListFragment = new LivetickerListFragment();
        livetickerListFragment.setArguments(bundle);
        return livetickerListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.authorID = getArguments().getString("authorID");
        sharedPreferences = getContext().getSharedPreferences(Constants.SHARED_PREF_TAG, Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_liveticker_list, container, false);
        unbinder = ButterKnife.bind(this, view);

        spinner = (Spinner) getActivity().findViewById(R.id.channel_bar_spinner);
        arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.feed_spinner_values, R.layout.spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(this);
        sorting = sharedPreferences.getString(Constants.SHARED_PREF_KEY_LIVETICKER_SORTING, Constants.LIVETICKER_SORTING_ALL);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter = new FeedRecyclerViewAdapter(recyclerView, getContext()));
        livetickerReference = FirebaseDatabase.getInstance().getReference("liveticker/");
        initializeLivetickerListener();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        livetickerReference.orderByChild("idState").startAt(authorID + "a").endAt(authorID + "c").addChildEventListener(livetickerListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (livetickerListener != null && livetickerReference != null) {
            livetickerReference.removeEventListener(livetickerListener);
        }
    }

    private void initializeLivetickerListener() {
        livetickerListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Liveticker liveticker = dataSnapshot.getValue(Liveticker.class);

                if (liveticker != null) {
                    adapter.add(liveticker);
                    adapter.sortByDate();
                }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (livetickerListener != null && livetickerReference != null) {
            livetickerReference.removeEventListener(livetickerListener);
        }

        adapter.clear();

        if (arrayAdapter.getItem(i).equals(getString(R.string.all))) {
            livetickerReference.orderByChild("idState").startAt(authorID + "a").endAt(authorID + "c").addChildEventListener(livetickerListener);
        } else if (arrayAdapter.getItem(i).equals(getString(R.string.state_not_started))) {
            livetickerReference.orderByChild("idState").equalTo(authorID + "a").addChildEventListener(livetickerListener);
        } else if (arrayAdapter.getItem(i).equals(getString(R.string.state_started))) {
            livetickerReference.orderByChild("idState").equalTo(authorID + "ab").addChildEventListener(livetickerListener);
        } else if (arrayAdapter.getItem(i).equals(getString(R.string.state_finished))) {
            livetickerReference.orderByChild("idState").equalTo(authorID + "c").addChildEventListener(livetickerListener);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
