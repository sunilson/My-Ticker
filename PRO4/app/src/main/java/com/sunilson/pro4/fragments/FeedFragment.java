package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.sunilson.pro4.R;
import com.sunilson.pro4.adapters.FeedRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by linus_000 on 15.03.2017.
 */

public class FeedFragment extends BaseFragment {

    private RecyclerView.LayoutManager layoutManager;
    private FeedRecyclerViewAdapter recyclerViewAdapter;
    private ValueEventListener queueListener;

    @BindView(R.id.feedRecyclerView)
    RecyclerView recyclerView;

    public static FeedFragment newInstance() {
        return new FeedFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        ArrayList<String> list = new ArrayList<>();
        list.add("yo");
        list.add("yo");
        list.add("yo");
        list.add("yo");
        list.add("yo");
        list.add("yo");
        list.add("yo");
        list.add("yo");

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerViewAdapter = new FeedRecyclerViewAdapter(list);
        recyclerView.setAdapter(recyclerViewAdapter);

        initializeQueueListener();

        Map<String, String> data = new HashMap<>();

        data.put("userID", FirebaseAuth.getInstance().getCurrentUser().getUid());

        final DatabaseReference ref = mReference.child("queue").child("requestLivetickerQueue").child("tasks").push();
        ref.setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                DatabaseReference taskRef = mReference.child("queue").child("requestLivetickerQueue").child("tasks").child(ref.getKey());
                taskRef.addValueEventListener(queueListener);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    private void initializeQueueListener() {
        queueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("_state").getValue() != null) {
                    if (dataSnapshot.child("_state").getValue().toString().equals("success")) {
                        Toast.makeText(getActivity(), "success", Toast.LENGTH_SHORT).show();
                    } else if (dataSnapshot.child("_state").getValue().toString().equals("error")) {
                        Toast.makeText(getActivity(), dataSnapshot.child("_error_details").child("error").getValue().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }
}