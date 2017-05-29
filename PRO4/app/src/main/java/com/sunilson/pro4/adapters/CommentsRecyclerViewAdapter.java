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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * @author Linus Weiss
 */

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter {
    private List<Comment> data = new ArrayList<>();
    private Context ctx;
    private RecyclerView recyclerView;
    private DateFormat dateFormat, timeFormat;

    public CommentsRecyclerViewAdapter(RecyclerView recyclerView, Context context) {
        this.recyclerView = recyclerView;
        this.ctx = context;
        this.dateFormat = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        TextView content, time, date, username;
        ImageView profilePicture;

        public ViewHolder(View itemView) {
            super(itemView);
            content = (TextView) itemView.findViewById(R.id.comment_content);
            username = (TextView) itemView.findViewById(R.id.comment_username);
            time = (TextView) itemView.findViewById(R.id.comment_time);
            date = (TextView) itemView.findViewById(R.id.comment_date);
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

        vh.date.setText(dateFormat.format(comment.getTimestamp()));
        vh.time.setText(timeFormat.format(comment.getTimestamp()));

        if (comment.getUserName() != null) {
            vh.username.setText(comment.getUserName());
        }

        if (comment.getProfilePicture() != null) {
            DrawableRequestBuilder<Integer> placeholder = Glide.with(ctx).load(R.drawable.profile_placeholder).bitmapTransform(new CropCircleTransformation(ctx));
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
