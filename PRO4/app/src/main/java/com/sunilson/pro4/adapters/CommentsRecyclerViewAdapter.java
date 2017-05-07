package com.sunilson.pro4.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sunilson.pro4.R;
import com.sunilson.pro4.baseClasses.Comment;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

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
            content = (TextView) itemView.findViewById(R.id.comment_content);
            username = (TextView) itemView.findViewById(R.id.comment_username);
            timestamp = (TextView) itemView.findViewById(R.id.comment_timestamp);
            profilePicture = (ImageView) itemView.findViewById(R.id.comment_profile_picture);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_recyclerview_element, parent, false);
        return new CommentsRecyclerViewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CommentsRecyclerViewAdapter.ViewHolder vh = (CommentsRecyclerViewAdapter.ViewHolder) holder;
        Comment comment = data.get(position);

        vh.content.setText(comment.getContent());
        vh.timestamp.setText(comment.getTimestamp().toString());

        if (comment.getUserName() != null) {
            vh.username.setText(comment.getUserName());
        }

        if (comment.getProfilePicture() != null) {
            DrawableRequestBuilder<Integer> placeholder = Glide.with(ctx).load(R.drawable.default_placeholder).bitmapTransform(new CropCircleTransformation(ctx));
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(comment.getProfilePicture());
            Glide.with(ctx).using(new FirebaseImageLoader()).load(storageReference).thumbnail(placeholder).bitmapTransform(new CropCircleTransformation(ctx)).crossFade().into(vh.profilePicture);
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(Comment comment) {
        this.data.add(comment);
        notifyItemInserted(data.indexOf(comment));
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
