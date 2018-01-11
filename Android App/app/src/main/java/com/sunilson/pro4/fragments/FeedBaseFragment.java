package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sunilson.pro4.BaseApplication;
import com.sunilson.pro4.R;
import com.sunilson.pro4.baseClasses.Liveticker;
import com.sunilson.pro4.exceptions.LivetickerSetException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Linus Weiss
 */

public abstract class FeedBaseFragment extends BaseFragment {

    protected DatabaseReference currentResultReference;
    protected ValueEventListener resultListener;
    protected FirebaseAuth.AuthStateListener authStateListener;
    protected String path;
    protected String feedExtra;
    protected String currentUser;
    protected boolean started;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeAuthListener();
        initializeResultListener();
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (authStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        }

        if (currentResultReference != null && resultListener != null) {
            currentResultReference.removeEventListener(resultListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.feed_menu_refresh:
                requestFeed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected ArrayList<Liveticker> retrieveLivetickers(DataSnapshot snapshot, String child) {
        ArrayList<Liveticker> result = new ArrayList<>();

        for (DataSnapshot postSnapshot : snapshot.child(child).getChildren()) {
            Liveticker tempLiveticker = postSnapshot.getValue(Liveticker.class);
            if (tempLiveticker.getLivetickerID() == null) {
                try {
                    tempLiveticker.setLivetickerID(postSnapshot.getKey());
                } catch (LivetickerSetException e) {
                    e.printStackTrace();
                }
            }
            result.add(tempLiveticker);
        }

        return result;
    }


    public void requestFeed() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(getContext(), R.string.feed_load_failure, Toast.LENGTH_SHORT).show();
            loading(false);
            return;
        } else if (!((BaseApplication) getActivity().getApplication()).getInternetConnected()) {
            Toast.makeText(getContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
            loading(false);
            return;
        }

        //Remove current result listener
        if (currentResultReference != null && resultListener != null) {
            currentResultReference.removeEventListener(resultListener);
        }

        //Request Feed from Database
        loading(true);

        Map<String, String> map = new HashMap<>();
        if (feedExtra != null) {
            map.put("extra", feedExtra);
        }

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/request/" + user.getUid() + path).push();
        ref.setValue((feedExtra == null) ? true : map).addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Reassign result listeners
                currentResultReference = FirebaseDatabase.getInstance().getReference("/result/" + user.getUid() + path + ref.getKey());
                currentResultReference.addValueEventListener(resultListener);
            }
        }).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), R.string.feed_load_failure, Toast.LENGTH_SHORT).show();
                loading(false);
            }
        });
    }

    abstract void initializeAuthListener();

    abstract void initializeResultListener();

    abstract void loading(boolean loading);
}
