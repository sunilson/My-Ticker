package com.sunilson.pro4.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sunilson.pro4.R;
import com.sunilson.pro4.baseClasses.Liveticker;
import com.sunilson.pro4.baseClasses.LivetickerEvent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class LivetickerFragment extends BaseFragment implements View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private DatabaseReference livetickerContentReference;
    private Liveticker liveticker;
    private ChildEventListener livetickerContentListener;
    private FirebaseStorage storage;
    private Uri imageURI;

    @BindView(R.id.fragment_liveticker_camera_button)
    Button cameraButton;

    @BindView(R.id.fragment_liveticker_send_button)
    Button sendButton;

    @BindView(R.id.fragment_liveticker_input)
    EditText textInput;

    public static LivetickerFragment newInstance() {
        LivetickerFragment livetickerFragment = new LivetickerFragment();
        return livetickerFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storage = FirebaseStorage.getInstance();

        //livetickerContentReference = ((BaseActivity)getActivity()).getReference().child(Constants.LIVETICKER_CONTENT_PATH).child(liveticker.getLivetickerID());
        //initializeContentListener();
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
                startActivityForResult(imageIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                try {
                    if (imageURI != null) {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageURI);
                        storeImageToDatabase(bitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * Store Bitmap to Firebase Storage with unique Name. Then delete local image.
     *
     * @param bitmap Image captured from camera
     */
    private void storeImageToDatabase(Bitmap bitmap) {
        String uniqueId = UUID.randomUUID().toString();
        StorageReference imageRef = storage.getReference().child("livetickerImages/" + uniqueId + ".jpg");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pushImageOnQueue(taskSnapshot.getDownloadUrl().toString());
                getActivity().getContentResolver().delete(imageURI, null, null);
                imageURI = null;
            }
        });
    }

    /**
     * Creates a new Image Event and pushes it onto the Event Queue
     *
     * @param downloadURL URL of captured image in Firebase Storage
     */
    private void pushImageOnQueue(String downloadURL) {
        //TODO Image Caption
        //TODO Image important?

        LivetickerEvent event = new LivetickerEvent();
        event.setType("image");
        event.setContent(downloadURL);
        event.setTimestamp(Calendar.getInstance().getTimeInMillis());

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("queue").child("addLivetickerEventQueue").child("tasks").push();
        dRef.setValue(event);
    }

    private void pushTextOnQueue() {
        //TODO Text important?

        LivetickerEvent event = new LivetickerEvent();
        event.setType("text");
        event.setContent(textInput.getText().toString());
        event.setTimestamp(Calendar.getInstance().getTimeInMillis());

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference().child("queue").child("addLivetickerEventQueue").child("tasks").push();
        dRef.setValue(event);
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
