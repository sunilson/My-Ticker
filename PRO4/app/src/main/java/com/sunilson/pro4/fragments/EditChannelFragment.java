package com.sunilson.pro4.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
import com.sunilson.pro4.dialogFragments.LivetickerPicktureCropDialog;
import com.sunilson.pro4.dialogFragments.PickImageDialog;
import com.sunilson.pro4.utilities.Constants;
import com.sunilson.pro4.utilities.Utilities;
import com.sunilson.pro4.views.SubmitButtonBig;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class EditChannelFragment extends BaseFragment implements View.OnClickListener {

    private DatabaseReference userReference, resultReference;
    private ValueEventListener loadUserDataListener, resultListener;
    private User tempUser;
    private String currentType;
    private boolean started, loading;

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

    @BindView(R.id.submit_button)
    Button submitButton;

    @BindView(R.id.submit_button_view)
    SubmitButtonBig submitButtonBig;

    @BindView(R.id.fragment_edit_channel_pick_profile)
    ImageButton getProfileImage;

    @BindView(R.id.fragment_edit_channel_pick_title)
    ImageButton getTitleImage;

    @BindView(R.id.progress_overlay)
    View progressOverlay;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tempUser = new User();
        initializeUserListener();
        initializeResultListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!started) {
            loading(true);
            started = true;
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                userReference = FirebaseDatabase.getInstance().getReference("users/" + user.getUid());
                userReference.addListenerForSingleValueEvent(loadUserDataListener);
            }
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
        submitButton.setOnClickListener(this);
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
                        } else {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(Constants.PROFILE_PICTURE_PLACEHOLDER);
                            Glide.with(getActivity()).using(new FirebaseImageLoader()).load(storageReference).into(profileImage);
                        }

                        if (tempUser.getTitlePicture() != null) {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(tempUser.getTitlePicture());
                            Glide.with(getActivity()).using(new FirebaseImageLoader()).load(storageReference).into(titleImage);
                        } else {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(Constants.PROFILE_PICTURE_PLACEHOLDER);
                            Glide.with(getActivity()).using(new FirebaseImageLoader()).load(storageReference).into(profileImage);
                        }
                    }
                    loading(false);
                }
                loading(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.PICK_IMAGE_DIALOG_REQUEST_CODE:
                switch (resultCode) {
                    case Constants.PICK_IMAGE_DIALOG_RESULT_CAMERA:
                        dispatchTakePictureIntent();
                        break;
                    case Constants.PICK_IMAGE_DIALOG_RESULT_GALLERY:
                        dispatchChooseImageFromGalleryIntent(Constants.REQUEST_IMAGE_GALLERY);
                        break;
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
                        break;
                    case Constants.PICTURE_DIALOG__CROP_RESULT_CODE_FAILURE:
                        //TODO Error Handling
                        loading(false);
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
                    startCamera();
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
     * Take picture from camera on generated URI
     */
    public void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }
    }

    private void startCamera() {

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
        DialogFragment dialogFragment = LivetickerPicktureCropDialog.newInstance(null, fixedAspect, x, y);
        dialogFragment.setTargetFragment(this, Constants.PICTURE_DIALOG_CROP_REQUEST_CODE);
        dialogFragment.show(getFragmentManager(), "dialog");
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

        Bitmap resultBitmap = bitmap;

        if (checkImageToSmall(resultBitmap.getWidth(), resultBitmap.getHeight())) {
            Toast.makeText(getContext(), R.string.image_upload_failure_size, Toast.LENGTH_SHORT).show();
            loading(false);
            return;
        } else if (checkImageTooBig(resultBitmap.getWidth(), resultBitmap.getHeight())) {
            Toast.makeText(getContext(), R.string.image_too_big_resize, Toast.LENGTH_SHORT).show();
            if (currentType.equals(Constants.EDIT_CHANNEL_IMAGE_TYPE_PROFILE)) {
                resultBitmap = resizeProfileImage(resultBitmap);
            } else if (currentType.equals(Constants.EDIT_CHANNEL_IMAGE_TYPE_TITLE)) {
                resultBitmap = resizeTitleImage(resultBitmap);
            }
        }

        String uniqueId = UUID.randomUUID().toString();

        //Create references to Storage
        final StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("livetickerImages/" + uniqueId + ".jpg");

        //Get Full Image
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        resultBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        final byte[] data = byteArrayOutputStream.toByteArray();

        progressBar.setProgress(30);

        final Bitmap uploadBitmap = resultBitmap;
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
                    profileImage.setImageBitmap(uploadBitmap);
                } else if (currentType.equals("title")) {
                    tempUser.setTitlePicture(fullSnapshot.getDownloadUrl().toString());
                    titleImage.setImageBitmap(uploadBitmap);
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

    private boolean checkImageToSmall(int width, int height) {
        if (currentType.equals(Constants.EDIT_CHANNEL_IMAGE_TYPE_PROFILE)) {
            if (width >= Constants.PROFILE_IMAGE_MIN_SIZE && height >= Constants.PROFILE_IMAGE_MIN_SIZE) {
                return false;
            }
            return true;
        } else if (currentType.equals(Constants.EDIT_CHANNEL_IMAGE_TYPE_TITLE)) {
            if (width >= Constants.TITLE_IMAGE_MIN_WIDTH && height >= Constants.TITLE_IMAGE_MIN_HEIGHT) {
                return false;
            }
            return true;
        }

        return true;
    }

    private boolean checkImageTooBig(int width, int height) {
        if (currentType.equals(Constants.EDIT_CHANNEL_IMAGE_TYPE_PROFILE)) {
            if (width > Constants.PROFILE_IMAGE_MAX_SIZE || height > Constants.PROFILE_IMAGE_MAX_SIZE) {
                return true;
            }
            return false;
        } else if (currentType.equals(Constants.EDIT_CHANNEL_IMAGE_TYPE_TITLE)) {
            if (width > Constants.TITLE_IMAGE_MAX_WIDTH || height > Constants.TITLE_IMAGE_MAX_HEIGHT) {
                return true;
            }
            return false;
        }

        return false;
    }

    private Bitmap resizeProfileImage(Bitmap image) {
        return Bitmap.createScaledBitmap(image, Constants.PROFILE_IMAGE_MAX_SIZE, Constants.PROFILE_IMAGE_MAX_SIZE, true);
    }

    private Bitmap resizeTitleImage(Bitmap image) {
        return Bitmap.createScaledBitmap(image, Constants.TITLE_IMAGE_MAX_WIDTH, Constants.TITLE_IMAGE_MAX_HEIGHT, true);
    }

    /**
     * Save Updated Channel to Server Queue for further processing
     */
    private void saveChannel() {
        loadingSubmit(true);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (!userName.getText().toString().isEmpty()) {
            tempUser.setUserName(userName.getText().toString());
        } else  {
            Toast.makeText(getActivity(), R.string.username_empty, Toast.LENGTH_SHORT).show();
            loading(false);
            return;
        }

        if (info.getText() != null) {
            tempUser.setInfo(info.getText().toString());
        }

        if (status.getText() != null) {
            tempUser.setStatus(status.getText().toString());
        }

        if (tempUser.getTitlePicture().isEmpty() || tempUser.getProfilePicture().isEmpty()) {
            Toast.makeText(getActivity(), R.string.profile_pictures_invalid, Toast.LENGTH_LONG).show();
            loading(false);
            return;
        }

        tempUser.setUserID(user.getUid());

        final DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("/request/" + user.getUid() + "/editChannel/" ).push();
        dRef.setValue(tempUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (resultReference != null) {
                    resultReference.removeEventListener(resultListener);
                }
                resultReference = FirebaseDatabase.getInstance().getReference("result/" + user.getUid() + "/editChannel/" + dRef.getKey());
                resultReference.addValueEventListener(resultListener);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), R.string.editing_channel_failure, Toast.LENGTH_LONG).show();
                loadingSubmit(false);
            }
        });

    }

    private void getImage(String type) {
        currentType = type;
        DialogFragment dialogFragment = PickImageDialog.newInstance();
        dialogFragment.setTargetFragment(this, Constants.PICK_IMAGE_DIALOG_REQUEST_CODE);
        dialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
    }

    private void loadingSubmit(boolean loading) {
        this.loading = true;

        if (loading) {
            getProfileImage.setEnabled(false);
            getTitleImage.setEnabled(false);
            submitButtonBig.loading(true);
        } else {
            submitButtonBig.loading(false);
            getProfileImage.setEnabled(true);
            getTitleImage.setEnabled(true);
        }
    }

    private void loading(boolean loading) {
        this.loading = loading;
        if (loading) {
            Utilities.animateView(progressOverlay, View.VISIBLE, 0.4f, 200);
        } else {
            Utilities.animateView(progressOverlay, View.GONE, 0, 200);
            progressBar.setProgress(0);
        }
    }

    @Override
    public void onClick(View view) {
        if (!this.loading) {
            switch (view.getId()) {
                case R.id.submit_button:
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

    private void initializeResultListener() {
        resultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("state").getValue() != null) {
                    if (dataSnapshot.child("state").getValue().toString().equals("success")) {
                        loading(false);
                        getActivity().finish();
                    } else if (dataSnapshot.child("state").getValue().toString().equals("error")) {
                        loading(false);
                        Toast.makeText(getContext(), dataSnapshot.child("errorDetails").getValue().toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }
}
