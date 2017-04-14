package com.sunilson.pro4.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialcamera.MaterialCamera;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
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
import com.sunilson.pro4.dialogFragments.LivetickerPictureCaptionDialog;
import com.sunilson.pro4.exceptions.LivetickerEventSetException;
import com.sunilson.pro4.utilities.Constants;
import com.sunilson.pro4.utilities.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class LivetickerFragment extends BaseFragment implements View.OnClickListener {

    private DatabaseReference livetickerContentReference, currentResultReference;
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

    @BindView(R.id.progress_overlay)
    View progressOverlay;

    @BindView(R.id.progress_overlay_progressbar)
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

        livetickerContentReference.addChildEventListener(livetickerContentListener);
    }

    @Override
    public void onStop() {
        super.onStop();

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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_liveticker_camera_button:
                dispatchTakePictureIntent(Constants.REQUEST_IMAGE_CAPTURE);
                break;
            case R.id.fragment_liveticker_send_button:
                createTextEvent();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (imageURI != null) {
            File file = new File(imageURI.getPath());
            file.delete();
            imageURI = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            //Result from Camera Activity with the captured picture
            case Constants.REQUEST_IMAGE_CAPTURE:
                if (resultCode == Activity.RESULT_OK) {
                    imageURI = Uri.parse(data.getDataString());
                    DialogFragment dialogFragment = LivetickerPictureCaptionDialog.newInstance(imageURI);
                    dialogFragment.setTargetFragment(this, Constants.PICTURE_DIALOG_REQUEST_CODE);
                    dialogFragment.show(getFragmentManager(), "dialog");
                    break;
                } else if (data != null) {
                    Exception e = (Exception) data.getSerializableExtra(MaterialCamera.ERROR_EXTRA);
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            case Constants.PICTURE_DIALOG_REQUEST_CODE:
                switch (resultCode) {
                    case Constants.PICTURE_DIALOG_RESULT_CODE_SUCCESS:
                        storeImageToDatabase(imageURI, data.getStringExtra("caption"));
                        break;
                    case Constants.PICTURE_DIALOG_RESULT_CODE_FAILURE:
                        File file = new File(imageURI.getPath());
                        file.delete();
                        imageURI = null;
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_CAMERA_PERMISSION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera(Constants.REQUEST_IMAGE_CAPTURE);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(getContext(), R.string.camera_permission_failure, Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
        }
    }

    /**
     * Store Bitmap to Firebase Storage with unique Name. Then delete local image.
     */
    private void storeImageToDatabase(Uri uri, final String caption) {
        loadingAddingNewEvent(true);
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                        loadingAddingNewEvent(false);
                        createImageEvent(fullSnapshot.getDownloadUrl().toString(), thumbSnapshot.getDownloadUrl().toString(), caption);
                        File file = new File(imageURI.getPath());
                        file.delete();
                        imageURI = null;
                        progressBarImageUpload.setProgress(0);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingAddingNewEvent(false);
                        Toast.makeText(getContext(), R.string.image_upload_failure, Toast.LENGTH_LONG).show();
                        File file = new File(imageURI.getPath());
                        file.delete();
                        imageURI = null;
                        progressBarImageUpload.setProgress(0);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingAddingNewEvent(false);
                Toast.makeText(getContext(), R.string.image_upload_failure, Toast.LENGTH_LONG).show();
                File file = new File(imageURI.getPath());
                file.delete();
                imageURI = null;
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
        Liveticker liveticker = ((LivetickerActivity) getActivity()).getLiveticker();

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
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera(Constants.REQUEST_IMAGE_CAPTURE);
        }
    }

    private void startCamera(int requestCode) {
        //File saveDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File saveDir = new File(Environment.getExternalStorageDirectory(), "Pro4");
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            Toast.makeText(getContext(), R.string.camera_permission_failure, Toast.LENGTH_LONG).show();
            return;
        }

        new MaterialCamera(this).saveDir(saveDir).labelConfirm(R.string.camera_confirm).labelRetry(R.string.camera_retry).stillShot().start(requestCode);
    }
}
