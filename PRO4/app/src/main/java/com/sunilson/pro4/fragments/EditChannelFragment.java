package com.sunilson.pro4.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
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
import com.sunilson.pro4.baseClasses.User;
import com.sunilson.pro4.dialogFragments.PickImageDialog;
import com.sunilson.pro4.utilities.Constants;
import com.sunilson.pro4.utilities.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class EditChannelFragment extends BaseFragment implements View.OnClickListener {

    private DatabaseReference userReference;
    private ValueEventListener loadUserDataListener;
    private FirebaseStorage storage;
    private FirebaseUser user;
    private Uri cameraURI, galleryURI;
    private User tempUser;
    private boolean started;

    @BindView(R.id.fragment_edit_channel_progressBar_image_upload)
    ProgressBar progressBar;

    @BindView(R.id.fragment_edit_channel_username)
    EditText userName;

    @BindView(R.id.fragment_edit_channel_status)
    EditText status;

    @BindView(R.id.fragment_edit_channel_info)
    EditText info;

    @BindView(R.id.fragment_edit_channel_profile)
    ImageView profileImage;

    @BindView(R.id.fragment_edit_channel_title)
    ImageView titleImage;

    @BindView(R.id.fragment_edit_channel_save)
    Button save;

    @BindView(R.id.fragment_edit_channel_pick_profile)
    Button getProfileImage;

    @BindView(R.id.fragment_edit_channel_pick_title)
    Button getTitleImage;

    @BindView(R.id.progress_overlay)
    View progressOverlay;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tempUser = new User();
        storage = FirebaseStorage.getInstance();
        initializeUserListener();
    }

    public static EditChannelFragment newInstance() {
        return new EditChannelFragment();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_channel, container, false);
        unbinder = ButterKnife.bind(this, view);
        save.setOnClickListener(this);
        getProfileImage.setOnClickListener(this);
        getTitleImage.setOnClickListener(this);
        return view;
    }

    private void initializeUserListener() {
        loadUserDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(User.class) != null) {
                    tempUser = dataSnapshot.getValue(User.class);
                    if (tempUser != null) {
                        if (tempUser.getUserName() != null) {
                            userName.setText(tempUser.getUserName());
                        }

                        if (tempUser.getInfo() != null) {
                            info.setText(tempUser.getInfo());
                        }

                        if (tempUser.getStatus() != null) {
                            status.setText(tempUser.getStatus());
                        }

                        if (tempUser.getProfilePicture() != null) {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(tempUser.getProfilePicture());
                            Glide.with(getActivity()).using(new FirebaseImageLoader()).load(storageReference).into(profileImage);
                        }

                        if (tempUser.getTitlePicture() != null) {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(tempUser.getProfilePicture());
                            Glide.with(getActivity()).using(new FirebaseImageLoader()).load(storageReference).into(titleImage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }


    public void loadUserData(FirebaseUser user) {
        this.user = user;

        if (!started) {
            started = true;
            userReference = FirebaseDatabase.getInstance().getReference("users/" + user.getUid());
            userReference.addListenerForSingleValueEvent(loadUserDataListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.PICK_IMAGE_DIALOG_REQUEST_CODE:
                switch (resultCode) {
                    case Constants.PICK_IMAGE_DIALOG_RESULT_CAMERA:
                        switch (data.getStringExtra("type")) {
                            case "profile":
                                dispatchTakePictureIntent(Constants.REQUEST_IMAGE_CAPTURE_PROFILE, data.getStringExtra("type"));
                                break;
                            case "title":
                                dispatchTakePictureIntent(Constants.REQUEST_IMAGE_CAPTURE_TITLE, data.getStringExtra("type"));
                                break;
                        }

                        break;
                    case Constants.PICK_IMAGE_DIALOG_RESULT_GALLERY:
                        break;
                }
                break;
            case Constants.REQUEST_IMAGE_CAPTURE_PROFILE:
                if (cameraURI != null) {
                    storeImageToDatabase(cameraURI, "profile");
                    cameraURI = null;
                }
                break;
            case Constants.REQUEST_IMAGE_CAPTURE_TITLE:
                if (cameraURI != null) {
                    storeImageToDatabase(cameraURI, "title");
                    cameraURI = null;
                }
                break;
        }
    }

    /**
     * Take picture from camera on generated URI
     */
    public void dispatchTakePictureIntent(int requestCode, String type) {
        Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (imageIntent.resolveActivity(getContext().getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = Utilities.createImageFile(getContext());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (photoFile != null) {
                //Generate URI, so image can later be accessed again
                cameraURI = FileProvider.getUriForFile(getContext(), "com.sunilson.pro4.fileprovider", photoFile);

                //Start Camera activity
                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraURI);
                startActivityForResult(imageIntent, requestCode);
            }
        }
    }

    /**
     * Store Bitmap to Firebase Storage with unique Name. Then delete local image.
     */
    private void storeImageToDatabase(final Uri uri, final String type) {
        loading(true);

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        progressBar.setVisibility(View.VISIBLE);

        String uniqueId = UUID.randomUUID().toString();

        //Create references to Storage
        final StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("livetickerImages/" + uniqueId + ".jpg");
        final StorageReference thumbRef = storage.getReference().child("livetickerImages/" + uniqueId + "_thumb.jpg");

        //Get Full Image
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        final byte[] data = byteArrayOutputStream.toByteArray();

        progressBar.setProgress(30);

        final Bitmap finalBitmap = bitmap;
        imageRef.putBytes(data).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                Double d = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressBar.setProgress(d.intValue() + 30);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot fullSnapshot) {
                if (type.equals("profile")) {
                    tempUser.setProfilePicture(fullSnapshot.getDownloadUrl().toString());
                    profileImage.setImageBitmap(finalBitmap);
                } else if (type.equals("title")) {
                    tempUser.setTitlePicture(fullSnapshot.getDownloadUrl().toString());
                    titleImage.setImageBitmap(finalBitmap);
                }
                getActivity().getContentResolver().delete(uri, null, null);
                loading(false);
                progressBar.setVisibility(View.GONE);
                progressBar.setProgress(0);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //TODO Failure Handeln
            }
        });
    }

    private void saveChannel() {
        if (userName.getText() != null) {
            tempUser.setUserName(userName.getText().toString());
        }

        if (info.getText() != null) {
            tempUser.setInfo(info.getText().toString());
        }

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("request/editChannel/" + user.getUid()).push();
        dRef.setValue(tempUser);
    }

    private void getImage(String type) {
        DialogFragment dialogFragment = PickImageDialog.newInstance(type);
        dialogFragment.setTargetFragment(this, Constants.PICK_IMAGE_DIALOG_REQUEST_CODE);
        dialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }

    private void loading(boolean loading) {
        if (loading) {
            Utilities.animateView(progressOverlay,View.VISIBLE, 0.4f, 200);
        } else {
            Utilities.animateView(progressOverlay,View.GONE, 0, 200);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_edit_channel_save:
                saveChannel();
                break;
            case R.id.fragment_edit_channel_pick_profile:
                getImage("profile");
                break;
            case R.id.fragment_edit_channel_pick_title:
                getImage("title");
                break;
        }
    }
}
