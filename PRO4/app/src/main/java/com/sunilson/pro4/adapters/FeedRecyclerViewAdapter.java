package com.sunilson.pro4.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
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
import com.sunilson.pro4.activities.ChannelActivity;
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
    private final View.OnClickListener authorClickListener = new AuthorClickListener();
    private RecyclerView recyclerView;

    public FeedRecyclerViewAdapter(RecyclerView recyclerView, Context context) {
        this.recyclerView = recyclerView;
        this.ctx = context;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        TextView title, author, state, status, commentCount, likeCount;
        ImageView profilePicture, stateImage;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.feed_recyclerview_element_title);
            author = (TextView) itemView.findViewById(R.id.feed_recyclerview_element_author);
            state = (TextView) itemView.findViewById(R.id.feed_recyclerview_element_state);
            status = (TextView) itemView.findViewById(R.id.feed_recyclerview_element_status);
            commentCount = (TextView) itemView.findViewById(R.id.feed_recyclerview_element_comment_count);
            likeCount = (TextView) itemView.findViewById(R.id.feed_recyclerview_element_like_count);
            stateImage = (ImageView) itemView.findViewById(R.id.feed_recyclerview_element_state_image);
            profilePicture = (ImageView) itemView.findViewById(R.id.feed_recyclerview_element_image);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_recyclerview_element, parent, false);
        v.findViewById(R.id.feed_recyclerview_element_author_container).setOnClickListener(authorClickListener);
        v.setOnClickListener(mOnclickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder vh = (ViewHolder) holder;
        Liveticker liveticker = data.get(position);
        vh.title.setText(liveticker.getTitle());
        vh.author.setText(liveticker.getUserName());
        switch (liveticker.getState()) {
            case Constants.LIVETICKER_NOT_STARTED_STATE:
                vh.state.setText(ctx.getString(R.string.state_not_started));
                vh.stateImage.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.state_not_started));
                break;
            case Constants.LIVETICKER_STARTED_STATE:
                vh.state.setText(ctx.getString(R.string.state_started));
                vh.stateImage.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.state_started));
                break;
            default:
                vh.state.setText(ctx.getString(R.string.state_finished));
                vh.stateImage.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.state_finished));
                break;
        }

        vh.status.setText(liveticker.getStatus());
        if (liveticker.getCommentCount() > 9999) {
            vh.commentCount.setText("9999+");
        } else {
            vh.commentCount.setText(Integer.toString(liveticker.getCommentCount()));
        }

        if (liveticker.getLikeCount() > 9999) {
            vh.likeCount.setText("9999+");
        } else {
            vh.likeCount.setText(Integer.toString(liveticker.getLikeCount()));
        }

        if (liveticker.getProfilePicture() != null) {
            DrawableRequestBuilder<Integer> placeholder = Glide.with(ctx).load(R.drawable.profile_placeholder).bitmapTransform(new CropCircleTransformation(ctx));
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(liveticker.getProfilePicture());
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

    private class AuthorClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            View parent =  (View)(view.getParent()).getParent();
            int pos = recyclerView.getChildLayoutPosition(parent);
            Liveticker element = data.get(pos);
            if (element.getAuthorID() != null) {
                Intent i = new Intent(ctx, ChannelActivity.class);
                i.putExtra("type", "view");
                i.putExtra("authorID", element.getAuthorID());
                ctx.startActivity(i);
            }
        }
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }
}