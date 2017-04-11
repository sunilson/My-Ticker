package com.sunilson.pro4.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import com.sunilson.pro4.dialogFragments.LivetickerPictureEditDialog;
import com.sunilson.pro4.exceptions.LivetickerEventSetException;
import com.sunilson.pro4.utilities.Constants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class LivetickerFragment extends BaseFragment implements View.OnClickListener {

    private DatabaseReference livetickerContentReference, currentResultReference;
    private ValueEventListener resultListener;
    private ChildEventListener livetickerContentListener;
    private FirebaseStorage storage;
    private Uri imageURI;
    private LivetickerRecyclerViewAdapter livetickerAdapter;

    @BindView(R.id.fragment_liveticker_recyclerView)
    RecyclerView livetickerContents;

    @BindView(R.id.fragment_liveticker_progressBar)
    ProgressBar progressBar;

    @BindView(R.id.fragment_liveticker_camera_button)
    Button cameraButton;

    @BindView(R.id.fragment_liveticker_send_button)
    Button sendButton;

    @BindView(R.id.fragment_liveticker_input)
    EditText textInput;

    @BindView(R.id.fragment_liveticker_progressBar_image_upload)
    ProgressBar progressBarImageUpload;

    public static LivetickerFragment newInstance() {
        LivetickerFragment livetickerFragment = new LivetickerFragment();
        return livetickerFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = FirebaseStorage.getInstance();

        livetickerContentReference = ((BaseActivity) getActivity()).getReference().child(Constants.LIVETICKER_CONTENT_PATH).child(((LivetickerActivity) getActivity()).getLiveticker().getLivetickerID());
        initializeContentListener();
    }

    @Override
    public void onStart() {
        super.onStart();

        livetickerContents.setLayoutManager(new LinearLayoutManager(getActivity()));
        livetickerContents.setAdapter(livetickerAdapter = new LivetickerRecyclerViewAdapter(livetickerContents, getContext()));
        initializeResultListener();

        livetickerContentReference.addChildEventListener(livetickerContentListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        //Remove Event Listener from Queue, if it has been started
        if (currentResultReference != null && resultListener != null) {
            currentResultReference.removeEventListener(resultListener);
        }

        if (livetickerContentReference != null && livetickerContentListener != null) {
            livetickerContentReference.removeEventListener(livetickerContentListener);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_liveticker, container, false);
        unbinder = ButterKnife.bind(this, view);
        cameraButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        return view;
    }

    private void initializeContentListener() {
        livetickerContentListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                livetickerAdapter.addEvent(dataSnapshot.getValue(LivetickerEvent.class));
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_liveticker_camera_button:
                dispatchTakePictureIntent();
                break;
            case R.id.fragment_liveticker_send_button:
                pushTextOnQueue();
                break;
        }
    }

    /**
     * Take picture from camera on generated URI
     */
    private void dispatchTakePictureIntent() {
        Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (imageIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (photoFile != null) {
                //Generate URI, so image can later be accessed again
                imageURI = FileProvider.getUriForFile(getActivity(), "com.sunilson.pro4.fileprovider", photoFile);

                //Start Camera activity
                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
                startActivityForResult(imageIntent, Constants.REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            //Result from Camera Activity with the captured picture
            case Constants.REQUEST_IMAGE_CAPTURE:
                if (imageURI != null) {
                    DialogFragment dialogFragment = LivetickerPictureEditDialog.newInstance(imageURI);
                    dialogFragment.setTargetFragment(this, Constants.PICTURE_DIALOG_REQUEST_CODE);
                    dialogFragment.show(getFragmentManager(), "dialog");
                }
                break;
            case Constants.PICTURE_DIALOG_REQUEST_CODE:
                switch (resultCode) {
                    case Constants.PICTURE_DIALOG_RESULT_CODE_SUCCESS:
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), (Uri) data.getExtras().getParcelable("imageURI"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        storeImageToDatabase(bitmap, data.getStringExtra("caption"));
                        break;
                    case Constants.PICTURE_DIALOG_RESULT_CODE_FAILURE:
                        getActivity().getContentResolver().delete((Uri) data.getExtras().getParcelable("imageURI"), null, null);
                        break;
                }
                break;
        }
    }

    /**
     * Store Bitmap to Firebase Storage with unique Name. Then delete local image.
     *
     * @param bitmap Image captured from camera
     */
    private void storeImageToDatabase(Bitmap bitmap, final String caption) {
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
        Bitmap thumbMap = ThumbnailUtils.extractThumbnail(bitmap, 200, 200);
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
                        pushImageOnQueue(fullSnapshot.getDownloadUrl().toString(), thumbSnapshot.getDownloadUrl().toString(), caption);
                        getActivity().getContentResolver().delete(imageURI, null, null);
                        imageURI = null;
                        progressBarImageUpload.setVisibility(View.GONE);
                        progressBarImageUpload.setProgress(0);
                    }
                });
            }
        });


    }

    /**
     * Creates a new Image Event and pushes it onto the Event Queue
     *
     * @param downloadURL URL of captured image in Firebase Storage
     */
    private void pushImageOnQueue(String downloadURL, String thumbURL, String caption) {
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
        pushEventToQueue(event);
    }

    private void pushTextOnQueue() {
        //TODO Text important?

        LivetickerEvent event = new LivetickerEvent();
        event.setType("text");
        try {
            event.setContent(textInput.getText().toString());
        } catch (LivetickerEventSetException e) {
            e.printStackTrace();
        }
        pushEventToQueue(event);
    }

    /**
     * Pushes given event to Firebase Server
     *
     * @param event Finished Event
     */
    private void pushEventToQueue(LivetickerEvent event) {
        loadingAddingNewEvent(true);

        Liveticker liveticker = ((LivetickerActivity) getActivity()).getLiveticker();

        event.setAuthorID(liveticker.getAuthorID());
        event.setLivetickerID(liveticker.getLivetickerID());
        final DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("request").child(Constants.LIVETICKER_ADD_EVENT_PATH).push();
        dRef.setValue(event).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Remove Event Listener from Queue, if it has been started
                if (currentResultReference != null && resultListener != null) {
                    currentResultReference.removeEventListener(resultListener);
                }
                //Listen for results from Queue
                DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference().child("result").child(Constants.LIVETICKER_ADD_EVENT_PATH).child(dRef.getKey());

                //Add Listener to Reference and store Reference so we can later detach Listener
                taskRef.addValueEventListener(resultListener);
                currentResultReference = taskRef;
            }
        });
    }

    private void initializeResultListener() {
        resultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("state").getValue() != null) {
                    if (dataSnapshot.child("state").getValue().toString().equals("success")) {
                        Toast.makeText(getActivity(), "Added", Toast.LENGTH_LONG).show();
                    } else if (dataSnapshot.child("state").getValue().toString().equals("error")) {
                        loadingAddingNewEvent(false);
                        Toast.makeText(getActivity(), dataSnapshot.child("errorDetails").getValue().toString(), Toast.LENGTH_LONG).show();
                    }
                    loadingAddingNewEvent(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void loadingAddingNewEvent(boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void finishAddingNewEvent() {

    }

    /**
     * Create file to be used for camera Activity
     *
     * @return File that can be used by Camera Activity to store Image
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        String mCurrentPath = image.getAbsolutePath();
        return image;
    }
}
