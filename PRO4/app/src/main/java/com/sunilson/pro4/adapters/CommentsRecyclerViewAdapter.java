package com.sunilson.pro4.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunilson.pro4.R;
import com.sunilson.pro4.baseClasses.Comment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linus_000 on 03.05.2017.
 */

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter {
    private List<Comment> data = new ArrayList<>();
    private Context ctx;
    private RecyclerView recyclerView;

    public CommentsRecyclerViewAdapter(RecyclerView recyclerView, Context context) {
        this.recyclerView = recyclerView;
        this.ctx = context;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        TextView content, timestamp, username;
        ImageView profilePicture;

        public ViewHolder(View itemView) {
            super(itemView);
            content = (TextView) itemView.findViewById(R.id.feed_recyclerview_element_title);
            username = (TextView) itemView.findViewById(R.id.feed_recyclerview_element_author);
            timestamp = (TextView) itemView.findViewById(R.id.feed_recyclerview_element_state);
            profilePicture = (ImageView) itemView.findViewById(R.id.feed_recyclerview_element_image);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_recyclerview_element, parent, false);
        return new CommentsRecyclerViewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CommentsRecyclerViewAdapter.ViewHolder vh = (CommentsRecyclerViewAdapter.ViewHolder) holder;
        Comment comment = data.get(position);

        vh.content.setText(comment.getText());
        vh.timestamp.setText(comment.getTimestamp().toString());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(ArrayList<Comment> list) {
        this.data = list;
        notifyDataSetChanged();
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }
}
