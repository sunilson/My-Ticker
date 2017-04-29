package com.sunilson.pro4.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.sunilson.pro4.activities.LivetickerActivity;
import com.sunilson.pro4.baseClasses.Liveticker;
import com.sunilson.pro4.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * @author Linus Weiss
 */

public class FeedRecyclerViewAdapter extends RecyclerView.Adapter {

    private List<Liveticker> data = new ArrayList<>();
    private Context ctx;
    private final View.OnClickListener mOnclickListener = new FeedClickListener();
    private RecyclerView recyclerView;

    public FeedRecyclerViewAdapter(RecyclerView recyclerView, Context context) {
        this.recyclerView = recyclerView;
        this.ctx = context;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, author, state, status;
        ImageView profilePicture;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.feed_recyclerview_element_title);
            author = (TextView) itemView.findViewById(R.id.feed_recyclerview_element_author);
            state = (TextView) itemView.findViewById(R.id.feed_recyclerview_element_state);
            status = (TextView) itemView.findViewById(R.id.feed_recyclerview_element_status);
            profilePicture = (ImageView) itemView.findViewById(R.id.feed_recyclerview_element_image);
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
        vh.title.setText(data.get(position).getTitle());
        vh.author.setText(data.get(position).getUserName());
        vh.state.setText(data.get(position).getState());
        vh.status.setText(data.get(position).getStatus());

        if (data.get(position).getProfilePicture() != null) {
            Log.i(Constants.LOGGING_TAG, "Profile picture");
            DrawableRequestBuilder<Integer> placeholder = Glide.with(ctx).load(R.drawable.default_placeholder).bitmapTransform(new CropCircleTransformation(ctx));
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(data.get(position).getProfilePicture());
            Glide.with(ctx).using(new FirebaseImageLoader()).load(storageReference).thumbnail(placeholder).bitmapTransform(new CropCircleTransformation(ctx)).crossFade().into(vh.profilePicture);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(ArrayList<Liveticker> list) {
        this.data = list;
        notifyDataSetChanged();
    }

    private class FeedClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int pos = recyclerView.getChildLayoutPosition(view);
            Liveticker element = data.get(pos);
            Intent i = new Intent(ctx, LivetickerActivity.class);
            i.putExtra("livetickerID", element.getLivetickerID());
            ctx.startActivity(i);
        }
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }
}