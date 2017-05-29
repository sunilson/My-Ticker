package com.sunilson.pro4.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sunilson.pro4.BaseApplication;
import com.sunilson.pro4.R;
import com.sunilson.pro4.activities.BaseActivity;
import com.sunilson.pro4.activities.LivetickerActivity;
import com.sunilson.pro4.adapters.LivetickerRecyclerViewAdapter;
import com.sunilson.pro4.baseClasses.Liveticker;
import com.sunilson.pro4.baseClasses.LivetickerEvent;
import com.sunilson.pro4.baseClasses.User;
import com.sunilson.pro4.dialogFragments.ConfirmDialog;
import com.sunilson.pro4.dialogFragments.InfoDialog;
import com.sunilson.pro4.dialogFragments.LivetickerPictureCaptionDialog;
import com.sunilson.pro4.dialogFragments.RegisterDialog;
import com.sunilson.pro4.dialogFragments.ShareDialog;
import com.sunilson.pro4.exceptions.LivetickerEventSetException;
import com.sunilson.pro4.exceptions.LivetickerSetException;
import com.sunilson.pro4.utilities.Constants;
import com.sunilson.pro4.utilities.Utilities;
import com.sunilson.pro4.views.SubscribeButton;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.sunilson.pro4.R.string.loading;

/**
 * @author Linus Weiss
 */

public class LivetickerFragment extends BaseFragment implements View.OnClickListener {

    private DatabaseReference livetickerContentReference, viewerCountRef, livetickerReference, subscriptionReference, likeReference, eventResultReference, viewerRef;
    private ChildEventListener livetickerContentListener;
    private ValueEventListener livetickerListener, viewerListener, authorListener, subscriptionListener, likedListener, eventResultListener;
    private StorageReference imageRef, thumbRef;
    private byte[] image, thumbnail;
    private Liveticker liveticker;
    private User author;
    private MenuItem notificationMenuItem;
    private FirebaseStorage storage;
    private boolean owner, subscribed, liked, started, notificationsOn, loadedFirstItem;
    private LivetickerRecyclerViewAdapter livetickerAdapter;
    private TextView status;
    private int loaded = 0;
    private int oldCommentCount = 0;
    private EditText textInput;
    private LinearLayout inputLayout;
    private ImageButton cameraButton, sendButton;
    private RelativeLayout textInputLoading;
    private Timer timer;
    private String caption, thumbnailUrl, livetickerID;
    private Bundle savedReferences;

    @BindView(R.id.fragment_liveticker_recyclerView)
    RecyclerView livetickerContents;

    @BindView(R.id.progress_overlay)
    View progressOverlay;

    @BindView(R.id.progress_overlay_progressbar)
    ProgressBar progressBarImageUpload;

    @BindView(R.id.fragment_liveticker_author_box)
    LinearLayout authorBox;

    @BindView(R.id.fragment_liveticker_container)
    RelativeLayout container;

    @BindView(R.id.fragment_liveticker_username)
    TextView userName;

    @BindView(R.id.fragment_liveticker_profile_picture)
    ImageView profilePicture;

    @BindView(R.id.subscribe_button)
    Button subscribeButton;

    @BindView(R.id.subscribe_button_view)
    SubscribeButton subscribeButtonView;

    @BindView(R.id.fragment_liveticker_like_icon)
    ImageView likeIcon;

    @BindView(R.id.fragment_liveticker_like_button)
    LinearLayout likeButton;

    @BindView(R.id.fragment_liveticker_comment_button)
    LinearLayout commentButton;

    @BindView(R.id.fragment_liveticker_edit_delete)
    ImageButton deleteIcon;

    @BindView(R.id.fragment_liveticker_edit_state)
    ImageButton editState;

    @BindView(R.id.fragment_liveticker_state_text)
    TextView stateText;

    @BindView(R.id.fragment_liveticker_state_layout)
    LinearLayout stateLayout;

    @BindView(R.id.fragment_liveticker_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.fragment_liveticker_scrollView)
    NestedScrollView scrollView;

    @BindView(R.id.fragment_liveticker_viewer_count)
    TextView viewerCount;

    @BindView(R.id.fragment_liveticker_like_count)
    TextView likeCount;

    @BindView(R.id.fragment_liveticker_comment_count)
    TextView commentCount;

    @BindView(R.id.fragment_liveticker_comment_icon_count)
    TextView commentIconCount;

    @BindView(R.id.fragment_liveticker_comment_icon_red_circle)
    FrameLayout redCircle;

    @BindView(R.id.fragment_liveticker_content_container)
    RelativeLayout contentContainer;

    @BindView(R.id.fragment_liveticker_refresh_button)
    Button refreshButton;

    @OnClick(R.id.fragment_liveticker_refresh_button)
    public void refresh() {
        Intent i = new Intent(getActivity(), LivetickerActivity.class);
        i.putExtra("livetickerID", livetickerID);
        startActivity(i);
        getActivity().finish();
    }

    /* ------------------------------------------- */
    /* ------------- Android Methods ------------- */
    /* ------------------------------------------- */

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

        livetickerID = getArguments().getString("livetickerID");
        storage = FirebaseStorage.getInstance();
        livetickerContentReference = ((BaseActivity) getActivity()).getReference().child(Constants.LIVETICKER_CONTENT_PATH).child(livetickerID);
        initializeContentListener();
        livetickerReference = FirebaseDatabase.getInstance().getReference("liveticker/" + livetickerID);
        savedReferences = new Bundle();

        initializeLivetickerListener();
        initializeAuthorListener();
        initializeSubscriptionListener();
        initializeLikedListener();
        initializeEventResultListener();
        initializeViewerListener();
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();


        if (!((BaseApplication) getActivity().getApplication()).getInternetConnected()) {
            Toast.makeText(getContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
            refreshButton.setVisibility(VISIBLE);
            contentContainer.setVisibility(GONE);
            return;
        }

        loading(true);
        loaded = 0;
        loadingAddingNewEvent(false);

        livetickerAdapter.clear();
        loadedFirstItem = false;
        livetickerContentReference.addChildEventListener(livetickerContentListener);
        livetickerReference.addValueEventListener(livetickerListener);

        if (livetickerAdapter != null) {
            livetickerContents.setAdapter(livetickerAdapter = new LivetickerRecyclerViewAdapter(livetickerContents, getContext()));
        }

        if (savedReferences != null) {
            final String thumbRef = savedReferences.getString("thumbRef");
            if (thumbRef != null) {
                loadingAddingNewEvent(true);
                this.thumbRef = FirebaseStorage.getInstance().getReferenceFromUrl(thumbRef);

                List<UploadTask> tasks = this.thumbRef.getActiveUploadTasks();
                if (tasks.size() > 0) {
                    UploadTask task = tasks.get(0);

                    task.addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot thumbSnapshot) {
                            LivetickerFragment.this.thumbRef = null;
                            LivetickerFragment.this.imageRef.putBytes(image).addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot fullSnapshot) {
                                    LivetickerFragment.this.imageRef = null;
                                    loadingAddingNewEvent(false);
                                    createImageEvent(fullSnapshot.getDownloadUrl().toString(), thumbSnapshot.getDownloadUrl().toString(), caption);
                                }
                            }).addOnFailureListener(getActivity(), new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    loadingAddingNewEvent(false);
                                    progressBarImageUpload.setProgress(0);
                                    DialogFragment dialogFragment = ConfirmDialog.newInstance(Constants.CONFIRM_TYPE_IMAGE_UPLOAD, getString(R.string.image_upload_failure));
                                    dialogFragment.setTargetFragment(LivetickerFragment.this, Constants.CONFIRM_DIALOG_REQUEST);
                                    dialogFragment.show(getFragmentManager(), "dialog");
                                }
                            });
                            loadingAddingNewEvent(false);
                        }
                    });
                }
            } else {
                final String imageRef = savedReferences.getString("imageRef");
                if (imageRef != null) {
                    loadingAddingNewEvent(true);
                    this.imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageRef);

                    List<UploadTask> tasks = this.imageRef.getActiveUploadTasks();
                    if (tasks.size() > 0) {
                        UploadTask task = tasks.get(0);

                        task.addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot fullSnapshot) {
                                LivetickerFragment.this.imageRef = null;
                                loadingAddingNewEvent(false);
                                createImageEvent(fullSnapshot.getDownloadUrl().toString(), thumbnailUrl, caption);
                            }
                        });
                    }
                }
            }

            savedReferences = new Bundle();
        }
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

        if (subscriptionReference != null && subscriptionListener != null) {
            subscriptionReference.removeEventListener(subscriptionListener);
        }

        if (likedListener != null && likeReference != null) {
            likeReference.removeEventListener(likedListener);
        }

        if (eventResultReference != null && eventResultListener != null) {
            eventResultReference.removeEventListener(eventResultListener);
        }

        if (viewerListener != null && viewerRef != null) {
            viewerRef.removeEventListener(viewerListener);
        }

        if (viewerRef != null) {
            viewerRef.removeValue();
            viewerRef = null;
        }


        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        if (thumbRef != null) {
            savedReferences.putString("thumbRef", thumbRef.toString());
        }

        if (imageRef != null) {
            savedReferences.putString("imageRef", imageRef.toString());
        }

        loading(false);
    }

    public void onFragmentResumeFromBackstack() {
        if (owner) {
            inputLayout.setVisibility(View.VISIBLE);
            authorBox.setVisibility(View.VISIBLE);
            oldCommentCount = liveticker.getCommentCount();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_liveticker_2, container, false);
        unbinder = ButterKnife.bind(this, view);

        textInput = (EditText) getActivity().findViewById(R.id.fragment_liveticker_input);
        inputLayout = (LinearLayout) getActivity().findViewById(R.id.fragment_liveticker_input_layout);
        cameraButton = (ImageButton) getActivity().findViewById(R.id.fragment_liveticker_camera_button);
        sendButton = (ImageButton) getActivity().findViewById(R.id.fragment_liveticker_send_button);
        textInputLoading = (RelativeLayout) getActivity().findViewById(R.id.fragment_liveticker_input_loading_container);

        setupClickListeners();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        livetickerContents.setLayoutManager(linearLayoutManager);
        livetickerContents.setAdapter(livetickerAdapter = new LivetickerRecyclerViewAdapter(livetickerContents, getContext()));
        livetickerContents.setNestedScrollingEnabled(false);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorAccent, R.color.colorPrimary);

        status = (TextView) getActivity().findViewById(R.id.liveticker_status);
        return view;
    }

    @Override
    public void onClick(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        switch (view.getId()) {
            case R.id.fragment_liveticker_camera_button:
                dispatchTakePictureIntent();
                break;
            case R.id.fragment_liveticker_send_button:
                createTextEvent();
                break;
            case R.id.subscribe_button:
                if (user != null && !user.isAnonymous()) {
                    subscribeToAuthor();
                } else {
                    DialogFragment registerDialog = RegisterDialog.newInstance();
                    registerDialog.show(getFragmentManager(), "dialog");
                }
                break;
            case R.id.fragment_liveticker_like_button:
                toggleLike(user);
                break;
            case R.id.fragment_liveticker_comment_button:
                openCommentSection();
                break;
            case R.id.fragment_liveticker_comment_icon:
                openCommentSection();
                break;
            case R.id.fragment_liveticker_share_icon:
                DialogFragment dialog = ShareDialog.newInstance("https://firenote.at/liveticker/" + liveticker.getLivetickerID());
                dialog.show(getFragmentManager(), "dialog");
                break;
            case R.id.fragment_liveticker_profile_picture:
                openChannel(liveticker.getAuthorID());
                break;
            case R.id.fragment_liveticker_username:
                openChannel(liveticker.getAuthorID());
                break;
            case R.id.fragment_liveticker_edit_delete:
                DialogFragment dialogFragment = ConfirmDialog.newInstance("delete", getString(R.string.delete_liveticker_confirm));
                dialogFragment.setTargetFragment(this, Constants.CONFIRM_DIALOG_REQUEST);
                dialogFragment.show(getFragmentManager(), "dialog");
                break;
            case R.id.fragment_liveticker_edit_state:
                DialogFragment dialogFragmentState = null;
                if (liveticker.getState().equals(Constants.LIVETICKER_NOT_STARTED_STATE)) {
                    dialogFragmentState = ConfirmDialog.newInstance("state", getString(R.string.start_liveticker_confirm));
                } else if (liveticker.getState().equals(Constants.LIVETICKER_STARTED_STATE)) {
                    dialogFragmentState = ConfirmDialog.newInstance("state", getString(R.string.finish_liveticker_confirm));
                } else {
                    break;
                }
                dialogFragmentState.setTargetFragment(this, Constants.CONFIRM_DIALOG_REQUEST);
                dialogFragmentState.show(getFragmentManager(), "dialog");
                break;
        }
    }

    private void setupClickListeners() {
        cameraButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        subscribeButton.setOnClickListener(this);
        likeButton.setOnClickListener(this);
        commentButton.setOnClickListener(this);
        userName.setOnClickListener(this);
        profilePicture.setOnClickListener(this);
        deleteIcon.setOnClickListener(this);
        editState.setOnClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ;
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
            case Constants.CONFIRM_DIALOG_REQUEST:
                switch (resultCode) {
                    case Constants.CONFIRM_DIALOG_SUCCESS:
                        if (user != null && liveticker != null) {
                            switch (data.getStringExtra("type")) {
                                case "delete":
                                    deleteLiveticker();
                                    break;
                                case "state":
                                    toggleState();
                                    break;
                                case Constants.CONFIRM_TYPE_IMAGE_UPLOAD:
                                    storeImageToDatabase(null, null);
                                    break;
                            }
                        }
                        break;
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_liveticker, menu);
        if (menu != null) {
            notificationMenuItem = menu.findItem(R.id.liveticker_menu_notifications);
            if (((LivetickerActivity) getActivity()).currentFragment.equals(Constants.FRAGMENT_COMMENTS_TAG)) {
                menu.findItem(R.id.liveticker_menu_share).setVisible(false);
                menu.findItem(R.id.liveticker_menu_info).setVisible(false);
                notificationMenuItem.setVisible(false);
            } else {
                menu.findItem(R.id.liveticker_menu_share).setVisible(true);
                menu.findItem(R.id.liveticker_menu_info).setVisible(true);
                notificationMenuItem.setVisible(true);
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.liveticker_menu_share:
                DialogFragment dialog = ShareDialog.newInstance("https://firenote.at/liveticker/" + liveticker.getLivetickerID());
                dialog.show(getFragmentManager(), "dialog");
                break;
            case R.id.liveticker_menu_info:
                DialogFragment dialog2 = InfoDialog.newInstance(liveticker.getDescription());
                dialog2.show(getFragmentManager(), "dialog");
                break;
            case R.id.liveticker_menu_notifications:
                toggleNotifications();
                break;
        }
        return super.onOptionsItemSelected(item);
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
        }
    }



    /* ------------------------------------------- */
    /* -------------- Event Methods -------------- */
    /* ------------------------------------------- */

    /**
     * Store Bitmap to Firebase Storage with unique Name. Then delete local image.
     */
    private void storeImageToDatabase(Bitmap bitmap, String caption) {
        loadingAddingNewEvent(true);

        if (this.caption == null) {
            this.caption = caption;
        }

        //Create references to Storage
        if (imageRef == null || thumbRef == null) {
            String uniqueId = UUID.randomUUID().toString();
            imageRef = storage.getReference().child("livetickerImages/" + liveticker.getAuthorID() + "/" + liveticker.getLivetickerID() + "/" + uniqueId + ".jpg");
            thumbRef = storage.getReference().child("livetickerImages/" + liveticker.getAuthorID() + "/" + liveticker.getLivetickerID() + "/thumbs/" + uniqueId + "_thumb.jpg");
        }

        //Get Full Image
        if (bitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            image = byteArrayOutputStream.toByteArray();
        }

        //Generate Thumbnail of Image
        if (bitmap != null) {
            Bitmap thumbMap = ThumbnailUtils.extractThumbnail(bitmap, 400, 225);
            ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
            thumbMap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream2);
            thumbnail = byteArrayOutputStream2.toByteArray();
        }

        if (thumbnail != null && image != null) {
            thumbRef.putBytes(thumbnail).addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot thumbSnapshot) {
                    thumbRef = null;
                    thumbnailUrl = thumbSnapshot.getDownloadUrl().toString();
                    imageRef.putBytes(image).addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot fullSnapshot) {
                            imageRef = null;
                            loadingAddingNewEvent(false);
                            createImageEvent(fullSnapshot.getDownloadUrl().toString(), thumbnailUrl, LivetickerFragment.this.caption);
                        }
                    }).addOnFailureListener(getActivity(), new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingAddingNewEvent(false);
                            Toast.makeText(getContext(), R.string.image_upload_failure, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(getActivity(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingAddingNewEvent(false);
                    Toast.makeText(getContext(), R.string.image_upload_failure, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            loadingAddingNewEvent(false);
        }
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

        if (!textInput.getText().toString().isEmpty() && !liveticker.getState().equals(Constants.LIVETICKER_NOT_STARTED_STATE)) {
            LivetickerEvent event = new LivetickerEvent();
            event.setType("text");
            try {
                event.setContent(textInput.getText().toString());
            } catch (LivetickerEventSetException e) {
                e.printStackTrace();
            }
            addEventToDatabase(event);
        }
    }

    /**
     * Pushes given event to Firebase Server
     *
     * @param event Finished Event
     */
    private void addEventToDatabase(final LivetickerEvent event) {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        loadingAddingNewEvent(true);

        if (user == null) {
            return;
        }

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

        final DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("request/" + user.getUid() + "/addLivetickerEvent/").push();
        dRef.setValue(map).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingAddingNewEvent(false);
                Toast.makeText(getActivity(), R.string.add_liveticker_event_failure, Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                if (eventResultReference != null) {
                    eventResultReference.removeEventListener(eventResultListener);
                }

                eventResultReference = FirebaseDatabase.getInstance().getReference("result/" + user.getUid() + "/addLivetickerEvent/" + dRef.getKey());
                eventResultReference.addValueEventListener(eventResultListener);
            }
        });
    }

    private void loadingAddingNewEvent(boolean loading) {
        if (loading) {
            textInputLoading.setVisibility(VISIBLE);
            sendButton.setEnabled(false);
        } else {
            textInputLoading.setVisibility(GONE);
            sendButton.setEnabled(true);
        }
    }

    /**
     * Take picture from camera on generated URI
     */
    public void dispatchTakePictureIntent() {

        if (Utilities.requestCameraPermission(getActivity())) {
            startCamera(Constants.REQUEST_IMAGE_CAPTURE);
        }
    }

    private void startCamera(int requestCode) {
        DialogFragment dialogFragment = LivetickerPictureCaptionDialog.newInstance(null);
        dialogFragment.setTargetFragment(this, Constants.PICTURE_DIALOG_REQUEST_CODE);
        dialogFragment.show(getFragmentManager(), "dialog");
    }

    /* ------------------------------------------- */
    /* -----------Initialize Listeners ----------- */
    /* ------------------------------------------- */

    private void initializeContentListener() {
        livetickerContentListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    if(!loadedFirstItem) {
                        loadedFirstItem = true;
                        checkDoneLoading();
                    }

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

    private void initializeEventResultListener() {
        eventResultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.child("state") != null && dataSnapshot.child("state").getValue().toString().equals("success")) {
                    loadingAddingNewEvent(false);
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(textInput.getWindowToken(), 0);
                    textInput.setText("");
                } else if (dataSnapshot.getValue() != null && dataSnapshot.child("state") != null && dataSnapshot.child("state").getValue().toString().equals("error")) {
                    loadingAddingNewEvent(false);
                    ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(textInput.getWindowToken(), 0);
                    Toast.makeText(getContext(), R.string.error_adding_event, Toast.LENGTH_SHORT).show();
                }
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

                    if (oldCommentCount == 0) oldCommentCount = liveticker.getCommentCount();
                    if (oldCommentCount != liveticker.getCommentCount())
                        updateCommentIcon(liveticker.getCommentCount() - oldCommentCount);

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user != null) {
                        checkOwnership(user);

                        if (!owner) {
                            startSubscriptionListener(user);
                            addToRecentlyVisited(user);
                        }

                        registerAsViewer(user);
                        startLikeListener(user);
                        startNotificationsListener(user);

                        if (!started) {
                            startAuthorListener();
                            startViewListener();
                        }
                    }

                    checkDoneLoading();
                } else {
                    Toast.makeText(getContext(), R.string.liveticker_load_failure, Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void initializeViewerListener() {
        viewerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    viewerCount.setText(Integer.toString(dataSnapshot.getValue(Integer.class)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    /* ------------------------------------------- */
    /* --------- Listener Helper Methods --------- */
    /* ------------------------------------------- */

    private void checkOwnership(FirebaseUser user) {
        if (liveticker != null) {
            if (user.getUid().equals(liveticker.getAuthorID())) {
                owner = true;
                updateViews("both");
            } else {
                owner = false;
                updateViews("both");
            }
        }
    }

    private void startSubscriptionListener(FirebaseUser user) {
        //First remove current Listener
        if (subscriptionReference != null && subscriptionListener != null) {
            subscriptionReference.removeEventListener(subscriptionListener);
        }
        //Start new listener
        subscriptionReference = FirebaseDatabase.getInstance().getReference("subscriptions/" + liveticker.getAuthorID() + "/" + user.getUid());
        subscriptionReference.addValueEventListener(subscriptionListener);
    }

    private void addToRecentlyVisited(FirebaseUser user) {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("recentlyVisited/" + user.getUid() + "/" + liveticker.getLivetickerID());
        dRef.setValue(ServerValue.TIMESTAMP);
    }

    private void refreshViewer() {
        final Handler handler = new Handler();
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        registerAsViewer(FirebaseAuth.getInstance().getCurrentUser());
                    }
                });
            }
        };

        timer.schedule(doAsynchronousTask, 300000, 300000);
    }

    private void startViewListener() {
        viewerCountRef = FirebaseDatabase.getInstance().getReference("viewerCount/" + liveticker.getLivetickerID());
        viewerCountRef.addValueEventListener(viewerListener);
    }

    private void startNotificationsListener(FirebaseUser user) {
        FirebaseDatabase.getInstance().getReference("notifications/" + liveticker.getLivetickerID() + "/" + user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                notificationsOn = dataSnapshot != null && dataSnapshot.getValue() != null && dataSnapshot.getValue(Boolean.class);
                checkNotifications();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void startAuthorListener() {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("users/" + liveticker.getAuthorID());
        dRef.addListenerForSingleValueEvent(authorListener);
    }

    private void startLikeListener(FirebaseUser user) {
        if (likeReference != null && likedListener != null) {
            likeReference.removeEventListener(likedListener);
        }

        likeReference = FirebaseDatabase.getInstance().getReference("liked/" + liveticker.getLivetickerID() + "/" + user.getUid());
        likeReference.addValueEventListener(likedListener);
    }

    private void registerAsViewer(FirebaseUser user) {
        if (user != null && viewerRef == null) {
            viewerRef = FirebaseDatabase.getInstance().getReference("viewer/" + liveticker.getLivetickerID() + "/" + user.getUid());
            refreshViewer();
        }

        viewerRef.setValue(ServerValue.TIMESTAMP);
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

                status.setVisibility(View.VISIBLE);
                if (liveticker.getStatus() != null && !liveticker.getStatus().isEmpty()) {
                    status.setText(liveticker.getStatus());
                }

                commentCount.setText(Integer.toString(liveticker.getCommentCount()));
                likeCount.setText(Integer.toString(liveticker.getLikeCount()));

                if (liveticker.getState() != null) {
                    ((LivetickerActivity) getActivity()).updateLivetickerState(liveticker.getState());
                    if (liveticker.getState().equals(Constants.LIVETICKER_STARTED_STATE)) {
                        editState.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_stop_white_24dp));
                        stateLayout.setVisibility(View.GONE);
                        stateText.setText("Started at " + liveticker.getStateTimestamp());
                    } else if (liveticker.getState().equals(Constants.LIVETICKER_NOT_STARTED_STATE)) {
                        editState.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_play_circle_filled_white_24dp));
                        stateLayout.setVisibility(View.VISIBLE);
                        new CountDownTimer(liveticker.getStateTimestamp() - Calendar.getInstance().getTimeInMillis(), 1000) {
                            @Override
                            public void onTick(long ms) {
                                long days = TimeUnit.MILLISECONDS.toDays(ms);
                                ms -= TimeUnit.DAYS.toMillis(days);

                                long hours = TimeUnit.MILLISECONDS.toHours(ms);
                                ms -= TimeUnit.HOURS.toMillis(hours);

                                long minutes = TimeUnit.MILLISECONDS.toMinutes(ms);
                                ms -= TimeUnit.MINUTES.toMillis(minutes);

                                long seconds = TimeUnit.MILLISECONDS.toSeconds(ms);

                                stateText.setText("Starting in " + days + " days, " + hours + " hours, " + minutes + "minutes, and " + seconds + " seconds!");
                            }

                            @Override
                            public void onFinish() {
                                stateText.setText("Done");
                            }
                        }.start();
                    } else {
                        editState.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_flag_white_24dp));
                        stateLayout.setVisibility(View.GONE);
                        stateText.setText("Finished at " + liveticker.getStateTimestamp());
                    }
                }
            }
        }

        if (type.equals("author") || type.equals("both")) {
            if (author != null) {
                if (author.getUserName() != null) {
                    userName.setText(author.getUserName());
                }

                if (author.getProfilePicture() != null) {
                    DrawableRequestBuilder<Integer> placeholder = Glide.with(getContext()).load(R.drawable.profile_placeholder).bitmapTransform(new CropCircleTransformation(getContext()));
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(author.getProfilePicture());
                    Glide.with(getContext()).using(new FirebaseImageLoader()).load(storageReference).thumbnail(placeholder).bitmapTransform(new CropCircleTransformation(getContext())).crossFade().into(profilePicture);
                }
            }
        }
    }

    public void subscribeToAuthor() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null && liveticker != null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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

    public void updateCommentIcon(int count) {
        if (count > 0 && count < 10) {
            commentIconCount.setText(String.valueOf(count));
        }

        redCircle.setVisibility((count > 0) ? VISIBLE : GONE);
    }

    private void checkDoneLoading() {
        loaded++;

        if (loaded >= 4) {
            loading(false);
            loaded = 0;
        } else if (owner && loaded >= 3) {
            loading(false);
            loaded = 0;
        }
    }

    private void loading(boolean loading) {
        if (loading) {
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setRefreshing(true);
            container.setVisibility(View.GONE);
        } else {
            started = true;
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setEnabled(false);
            container.setVisibility(View.VISIBLE);
            if (owner) {
                inputLayout.setVisibility(View.VISIBLE);
                authorBox.setVisibility(View.VISIBLE);
            } else {
                inputLayout.setVisibility(View.GONE);
                authorBox.setVisibility(View.GONE);
            }
        }
    }

    /* ------------------------------------------- */
    /* ------------ Liveticker Methods ----------- */
    /* ------------------------------------------- */

    private void deleteLiveticker() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("request/" + user.getUid() + "/deleteLiveticker/").push();
        Map<String, String> map = new HashMap<>();
        map.put("livetickerID", liveticker.getLivetickerID());
        dRef.setValue(map).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), R.string.liveticker_deletion_failure, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleState() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("request/" + user.getUid() + "/toggleState/").push();
        Map<String, String> map = new HashMap<>();
        map.put("livetickerID", liveticker.getLivetickerID());
        dRef.setValue(map).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), R.string.liveticker_toggle_failure, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkNotifications() {
        if (notificationsOn) {
            notificationMenuItem.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_notifications_active_white_24dp));
        } else {
            notificationMenuItem.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_notifications_none_white_24dp));

        }
    }

    private void toggleNotifications() {

        notificationsOn = !notificationsOn;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (liveticker != null && liveticker.getLivetickerID() != null && user != null && !user.isAnonymous()) {
            if (!notificationsOn) {
                FirebaseDatabase.getInstance().getReference("notifications/" + liveticker.getLivetickerID() + "/" + user.getUid()).removeValue().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), R.string.connect_failure, Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), R.string.notifications_deactivated, Toast.LENGTH_SHORT).show();
                        checkNotifications();
                    }
                });
            } else {
                FirebaseDatabase.getInstance().getReference("notifications/" + liveticker.getLivetickerID() + "/" + user.getUid()).setValue(true).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), R.string.connect_failure, Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), R.string.notifications_activated, Toast.LENGTH_SHORT).show();
                        checkNotifications();
                    }
                });
            }
        }
    }

    private void toggleLike(FirebaseUser user) {
        if (liveticker != null && liveticker.getLivetickerID() != null && user != null && !user.isAnonymous()) {
            if (liked) {
                FirebaseDatabase.getInstance().getReference("liked/" + liveticker.getLivetickerID() + "/" + user.getUid()).removeValue().addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), R.string.connect_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                FirebaseDatabase.getInstance().getReference("liked/" + liveticker.getLivetickerID() + "/" + user.getUid()).setValue(true).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), R.string.connect_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            DialogFragment registerDialog = RegisterDialog.newInstance();
            registerDialog.show(getFragmentManager(), "dialog");
        }
    }

    private void openCommentSection() {
        if (liveticker.getLivetickerID() != null) {
            oldCommentCount = liveticker.getCommentCount();
            updateCommentIcon(0);
            ((LivetickerActivity) getActivity()).replaceFragment(CommentsFragment.newInstance(liveticker.getLivetickerID()), Constants.FRAGMENT_COMMENTS_TAG);
        }

    }
}
