package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sunilson.pro4.R;
import com.sunilson.pro4.adapters.ChannelFragmentPagerAdapter;
import com.sunilson.pro4.adapters.FeedRecyclerViewAdapter;
import com.sunilson.pro4.baseClasses.User;
import com.sunilson.pro4.dialogFragments.LivetickerPictureViewDialog;
import com.sunilson.pro4.utilities.Constants;
import com.sunilson.pro4.utilities.Utilities;
import com.sunilson.pro4.views.ChannelViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * @author Linus Weiss
 */

public class ChannelFragment extends BaseFragment {

    private String authorID;
    private DatabaseReference userRef;
    private ValueEventListener userListener;
    private FeedRecyclerViewAdapter adapter;
    private ChannelFragmentPagerAdapter fragmentPagerAdapter;
    private User user;
    private boolean started;
    private int loading = 0;

    @BindView(R.id.fragment_channel_container)
    FrameLayout container;

    @BindView(R.id.fragment_channel_loading_container)
    LinearLayout loadingContainer;

    @BindView(R.id.fragment_channel_status)
    TextView status;

    @BindView(R.id.fragment_channel_username)
    TextView userName;

    @BindView(R.id.fragment_channel_profile_picture)
    ImageView profilePicture;

    @BindView(R.id.fragment_channel_title_picture)
    ImageView titlePicture;

    @BindView(R.id.fragment_channel_subscriber_count)
    TextView subscriberCount;

    @BindView(R.id.fragment_channel_info)
    TextView info;

    @BindView(R.id.fragment_channel_tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.fragment_channel_viewpager)
    ChannelViewPager viewPager;

    @BindView(R.id.toolbar_channel)
    Toolbar toolbar;

    public static ChannelFragment newInstance(String authorID) {
        Bundle args = new Bundle();
        args.putString("authorID", authorID);
        ChannelFragment channelFragment = new ChannelFragment();
        channelFragment.setArguments(args);
        return channelFragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!started) {
            started = true;

            userRef.addListenerForSingleValueEvent(userListener);

            titlePicture.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                    float height = Utilities.dpFromPx(getContext(), titlePicture.getHeight()) - 50;
                    Log.i(Constants.LOGGING_TAG, Float.toString(height));
                    if (height > 0) {
                        LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.fragment_channel_empty_view);
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
                        params.height = Math.round(Utilities.pxFromDp(getContext(), height));
                        linearLayout.setLayoutParams(params);
                    }
                }
            });

            ViewTreeObserver vto = titlePicture.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {


                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel, container, false);
        unbinder = ButterKnife.bind(this, view);

        authorID = getArguments().getString("authorID");
        userRef = FirebaseDatabase.getInstance().getReference("users/" + authorID);
        initializeUserListener();

        viewPager.setAdapter(fragmentPagerAdapter = new ChannelFragmentPagerAdapter(getActivity().getSupportFragmentManager(), getActivity(), authorID));
        tabLayout.setupWithViewPager(viewPager);

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        return view;
    }

    private void initializeUserListener() {
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);

                if (user != null) {
                    updateViews();
                    checkLoading();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void checkLoading() {
        loading++;
        if(loading >= 1) {
            container.setVisibility(View.VISIBLE);
            loadingContainer.setVisibility(View.GONE);
        }
    }

    private void updateViews() {

        if (user.getTitlePicture() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(user.getTitlePicture());
            Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference).crossFade().into(titlePicture);
        }

        if (user.getProfilePicture() != null) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(user.getProfilePicture());
            Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference).bitmapTransform(new CropCircleTransformation(getContext())).crossFade().into(profilePicture);

            profilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment dialogFragment = LivetickerPictureViewDialog.newInstance(user.getProfilePicture());
                    dialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
                }
            });

        }

        if (user.getUserName() != null) {
            getActivity().setTitle(user.getUserName());
            userName.setText(user.getUserName());
        }

        if (user.getStatus() != null && !user.getStatus().isEmpty()) {
            status.setText(user.getStatus());
        }


    /*
        if (user.getInfo() != null) {
            info.setContent(user.getInfo());
        }

        */
    }
}
