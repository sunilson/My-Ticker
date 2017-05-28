package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sunilson.pro4.BaseApplication;
import com.sunilson.pro4.R;
import com.sunilson.pro4.adapters.ChannelFragmentPagerAdapter;
import com.sunilson.pro4.adapters.FeedRecyclerViewAdapter;
import com.sunilson.pro4.baseClasses.User;
import com.sunilson.pro4.dialogFragments.LivetickerPictureViewDialog;
import com.sunilson.pro4.dialogFragments.RegisterDialog;
import com.sunilson.pro4.utilities.Utilities;
import com.sunilson.pro4.views.ChannelViewPager;
import com.sunilson.pro4.views.SubscribeButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * @author Linus Weiss
 */

public class ChannelFragment extends BaseFragment implements View.OnClickListener {

    private String authorID;
    private DatabaseReference userRef, subscriptionReference;
    private ValueEventListener userListener, subscriptionListener;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FeedRecyclerViewAdapter adapter;
    private ChannelFragmentPagerAdapter fragmentPagerAdapter;
    private User user;
    private FirebaseUser firebaseUser;
    private boolean started, subscribed;
    private int loading = 0;

    @BindView(R.id.fragment_channel_container)
    FrameLayout container;

    @BindView(R.id.fragment_channel_swipe_layout)
    SwipeRefreshLayout swipeLayout;

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

    @BindView(R.id.subscribe_button_view)
    SubscribeButton subscribeButtonView;

    @BindView(R.id.subscribe_button)
    Button subscribeButton;

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
        if (!((BaseApplication) getActivity().getApplication()).getInternetConnected()) {
            Toast.makeText(getContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!started) {
            started = true;

            titlePicture.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                    float height = Utilities.dpFromPx(getContext(), titlePicture.getHeight()) - 50;
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

            loadChannel();
        }

        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (authStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
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
        initializeSubscriptionListener();
        initializeAuthListener();

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadChannel();
            }
        });

        subscribeButton.setOnClickListener(this);

        viewPager.setAdapter(fragmentPagerAdapter = new ChannelFragmentPagerAdapter(getActivity().getSupportFragmentManager(), getActivity(), authorID));
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    private void initializeSubscriptionListener() {
        subscriptionListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    subscribeButtonView.updateStatus(true);
                    subscribed = true;
                } else {
                    subscribeButtonView.updateStatus(false);
                    subscribed = false;
                }
                //checkLoading();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void initializeAuthListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                if (firebaseUser != null && !firebaseUser.getUid().equals(authorID)) {
                    if (subscriptionReference != null && subscriptionListener != null) {
                        subscriptionReference.removeEventListener(subscriptionListener);
                    }

                    //Start new listener
                    subscriptionReference = FirebaseDatabase.getInstance().getReference("subscriptions/" + authorID + "/" + firebaseUser.getUid());
                    subscriptionReference.addValueEventListener(subscriptionListener);
                }
            }
        };
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
            swipeLayout.setRefreshing(false);
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
                    DialogFragment dialogFragment = LivetickerPictureViewDialog.newInstance(user.getProfilePicture(), user.getUserName());
                    dialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
                }
            });

        }

        if (user.getUserName() != null) {
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

    public void subscribeToAuthor() {
        if (firebaseUser != null && authorID != null) {
            subscribeButtonView.loading(true);
            if (!subscribed) {
                DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("subscribedTo/" + firebaseUser.getUid() + "/" + authorID);
                dRef.setValue(true).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), R.string.subscribe_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("subscribedTo/" + firebaseUser.getUid() + "/" + authorID);
                dRef.removeValue().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), R.string.unsubscribe_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void loadChannel() {
        swipeLayout.setRefreshing(true);
        userRef.addListenerForSingleValueEvent(userListener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.subscribe_button:
                if (firebaseUser != null && !firebaseUser.isAnonymous()) {
                    subscribeToAuthor();
                } else {
                    DialogFragment registerDialog = RegisterDialog.newInstance();
                    registerDialog.show(getFragmentManager(), "dialog");
                }
                break;
        }
    }
}
