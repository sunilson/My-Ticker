package com.sunilson.pro4.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sunilson.pro4.R;
import com.sunilson.pro4.activities.BaseActivity;
import com.sunilson.pro4.activities.LivetickerActivity;
import com.sunilson.pro4.adapters.LivetickerRecyclerViewAdapter;
import com.sunilson.pro4.baseClasses.Liveticker;
import com.sunilson.pro4.baseClasses.LivetickerEvent;
import com.sunilson.pro4.baseClasses.User;
import com.sunilson.pro4.dialogFragments.LivetickerPictureCaptionDialog;
import com.sunilson.pro4.exceptions.LivetickerEventSetException;
import com.sunilson.pro4.exceptions.LivetickerSetException;
import com.sunilson.pro4.utilities.Constants;
import com.sunilson.pro4.utilities.Utilities;
import com.sunilson.pro4.views.SubscribeButton;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * @author Linus Weiss
 */

public class LivetickerFragment extends BaseFragment implements View.OnClickListener {

    private DatabaseReference livetickerContentReference, livetickerReference, subscriptionReference, authorReference, likeReference;
    private ChildEventListener livetickerContentListener;
    private ValueEventListener livetickerListener;
    private ValueEventListener authorListener;
    private ValueEventListener subscriptionListener;
    private ValueEventListener likedListener;
    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Liveticker liveticker;
    private User author;
    private FirebaseStorage storage;
    private boolean owner, subscribed, addedToRecent, liked;
    private LivetickerRecyclerViewAdapter livetickerAdapter;
    private TextView status;
    private int loaded = 0;

    @BindView(R.id.fragment_liveticker_recyclerView)
    RecyclerView livetickerContents;

    @BindView(R.id.fragment_liveticker_camera_button)
    ImageButton cameraButton;

    @BindView(R.id.fragment_liveticker_send_button)
    ImageButton sendButton;

    @BindView(R.id.fragment_liveticker_input)
    EditText textInput;

    @BindView(R.id.progress_overlay)
    View progressOverlay;

    @BindView(R.id.progress_overlay_progressbar)
    ProgressBar progressBarImageUpload;

    @BindView(R.id.fragment_liveticker_input_layout)
    LinearLayout inputLayout;

    @BindView(R.id.fragment_liveticker_loading_container)
    LinearLayout loadingContainer;

    @BindView(R.id.fragment_liveticker_container)
    LinearLayout container;

    @BindView(R.id.fragment_liveticker_username)
    TextView userName;

    @BindView(R.id.fragment_liveticker_profile_picture)
    ImageView profilePicture;

    @BindView(R.id.fragment_liveticker_title_image)
    ImageView titleImage;

    @BindView(R.id.subscribe_button)
    Button subscribeButton;

    @BindView(R.id.subscribe_button_view)
    SubscribeButton subscribeButtonView;

    @BindView(R.id.fragment_liveticker_like_icon)
    ImageButton likeIcon;

    @BindView(R.id.fragment_liveticker_comment_icon)
    ImageButton commentIcon;

    public static LivetickerFragment newInstance(String livetickerID) {
        LivetickerFragment livetickerFragment = new LivetickerFragment();
        Bundle args = new Bundle();
        args.putString("livetickerID", livetickerID);
        livetickerFragment.setArguments(args);
        return livetickerFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String livetickerID = getArguments().getString("livetickerID");
        storage = FirebaseStorage.getInstance();
        livetickerContentReference = ((BaseActivity) getActivity()).getReference().child(Constants.LIVETICKER_CONTENT_PATH).child(livetickerID);
        initializeContentListener();
        livetickerReference = FirebaseDatabase.getInstance().getReference("liveticker/" + livetickerID);

        initializeLivetickerListener();
        initializeAuthListener();
        initializeAuthorListener();
        initializeSubscriptionListener();
        initializeLikedListener();
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        livetickerAdapter.clear();

        loading(true);
        loaded = 0;

        livetickerContentReference.addChildEventListener(livetickerContentListener);
        livetickerReference.addValueEventListener(livetickerListener);
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (livetickerContentReference != null && livetickerContentListener != null) {
            livetickerContentReference.removeEventListener(livetickerContentListener);
        }

        if (livetickerListener != null) {
            livetickerReference.removeEventListener(livetickerListener);
        }

        if (authStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        }

        if (subscriptionReference != null && subscriptionListener != null) {
            subscriptionReference.removeEventListener(subscriptionListener);
        }

        if (likedListener != null && likeReference != null) {
            likeReference.removeEventListener(likedListener);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_liveticker, container, false);
        unbinder = ButterKnife.bind(this, view);
        cameraButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        subscribeButton.setOnClickListener(this);
        likeIcon.setOnClickListener(this);
        commentIcon.setOnClickListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        livetickerContents.setLayoutManager(linearLayoutManager);
        livetickerContents.setAdapter(livetickerAdapter = new LivetickerRecyclerViewAdapter(livetickerContents, getContext()));
        livetickerContents.setNestedScrollingEnabled(false);

        status = (TextView) getActivity().findViewById(R.id.liveticker_status);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_liveticker_camera_button:
                dispatchTakePictureIntent(Constants.REQUEST_IMAGE_CAPTURE);
                break;
            case R.id.fragment_liveticker_send_button:
                createTextEvent();
                break;
            case R.id.subscribe_button:
                if (user != null && !user.isAnonymous()) {
                    subscribeToAuthor();
                }
                break;
            case R.id.fragment_liveticker_like_icon:
                if (liveticker != null && user != null && !user.isAnonymous()) {
                    if (liked) {
                        FirebaseDatabase.getInstance().getReference("liked/" + liveticker.getLivetickerID() + "/" + user.getUid()).removeValue();
                    } else {
                        FirebaseDatabase.getInstance().getReference("liked/" + liveticker.getLivetickerID() + "/" + user.getUid()).setValue(true);
                    }
                }
                break;
            case R.id.fragment_liveticker_comment_icon:
                ((LivetickerActivity) getActivity()).replaceFragment(CommentsFragment.newInstance(liveticker.getLivetickerID()), Constants.FRAGMENT_COMMENTS_TAG);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            //Result from Camera Activity with the captured picture
            case Constants.PICTURE_DIALOG_REQUEST_CODE:
                switch (resultCode) {
                    case Constants.PICTURE_DIALOG_RESULT_CODE_SUCCESS:
                        storeImageToDatabase((Bitmap) data.getParcelableExtra("image"), data.getStringExtra("caption"));
                        break;
                    case Constants.PICTURE_DIALOG_RESULT_CODE_FAILURE:
                        //TODO Error Handling
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera(Constants.REQUEST_IMAGE_CAPTURE);
                } else {
                    Toast.makeText(getContext(), R.string.camera_permission_failure, Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    /**
     * Store Bitmap to Firebase Storage with unique Name. Then delete local image.
     */
    private void storeImageToDatabase(Bitmap bitmap, final String caption) {
        loadingAddingNewEvent(true);

        progressBarImageUpload.setVisibility(View.VISIBLE);

        String uniqueId = UUID.randomUUID().toString();

        //Create references to Storage
        final StorageReference imageRef = storage.getReference().child("livetickerImages/" + uniqueId + ".jpg");
        final StorageReference thumbRef = storage.getReference().child("livetickerImages/" + uniqueId + "_thumb.jpg");

        //Get Full Image
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        final byte[] data = byteArrayOutputStream.toByteArray();

        //Generate Thumbnail of Image
        Bitmap thumbMap = ThumbnailUtils.extractThumbnail(bitmap, 400, 225);
        ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
        thumbMap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream2);
        final byte[] thumbnail = byteArrayOutputStream2.toByteArray();

        progressBarImageUpload.setProgress(10);

        thumbRef.putBytes(thumbnail).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot thumbSnapshot) {
                progressBarImageUpload.setProgress(30);
                imageRef.putBytes(data).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        Double d = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressBarImageUpload.setProgress(d.intValue() + 30);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot fullSnapshot) {
                        loadingAddingNewEvent(false);
                        createImageEvent(fullSnapshot.getDownloadUrl().toString(), thumbSnapshot.getDownloadUrl().toString(), caption);
                        progressBarImageUpload.setProgress(0);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingAddingNewEvent(false);
                        Toast.makeText(getContext(), R.string.image_upload_failure, Toast.LENGTH_LONG).show();
                        progressBarImageUpload.setProgress(0);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingAddingNewEvent(false);
                Toast.makeText(getContext(), R.string.image_upload_failure, Toast.LENGTH_LONG).show();
                progressBarImageUpload.setProgress(0);
            }
        });
    }

    /**
     * Creates a new Image Event and pushes it onto the Event Queue
     *
     * @param downloadURL URL of captured image in Firebase Storage
     */
    private void createImageEvent(String downloadURL, String thumbURL, String caption) {
        //TODO Image important?

        LivetickerEvent event = new LivetickerEvent();
        event.setType("image");
        try {
            event.setContent(downloadURL);
        } catch (LivetickerEventSetException e) {
            e.printStackTrace();
        }

        event.setThumbnail(thumbURL);
        event.setCaption(caption);
        addEventToDatabase(event);
    }

    private void createTextEvent() {
        //TODO Text important?

        LivetickerEvent event = new LivetickerEvent();
        event.setType("text");
        try {
            event.setContent(textInput.getText().toString());
        } catch (LivetickerEventSetException e) {
            e.printStackTrace();
        }
        addEventToDatabase(event);
    }

    /**
     * Pushes given event to Firebase Server
     *
     * @param event Finished Event
     */
    private void addEventToDatabase(final LivetickerEvent event) {
        Map<String, Object> map = new HashMap<>();

        map.put("authorID", liveticker.getAuthorID());
        map.put("content", event.getContent());
        map.put("livetickerID", liveticker.getLivetickerID());
        map.put("timestamp", ServerValue.TIMESTAMP);
        map.put("type", event.getType());

        if (event.getType().equals("image")) {
            map.put("thumbnail", event.getThumbnail());
            map.put("caption", event.getCaption());
        }

        final DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("contents/" + liveticker.getLivetickerID()).push();
        dRef.setValue(map).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), R.string.add_liveticker_event_failure, Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (event.getType().equals("text")) {
                    textInput.setText("");
                }
            }
        });
    }

    private void loading(boolean loading) {
        if (loading) {
            loadingContainer.setVisibility(View.VISIBLE);
            container.setVisibility(View.GONE);
        } else {
            loadingContainer.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            if (owner) {
                inputLayout.setVisibility(View.VISIBLE);
            } else {
                inputLayout.setVisibility(View.GONE);
            }
        }
    }

    private void loadingAddingNewEvent(boolean loading) {
        if (loading) {
            Utilities.animateView(progressOverlay, View.VISIBLE, 0.4f, 200);
        } else {
            Utilities.animateView(progressOverlay, View.GONE, 0, 200);
        }
    }

    private void finishAddingNewEvent() {

    }

    /**
     * Take picture from camera on generated URI
     */
    public void dispatchTakePictureIntent(int requestCode) {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera(Constants.REQUEST_IMAGE_CAPTURE);
        }
    }

    private void startCamera(int requestCode) {
        DialogFragment dialogFragment = LivetickerPictureCaptionDialog.newInstance(null);
        dialogFragment.setTargetFragment(this, Constants.PICTURE_DIALOG_REQUEST_CODE);
        dialogFragment.show(getFragmentManager(), "dialog");
    }

    private void initializeContentListener() {
        livetickerContentListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot == null || dataSnapshot.getKey().equals("authorID")) {

                } else {
                    livetickerAdapter.addEvent(dataSnapshot.getValue(LivetickerEvent.class));
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

    private void initializeLikedListener() {
        likedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                liked = dataSnapshot.getValue() != null;
                if (liked) {
                    likeIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.like_icon_liked));
                } else {
                    likeIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.like_icon));
                }

                checkDoneLoading();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
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
                checkDoneLoading();
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
                user = firebaseAuth.getCurrentUser();
                checkOwnership();
            }
        };
    }

    private void initializeAuthorListener() {
        authorListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                author = dataSnapshot.getValue(User.class);
                updateViews("author");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void initializeLivetickerListener() {
        livetickerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                liveticker = dataSnapshot.getValue(Liveticker.class);
                if (liveticker != null) {
                    try {
                        liveticker.setLivetickerID(dataSnapshot.getKey());
                    } catch (LivetickerSetException e) {
                        e.printStackTrace();
                    }

                    //Start author listener
                    DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("users/" + liveticker.getAuthorID());
                    dRef.addListenerForSingleValueEvent(authorListener);

                    //Start subscription listener
                    if (user != null) {
                        checkOwnership();

                        if (!owner) {
                            //First remove current Listener
                            if (subscriptionReference != null && subscriptionListener != null) {
                                subscriptionReference.removeEventListener(subscriptionListener);
                            }

                            //Start new listener
                            subscriptionReference = FirebaseDatabase.getInstance().getReference("subscriptions/" + liveticker.getAuthorID() + "/" + user.getUid());
                            subscriptionReference.addValueEventListener(subscriptionListener);

                            if (!addedToRecent) {
                                addedToRecent = true;
                                addToRecentlyVisited();
                            }
                        }

                        if (likeReference != null && likedListener != null) {
                            likeReference.removeEventListener(likedListener);
                        }

                        likeReference = FirebaseDatabase.getInstance().getReference("liked/" + liveticker.getLivetickerID() + "/" + user.getUid());
                        likeReference.addValueEventListener(likedListener);
                        checkDoneLoading();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void checkOwnership() {
        if (user != null && liveticker != null) {
            if (user.getUid().equals(liveticker.getAuthorID())) {
                owner = true;
                updateViews("both");
            } else {
                owner = false;
                updateViews("both");
            }
        }
    }

    private void updateViews(String type) {

        if (type.equals("liveticker") || type.equals("both")) {
            if (liveticker != null) {

                if (liveticker.getTitle() != null) {
                    ((LivetickerActivity) getActivity()).updateLivetickerTitle(liveticker.getTitle());
                }
                if (liveticker.getDescription() != null) {
                    //description.setContent(liveticker.getDescription());
                }

                if (liveticker.getStatus() != null) {
                    status.setText(liveticker.getStatus());
                    status.setVisibility(View.VISIBLE);
                }

                if (liveticker.getState() != null) {
                    ((LivetickerActivity) getActivity()).updateLivetickerState(liveticker.getState());
                }
            }
        }

        if (type.equals("author") || type.equals("both")) {
            if (author != null) {
                if (author.getUserName() != null) {
                    userName.setText(author.getUserName());
                }

                if (author.getTitlePicture() != null) {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(author.getTitlePicture());
                    Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference).placeholder(R.drawable.default_placeholder).crossFade().into(titleImage);
                }

                if (author.getProfilePicture() != null) {
                    DrawableRequestBuilder<Integer> placeholder = Glide.with(getContext()).load(R.drawable.default_placeholder).bitmapTransform(new CropCircleTransformation(getContext()));
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(author.getProfilePicture());
                    Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference).thumbnail(placeholder).bitmapTransform(new CropCircleTransformation(getContext())).crossFade().into(profilePicture);
                }
            }
        }
    }

    public void subscribeToAuthor() {
        if (user != null && liveticker != null) {
            subscribeButtonView.loading(true);
            if (!subscribed) {
                DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("subscribedTo/" + user.getUid() + "/" + liveticker.getAuthorID());
                dRef.setValue(true).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), R.string.subscribe_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("subscribedTo/" + user.getUid() + "/" + liveticker.getAuthorID());
                dRef.removeValue().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), R.string.unsubscribe_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void addToRecentlyVisited() {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("recentlyVisited/" + user.getUid() + "/" + liveticker.getLivetickerID());
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", ServerValue.TIMESTAMP);
        dRef.setValue(map).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //TODO Error Handling
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_liveticker, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
            case R.id.liveticker_menu_comments:
                if (liveticker.getLivetickerID() != null) {
                    ((LivetickerActivity) getActivity()).replaceFragment(CommentsFragment.newInstance(liveticker.getLivetickerID()), Constants.FRAGMENT_COMMENTS_TAG);
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkDoneLoading() {
        loaded++;

        if (loaded >= 3) {
            loading(false);
        } else if (owner && loaded >= 2) {
            loading(false);
        }
    }
}
