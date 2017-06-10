package com.sunilson.pro4.adapters;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
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
import com.sunilson.pro4.baseClasses.LivetickerEvent;
import com.sunilson.pro4.dialogFragments.LivetickerPictureViewDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static com.sunilson.pro4.utilities.Constants.CONTENT_TYPE_IMAGE;
import static com.sunilson.pro4.utilities.Constants.CONTENT_TYPE_STATE;
import static com.sunilson.pro4.utilities.Constants.CONTENT_TYPE_TEXT;

/**
 * @author Linus Weiss
 */

public class LivetickerRecyclerViewAdapter extends RecyclerView.Adapter {

    private ArrayList<LivetickerEvent> data = new ArrayList<>();
    private RecyclerView liveticker;
    private DateFormat dateFormat, timeFormat;
    private View.OnClickListener onImageClickListener = new LivetickerClickListener();
    private Context ctx;

    public LivetickerRecyclerViewAdapter(RecyclerView recyclerView, Context ctx) {
        this.ctx = ctx;
        this.dateFormat = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
        this.liveticker = recyclerView;
    }

    private class ViewHolderText extends RecyclerView.ViewHolder {

        TextView content, date, time;

        public ViewHolderText(View itemView) {
            super(itemView);
            content = (TextView) itemView.findViewById(R.id.liveticker_recyclerView_textElement_text);
            date = (TextView) itemView.findViewById(R.id.liveticker_recyclerView_textElement_date);
            time = (TextView) itemView.findViewById(R.id.liveticker_recyclerView_textElement_time);
        }
    }

    private class ViewHolderImage extends RecyclerView.ViewHolder {

        TextView caption, date, time;
        ImageView image;

        public ViewHolderImage(View itemView) {
            super(itemView);
            caption = (TextView) itemView.findViewById(R.id.liveticker_recyclerView_imageElement_caption);
            image = (ImageView)itemView.findViewById(R.id.liveticker_recyclerView_imageElement_image);
            date = (TextView) itemView.findViewById(R.id.liveticker_recyclerView_imageElement_date);
            time = (TextView) itemView.findViewById(R.id.liveticker_recyclerView_imageElement_time);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;

        if(viewType == 0) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.liveticker_recyclerview_text_element, parent, false);
            return new ViewHolderText(v);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.liveticker_recyclerview_image_element, parent, false);
            v.setOnClickListener(onImageClickListener);
            return new ViewHolderImage(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        LivetickerEvent event = data.get(position);

        if(event.getType().equals(CONTENT_TYPE_TEXT)) {
            ViewHolderText viewHolderText = (ViewHolderText) holder;
            viewHolderText.content.setText(event.getContent());
            viewHolderText.date.setText(dateFormat.format(event.getTimestamp()));
            viewHolderText.time.setText(timeFormat.format(event.getTimestamp()));
        } else if (event.getType().equals(CONTENT_TYPE_IMAGE)) {
            ViewHolderImage viewHolderImage = (ViewHolderImage) holder;
            if (event.getThumbnail() != null) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(event.getThumbnail());
                DrawableRequestBuilder<Integer> placeholder = Glide.with(ctx).load(R.drawable.thumbnail_placeholder).bitmapTransform(new RoundedCornersTransformation(ctx, 3, 0, RoundedCornersTransformation.CornerType.ALL));
                Glide.with(ctx).using(new FirebaseImageLoader()).load(storageReference).thumbnail(placeholder).bitmapTransform(new RoundedCornersTransformation(ctx, 3, 0, RoundedCornersTransformation.CornerType.ALL)).animate(android.R.anim.fade_in).into(viewHolderImage.image);
            }

            //Picasso.with(ctx).load(event.getThumbnail()).placeholder(R.drawable.default_placeholder).into(viewHolderImage.image);
            viewHolderImage.date.setText(dateFormat.format(event.getTimestamp()));
            viewHolderImage.time.setText(timeFormat.format(event.getTimestamp()));
            if (event.getCaption() != null) {
                viewHolderImage.caption.setText(event.getCaption());
            }
        } else if (event.getType().equals(CONTENT_TYPE_STATE)) {
            ViewHolderText viewHolderText = (ViewHolderText) holder;
            if (event.getContent().equals("started")) {
                viewHolderText.content.setText(ctx.getString(R.string.started_liveticker));
            } else if (event.getContent().equals("created")) {
                viewHolderText.content.setText(ctx.getString(R.string.created_liveticker));
            } else if (event.getContent().equals("finished")) {
                viewHolderText.content.setText(ctx.getString(R.string.finished_liveticker));
            }
            viewHolderText.date.setText(dateFormat.format(event.getTimestamp()));
            viewHolderText.time.setText(timeFormat.format(event.getTimestamp()));
        }
    }

    @Override
    public int getItemViewType(int position) {
        LivetickerEvent event = data.get(position);
        if(event.getType().equals("text") || event.getType().equals("state")) {
            return 0;
        } else {
            return 1;
        }
    }

    public void addEvent(LivetickerEvent event) {
        data.add(event);
        notifyItemInserted(data.indexOf(event));
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private class LivetickerClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int pos = liveticker.getChildLayoutPosition(view);
            LivetickerEvent element = data.get(pos);
            DialogFragment dialogFragment = LivetickerPictureViewDialog.newInstance(element.getContent(), element.getCaption());
            dialogFragment.show(((AppCompatActivity)ctx).getSupportFragmentManager(), "dialog");
        }
    }
}
