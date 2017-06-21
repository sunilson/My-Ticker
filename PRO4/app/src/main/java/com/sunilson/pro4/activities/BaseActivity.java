package com.sunilson.pro4.activities;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sunilson.pro4.R;

import java.util.List;

/**
 * @author Linus Weiss
 */

public abstract class BaseActivity extends AppCompatActivity{

    protected FirebaseAuth mAuth;
    protected FirebaseAuth.AuthStateListener mAuthListener;
    protected FirebaseUser user;
    protected DatabaseReference mReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Replace icon in toolbar in the task manager when Android version is Lollipop or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.icontransparent);
            ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(getString(R.string.app_name), icon, ContextCompat.getColor(this, R.color.colorPrimary));
            setTaskDescription(taskDescription);
        }


        mAuth = FirebaseAuth.getInstance();
        mReference = FirebaseDatabase.getInstance().getReference();

        //Listener for handling Firebase authentication
        initializeAuthListener();
    }

    /**
     * Sign in anonymously when user is not signed in
     */
    protected void signInAnonymously() {
        if (mAuth.getCurrentUser() == null) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //Check if login failed
                            if (!task.isSuccessful()) {
                                Toast.makeText(BaseActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    /**
     * Initialize the Listener that listens for auth changes
     */
    private void initializeAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                //Notify if a user is logged in
                if (user != null) {
                    authChanged(user);
                    if (!user.isAnonymous()) {
                        if (!user.isEmailVerified()) {
                            mAuth.signOut();
                            signInAnonymously();
                            Toast.makeText(BaseActivity.this, R.string.not_verified_yet, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    //If user is not signed in, sign in anonymously
                    signInAnonymously();
                }
            }
        };
    }

    public DatabaseReference getReference() {
        return mReference;
    }

    /**
     * Listen for results from Activites/Fragments
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Notify all currently active fragments that a result has been returned
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    /**
     * Listen for results of permissions dialog
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Notify all currently active fragments that a permission result has been returned
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
            }
        }
    }

    public FirebaseUser getCurrentUser() {
        return user;
    }

    /**
     * Method is called when auth of user changes
     *
     * @param user The new user object
     */
    abstract protected void authChanged(FirebaseUser user);
}