package com.sunilson.pro4.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunilson.pro4.R;

import java.util.ArrayList;

/**
 * @author Linus Weiss
 */

public class SimpleDialogAdapter extends ArrayAdapter {

    private ArrayList<DataStructure> data = new ArrayList<>();
    private Context ctx;
    private int resource;

    public SimpleDialogAdapter(Context context, int resource) {
        super(context, resource);
        this.resource = resource;
        this.ctx = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = inflater.inflate(resource, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.text = (TextView) convertView.findViewById(R.id.pick_image_dialog_text);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.pick_image_dialog_icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        DataStructure dataStructure = data.get(position);
        viewHolder.text.setText(dataStructure.text);
        viewHolder.icon.setImageDrawable(ContextCompat.getDrawable(getContext(), dataStructure.icon));

        return convertView;
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    public String getStringAtPosition(int pos) {
        return data.get(pos).text;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    public void add(String text, int icon) {
        data.add(new DataStructure(text, icon));
        notifyDataSetChanged();
    }

    private class DataStructure {
        public String text;
        public int icon;

        DataStructure(String text, int icon) {
            this.text = text;
            this.icon = icon;
        }
    }

    static class ViewHolder {
        TextView text;
        ImageView icon;
    }
}
