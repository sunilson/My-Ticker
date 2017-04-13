package com.sunilson.pro4.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
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
import android.widget.Toast;

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
import com.sunilson.pro4.activities.MainActivity;
import com.sunilson.pro4.baseClasses.User;
import com.sunilson.pro4.dialogFragments.LivetickerPicktureCropDialog;
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

    private DatabaseReference userReference, resultReference;
    private ValueEventListener loadUserDataListener, resultListener;
    private FirebaseStorage storage;
    private FirebaseUser user;
    private Uri cameraURI, cropURI;
    private User tempUser;
    private String currentType;
    private boolean started;

    @BindView(R.id.progress_overlay_progressbar)
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
        initializeResultListener();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (resultReference != null) {
            resultReference.addValueEventListener(resultListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (resultReference != null && resultListener != null) {
            resultReference.removeEventListener(resultListener);
        }
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

    private void initializeResultListener() {
        resultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("state").getValue() != null) {
                    if (dataSnapshot.child("state").getValue().toString().equals("success")) {
                        Intent i = new Intent(getActivity(), MainActivity.class);
                        startActivity(i);
                    } else if (dataSnapshot.child("state").getValue().toString().equals("error")) {
                        loading(false);
                        Toast.makeText(getActivity(), dataSnapshot.child("errorDetails").getValue().toString(), Toast.LENGTH_LONG).show();
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
                        dispatchTakePictureIntent(Constants.REQUEST_IMAGE_CAPTURE);
                        break;
                    case Constants.PICK_IMAGE_DIALOG_RESULT_GALLERY:
                        dispatchChooseImageFromGalleryIntent(Constants.REQUEST_IMAGE_GALLERY);
                        break;
                }
                break;
            case Constants.REQUEST_IMAGE_CAPTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Integer x = null;
                    Integer y = null;
                    Boolean fixedAspect = null;

                    if (currentType.equals("profile")) {
                        fixedAspect = true;
                        x = 1;
                        y = 1;
                    } else if (currentType.equals("title")) {
                        fixedAspect = true;
                        x = 16;
                        y = 9;
                    }
                    DialogFragment dialogFragment = LivetickerPicktureCropDialog.newInstance(cameraURI, fixedAspect, x, y);
                    dialogFragment.setTargetFragment(this, Constants.PICTURE_DIALOG_CROP_REQUEST_CODE);
                    dialogFragment.show(getFragmentManager(), "dialog");
                }
                break;
            case Constants.REQUEST_IMAGE_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    Integer x = null;
                    Integer y = null;
                    Boolean fixedAspect = null;

                    if (currentType.equals("profile")) {
                        fixedAspect = true;
                        x = 1;
                        y = 1;
                    } else if (currentType.equals("title")) {
                        fixedAspect = true;
                        x = 16;
                        y = 9;
                    }
                    Uri uri = data.getData();
                    DialogFragment dialogFragment = LivetickerPicktureCropDialog.newInstance(uri, fixedAspect, x, y);
                    dialogFragment.setTargetFragment(this, Constants.PICTURE_DIALOG_CROP_REQUEST_CODE);
                    dialogFragment.show(getFragmentManager(), "dialog");
                }
                break;
            case Constants.PICTURE_DIALOG_CROP_REQUEST_CODE:
                switch (resultCode) {
                    case Constants.PICTURE_DIALOG_CROP_RESULT_CODE_SUCCESS:
                        Bitmap bitmap = data.getExtras().getParcelable("image");
                        storeImageToDatabase(bitmap);

                        if (cameraURI != null) {
                            getActivity().getContentResolver().delete(cameraURI, null, null);
                            cameraURI = null;
                        }
                        break;
                    case Constants.PICTURE_DIALOG__CROP_RESULT_CODE_FAILURE:
                        if (cameraURI != null) {
                            getActivity().getContentResolver().delete(cameraURI, null, null);
                            cameraURI = null;
                        }
                        break;
                }
                break;
        }
    }

    /**
     * Take picture from camera on generated URI
     */
    public void dispatchTakePictureIntent(int requestCode) {
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
     * Starts intent that let's the user pick an Image file from their system
     *
     * @param requestCode Used in ActivityResult to react to the users input
     */
    public void dispatchChooseImageFromGalleryIntent(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    /**
     * Store Bitmap to Firebase Storage with unique Name. Then delete local image.
     */
    private void storeImageToDatabase(final Bitmap bitmap) {
        loading(true);

        if (!Utilities.checkImageForType(bitmap.getWidth(), bitmap.getHeight(), currentType)) {
            Toast.makeText(getContext(), R.string.image_upload_failure_size, Toast.LENGTH_SHORT).show();
            loading(false);
        }

        String uniqueId = UUID.randomUUID().toString();

        //Create references to Storage
        final StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("livetickerImages/" + uniqueId + ".jpg");

        //Get Full Image
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        final byte[] data = byteArrayOutputStream.toByteArray();

        //Generate Thumbnail of Image
        final Bitmap thumbMap = ThumbnailUtils.extractThumbnail(bitmap, 320, 180);
        ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
        thumbMap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream2);

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
                if (currentType.equals("profile")) {
                    tempUser.setProfilePicture(fullSnapshot.getDownloadUrl().toString());
                    profileImage.setImageBitmap(bitmap);
                } else if (currentType.equals("title")) {
                    tempUser.setTitlePicture(fullSnapshot.getDownloadUrl().toString());
                    titleImage.setImageBitmap(bitmap);
                }
                loading(false);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), R.string.image_upload_failure, Toast.LENGTH_SHORT).show();
                loading(false);
            }
        });
    }

    /**
     * Save Updated Channel to Server Queue for further processing
     */
    private void saveChannel() {
        loading(true);
        if (userName.getText() != null) {
            tempUser.setUserName(userName.getText().toString());
        }

        if (info.getText() != null) {
            tempUser.setInfo(info.getText().toString());
        }

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("request/editChannel/" + user.getUid()).push();
        dRef.setValue(tempUser).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), R.string.editing_channel_failure, Toast.LENGTH_LONG).show();
            }
        });
        if (resultReference != null && resultListener != null) {
            resultReference.removeEventListener(resultListener);
        }
        resultReference = FirebaseDatabase.getInstance().getReference("result/editChannel/" + dRef.getKey());
        resultReference.addValueEventListener(resultListener);
    }

    private void getImage(String type) {
        currentType = type;
        DialogFragment dialogFragment = PickImageDialog.newInstance();
        dialogFragment.setTargetFragment(this, Constants.PICK_IMAGE_DIALOG_REQUEST_CODE);
        dialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }

    private void loading(boolean loading) {
        if (loading) {
            Utilities.animateView(progressOverlay, View.VISIBLE, 0.4f, 200);
        } else {
            Utilities.animateView(progressOverlay, View.GONE, 0, 200);
            progressBar.setProgress(0);
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (cameraURI != null) {
            getActivity().getContentResolver().delete(cameraURI, null, null);
            cameraURI = null;
        }
    }
}
