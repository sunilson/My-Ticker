package com.sunilson.pro4.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sunilson.pro4.R;
import com.sunilson.pro4.activities.LivetickerActivity;
import com.sunilson.pro4.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linus_000 on 17.03.2017.
 */

public class FeedRecyclerViewAdapter extends RecyclerView.Adapter {

    private List<String> data = new ArrayList<>();
    private Context ctx;
    private final View.OnClickListener mOnclickListener = new FeedClickListener();
    private final Gson gson = new Gson();
    private RecyclerView recyclerView;

    public FeedRecyclerViewAdapter(RecyclerView recyclerView, Context context) {
        this.recyclerView = recyclerView;
        this.ctx = context;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        TextView string;

        public ViewHolder(View itemView) {
            super(itemView);
            string = (TextView) itemView.findViewById(R.id.feed_recyclerview_element_card_text);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_recyclerview_element, parent, false);
        v.setOnClickListener(mOnclickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder vh = (ViewHolder) holder;
        vh.string.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(ArrayList<String> list) {
        this.data = list;
        notifyDataSetChanged();
    }

    public void add(String string) {
        data.add(string);
        notifyDataSetChanged();
    }

    private class FeedClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int pos = recyclerView.getChildLayoutPosition(view);
            String element = data.get(pos);
            Log.i(Constants.LOGGING_TAG, element);
            Intent i = new Intent(ctx, LivetickerActivity.class);
            i.putExtra("liveticker", gson.toJson(element));
            ctx.startActivity(i);
        }
    }
}