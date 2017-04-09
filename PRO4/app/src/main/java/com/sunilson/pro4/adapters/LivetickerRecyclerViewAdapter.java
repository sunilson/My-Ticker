package com.sunilson.pro4.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.sunilson.pro4.R;
import com.sunilson.pro4.baseClasses.LivetickerEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Linus Weiss
 */

public class LivetickerRecyclerViewAdapter extends RecyclerView.Adapter {

    private ArrayList<LivetickerEvent> data = new ArrayList<>();
    private DateFormat dateFormat;
    private Context ctx;

    public LivetickerRecyclerViewAdapter(RecyclerView recyclerView, Context ctx) {
        this.ctx = ctx;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getDefault());
    }

    private class ViewHolderText extends RecyclerView.ViewHolder {

        TextView content, date;

        public ViewHolderText(View itemView) {
            super(itemView);
            content = (TextView) itemView.findViewById(R.id.liveticker_recyclerView_textElement_text);
            date = (TextView) itemView.findViewById(R.id.liveticker_recyclerView_textElement_date);
        }
    }

    private class ViewHolderImage extends RecyclerView.ViewHolder {

        TextView caption, date;
        ImageView image;

        public ViewHolderImage(View itemView) {
            super(itemView);
            caption = (TextView) itemView.findViewById(R.id.liveticker_recyclerView_imageElement_caption);
            image = (ImageView)itemView.findViewById(R.id.liveticker_recyclerView_imageElement_image);
            date = (TextView) itemView.findViewById(R.id.liveticker_recyclerView_imageElement_date);
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
            return new ViewHolderImage(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        LivetickerEvent event = data.get(position);

        if(event.getType().equals("text")) {
            ViewHolderText viewHolderText = (ViewHolderText) holder;
            viewHolderText.content.setText(event.getContent());
            viewHolderText.date.setText(dateFormat.format(event.getTimestamp()));
        } else if (event.getType().equals("image")) {
            ViewHolderImage viewHolderImage = (ViewHolderImage) holder;
            Picasso.with(ctx).load(event.getThumbnail()).placeholder(R.drawable.default_placeholder).into(viewHolderImage.image);
            viewHolderImage.date.setText(dateFormat.format(event.getTimestamp()));
        }
    }

    @Override
    public int getItemViewType(int position) {
        LivetickerEvent event = data.get(position);
        if(event.getType().equals("text")) {
            return 0;
        } else {
            return 1;
        }
    }

    public void addEvent(LivetickerEvent event) {
        data.add(event);
        notifyItemInserted(data.indexOf(event));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
