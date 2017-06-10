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
import com.sunilson.pro4.baseClasses.User;
import com.sunilson.pro4.utilities.Constants;
import com.sunilson.pro4.utilities.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Linus Weiss
 */

public class SearchRecyclerViewAdapter extends RecyclerView.Adapter {

    private List<Object> data = new ArrayList<>();
    private Context ctx;
    private final View.OnClickListener mOnclickListener = new FeedClickListener();
    private final View.OnClickListener authorClickListener = new AuthorClickListener();
    private RecyclerView recyclerView;

    public SearchRecyclerViewAdapter(RecyclerView recyclerView, Context context) {
        this.recyclerView = recyclerView;
        this.ctx = context;
    }

    private class LivetickerViewHolder extends RecyclerView.ViewHolder {

        TextView title, author, state, status, commentCount, likeCount;
        ImageView profilePicture, stateImage, likeIcon;

        LivetickerViewHolder(View itemView) {
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

    private class ChannelViewHolder extends RecyclerView.ViewHolder {

        TextView username, status;
        ImageView profilePicture;

        ChannelViewHolder(View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.feed_recyclerview_user_element_username);
            status = (TextView) itemView.findViewById(R.id.feed_recyclerview_user_element_status);
            profilePicture = (ImageView) itemView.findViewById(R.id.feed_recyclerview_user_element_profile_picture);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == 0) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_recyclerview_element, parent, false);
            v.findViewById(R.id.feed_recyclerview_element_author_container).setOnClickListener(authorClickListener);
            v.setOnClickListener(mOnclickListener);
            return new LivetickerViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_recyclerview_user_element, parent, false);
            v.setOnClickListener(mOnclickListener);
            return new ChannelViewHolder(v);
        }
    }

    @Override
    public int getItemViewType(int position) {

        Object element = data.get(position);

        if (element instanceof Liveticker) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        Object object = data.get(position);

        if (object instanceof Liveticker) {
            LivetickerViewHolder vh = (LivetickerViewHolder) holder;
            Liveticker liveticker = (Liveticker) object;
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
            }

            if (liveticker.getCommentCount() > 9999) {
                vh.commentCount.setText("9999+");
            } else {
                vh.commentCount.setText(Integer.toString(liveticker.getCommentCount()));
            }

            if (liveticker.getLikeCount() > 9999) {
                vh.likeCount.setText("9999+");
                vh.likeIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.like_icon_liked));
            } else if (liveticker.getLikeCount() > 0) {
                vh.likeCount.setText(Integer.toString(liveticker.getLikeCount()));
                vh.likeIcon.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.like_icon_liked));
            } else {
                vh.likeCount.setText(Integer.toString(liveticker.getLikeCount()));
            }

            if (liveticker.getProfilePicture() != null) {
                Utilities.setupRoundImageViewWithPlaceholder(vh.profilePicture, ctx, liveticker.getProfilePicture(), R.drawable.profile_placeholder);
            }
        } else if (object instanceof User) {
            ChannelViewHolder vh = (ChannelViewHolder) holder;
            User user = (User) object;
            vh.username.setText(user.getUserName());

            if (user.getStatus().isEmpty()) {
                vh.status.setText(ctx.getString(R.string.no_status));
            } else {
                vh.status.setText(user.getStatus());
            }

            if (user.getProfilePicture() != null) {
                Utilities.setupRoundImageViewWithPlaceholder(vh.profilePicture, ctx, user.getProfilePicture(), R.drawable.profile_placeholder);
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(ArrayList<Object> list) {
        this.data = list;
        Collections.reverse(this.data);
        notifyDataSetChanged();
    }

    public void add(Object object) {
        this.data.add(object);
        int index = this.data.indexOf(object);
        notifyItemInserted(index);
    }

    public void sortByName() {
        Collections.sort(data, new Comparator<Object>() {
            @Override
            public int compare(Object o, Object o2) {
                String string1, string2;

                if (o instanceof Liveticker) {
                    string1 = ((Liveticker) o).getTitle();
                } else {
                    string1 = ((User) o).getUserName();
                }

                if (o2 instanceof Liveticker) {
                    string2 = ((Liveticker) o2).getTitle();
                } else {
                    string2 = ((User) o2).getUserName();
                }

                return string1.compareTo(string2);
            }
        });
    }

    private class FeedClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int pos = recyclerView.getChildLayoutPosition(view);
            Object object = data.get(pos);

            if (object instanceof Liveticker) {
                Liveticker element = (Liveticker) data.get(pos);
                Intent i = new Intent(ctx, LivetickerActivity.class);
                i.putExtra("livetickerID", element.getLivetickerID());
                ((Activity) ctx).startActivityForResult(i, Constants.LIVETICKER_ACTIVITY_REQUEST);
            } else if (object instanceof User) {
                User element = (User) data.get(pos);
                if (element.getUserID() != null) {
                    Intent i = new Intent(ctx, ChannelActivity.class);
                    i.putExtra("type", "view");
                    i.putExtra("authorID", element.getUserID());
                    ctx.startActivity(i);
                }
            }
        }
    }

    private class AuthorClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            View parent = (View) (view.getParent()).getParent();
            int pos = recyclerView.getChildLayoutPosition(parent);
            Object object = data.get(pos);

            if (object instanceof Liveticker) {
                Liveticker element = (Liveticker) data.get(pos);
                if (element.getAuthorID() != null) {
                    Intent i = new Intent(ctx, ChannelActivity.class);
                    i.putExtra("type", "view");
                    i.putExtra("authorID", element.getAuthorID());
                    ctx.startActivity(i);
                }
            }
        }
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }
}