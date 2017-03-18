package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sunilson.pro4.R;
import com.sunilson.pro4.adapters.FeedRecyclerViewAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by linus_000 on 15.03.2017.
 */

public class FeedFragment extends BaseFragment {

    private Unbinder unbinder;
    private RecyclerView.LayoutManager layoutManager;
    private FeedRecyclerViewAdapter recyclerViewAdapter;

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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}