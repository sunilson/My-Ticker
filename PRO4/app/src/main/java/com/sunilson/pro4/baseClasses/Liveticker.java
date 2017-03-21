package com.sunilson.pro4.baseClasses;

import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

/**
 * @author Linus Weiss
 */

/**
 *
 */
public class Liveticker {

    private RecyclerView.Adapter eventAdapter, commentAdapter;
    private RecyclerView eventList, commentList;
    private DatabaseReference mReference;
    private String livetickerID;

    /**
     *
     * @param livetickerID
     * @param mReference
     */
    public Liveticker(String livetickerID, DatabaseReference mReference) {
        this.livetickerID = livetickerID;
        this.mReference = mReference;
    }

}
