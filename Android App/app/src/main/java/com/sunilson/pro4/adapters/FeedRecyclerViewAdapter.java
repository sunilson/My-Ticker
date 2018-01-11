package com.sunilson.pro4.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunilson.pro4.R;
import com.sunilson.pro4.activities.ChannelActivity;
import com.sunilson.pro4.activities.LivetickerActivity;
import com.sunilson.pro4.baseClasses.Liveticker;
import com.sunilson.pro4.utilities.Constants;
import com.sunilson.pro4.utilities.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        ImageView profilePicture, stateImage, likeIcon;

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
            likeIcon = (ImageView) itemView.findViewById(R.id.feed_recyclerview_element_like_icon);
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

        if (!liveticker.getStatus().isEmpty()) {
            vh.status.setText(liveticker.getStatus());
        } else {
            vh.status.setText(ctx.getString(R.string.no_status));
        }

        if (liveticker.getCommentCount() > 999) {
            vh.commentCount.setText("999+");
        } else {
            vh.commentCount.setText(Integer.toString(liveticker.getCommentCount()));
        }

        if (liveticker.getLikeCount() > 9999) {
            vh.likeCount.setText("9999+");
            vh.likeIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.like_icon_liked));
        } else {
            vh.likeCount.setText(Integer.toString(liveticker.getLikeCount()));
            vh.likeIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.like_icon));
        }

        if (liveticker.getProfilePicture() != null) {
            Utilities.setupRoundImageViewWithPlaceholder(vh.profilePicture, ctx, liveticker.getProfilePicture(), R.drawable.profile_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(ArrayList<Liveticker> list) {
        this.data = list;
        Collections.reverse(this.data);
        notifyDataSetChanged();
    }

    public void add(Liveticker liveticker) {
        this.data.add(liveticker);
        int index = this.data.indexOf(liveticker);
        notifyItemInserted(index);
    }

    public void sortByDate() {
        Collections.sort(data, new Comparator<Liveticker>() {
            @Override
            public int compare(Liveticker liveticker, Liveticker t1) {
                if (liveticker.getStateTimestamp() < t1.getStateTimestamp()) {
                    return 1;
                } else if (liveticker.getStateTimestamp() > t1.getStateTimestamp()) {
                    return -1;
                }
                return 0;
            }
        });
    }

    private class FeedClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int pos = recyclerView.getChildLayoutPosition(view);
            Liveticker element = data.get(pos);
            Intent i = new Intent(ctx, LivetickerActivity.class);
            i.putExtra("livetickerID", element.getLivetickerID());
            ((Activity) ctx).startActivityForResult(i, Constants.LIVETICKER_ACTIVITY_REQUEST);
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