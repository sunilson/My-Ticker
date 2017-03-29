package com.sunilson.pro4.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.sunilson.pro4.baseClasses.LivetickerEvent;

import java.util.ArrayList;

/**
 * @author Linus Weiss
 */

public class LivetickerRecyclerViewAdapter extends RecyclerView.Adapter {

    private ArrayList<LivetickerEvent> data = new ArrayList<>();

    public LivetickerRecyclerViewAdapter(Context ctx, RecyclerView recyclerView) {

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
