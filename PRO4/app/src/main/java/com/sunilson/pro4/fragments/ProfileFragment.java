package com.sunilson.pro4.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sunilson.pro4.R;
import com.sunilson.pro4.activities.AuthenticationActivity;
import com.sunilson.pro4.activities.ChannelActivity;
import com.sunilson.pro4.adapters.FeedRecyclerViewAdapter;
import com.sunilson.pro4.baseClasses.Liveticker;
import com.sunilson.pro4.baseClasses.User;
import com.sunilson.pro4.dialogFragments.SettingsDialog;
import com.sunilson.pro4.exceptions.LivetickerSetException;
import com.sunilson.pro4.interfaces.FragmentAuthInterface;
import com.sunilson.pro4.utilities.Utilities;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Linus Weiss
 */

public class ProfileFragment extends BaseFragment implements FragmentAuthInterface {

    private FeedRecyclerViewAdapter adapter;
    private DatabaseReference livetickerReference;
    private ChildEventListener livetickerListener;

    @BindView(R.id.fragment_profile_anonymous)
    LinearLayout profileAnonymous;

    @BindView(R.id.fragment_profile_registered)
    LinearLayout profileRegistered;

    @BindView(R.id.fragment_profile_recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.fragment_profile_load_more)
    Button loadMore;

    @BindView(R.id.fragment_profile_placeholder)
    TextView placeholder;

    @BindView(R.id.fragment_profile_username)
    TextView userName;

    @BindView(R.id.fragment_profile_picture)
    ImageView profilePicture;

    @BindView(R.id.fragment_profile_title_image)
    ImageView titlePicture;


    @OnClick(R.id.fragment_profile_anonymous_button)
    public void register() {
        Intent i = new Intent(getActivity(), AuthenticationActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.fragment_profile_edit_channel)
    public void editProfile() {
        Intent i = new Intent(getActivity(), ChannelActivity.class);
        i.putExtra("type", "editChannel");
        startActivity(i);
    }

    @OnClick(R.id.fragment_profile_view_channel)
    public void viewProfile() {
        Intent i = new Intent(getActivity(), ChannelActivity.class);
        i.putExtra("type", "view");
        i.putExtra("authorID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        startActivity(i);
    }

    @OnClick(R.id.fragment_profile_settings)
    public void openSettings() {
        DialogFragment dialogFragment = SettingsDialog.newInstance();
        dialogFragment.show(getFragmentManager(), "dialog");
    }

    @OnClick(R.id.fragment_profile_load_more)
    public void openLivetickerList() {
        Intent i = new Intent(getActivity(), ChannelActivity.class);
        i.putExtra("type", "list");
        i.putExtra("authorID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        startActivity(i);
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeLivetickerListener();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        unbinder = ButterKnife.bind(this, view);
        checkUser();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter = new FeedRecyclerViewAdapter(recyclerView, getContext()));
        recyclerView.setNestedScrollingEnabled(false);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void authChanged(FirebaseUser user) {
        checkUser();
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !user.isAnonymous()) {
            livetickerReference = FirebaseDatabase.getInstance().getReference("liveticker/");
            livetickerReference.orderByChild("idState").startAt(user.getUid() + "a").endAt(user.getUid() + "ab").limitToLast(5).addChildEventListener(livetickerListener);

            FirebaseDatabase.getInstance().getReference("users/" + user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user != null) {
                        Utilities.setupRoundImageViewWithPlaceholder(profilePicture, getContext(), user.getProfilePicture(), R.drawable.profile_placeholder);
                        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(user.getTitlePicture());
                        Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference).placeholder(R.drawable.title_placeholder).crossFade().into(titlePicture);
                        userName.setText(user.getUserName());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else {
            livetickerReference = null;
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (livetickerReference != null && livetickerListener != null) {
            livetickerReference.removeEventListener(livetickerListener);
            adapter.clear();
        }
    }

    private void initializeLivetickerListener() {
        livetickerListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Liveticker liveticker = dataSnapshot.getValue(Liveticker.class);

                if (liveticker != null) {
                    try {
                        liveticker.setLivetickerID(dataSnapshot.getKey());
                    } catch (LivetickerSetException e) {
                        e.printStackTrace();
                    }

                    adapter.add(liveticker);
                    if(adapter.getItemCount() == 5) {
                        loadMore.setVisibility(View.VISIBLE);
                    }

                    if (adapter.getItemCount() == 0) {
                        placeholder.setVisibility(View.VISIBLE);
                    } else {
                        placeholder.setVisibility(View.GONE);
                    }

                    adapter.sortByDate();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void checkUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.isAnonymous()) {
                profileAnonymous.setVisibility(View.VISIBLE);
                profileRegistered.setVisibility(View.GONE);
            } else {
                profileAnonymous.setVisibility(View.GONE);
                profileRegistered.setVisibility(View.VISIBLE);
            }
        }
    }
}