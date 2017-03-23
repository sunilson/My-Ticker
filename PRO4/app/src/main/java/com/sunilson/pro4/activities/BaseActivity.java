package com.sunilson.pro4.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.utilities.Constants;

/**
 * Created by linus_000 on 17.03.2017.
 */

public abstract class BaseActivity extends AppCompatActivity{

    protected FirebaseAuth mAuth;
    protected FirebaseAuth.AuthStateListener mAuthListener;
    protected FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        //Listener for handling Firebase authentication
        initializeAuthListener();
    }

    protected void signInAnonymously() {
        if (mAuth.getCurrentUser() == null) {
            mAuth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(Constants.LOGGING_TAG, "signInAnonymously:onComplete:" + task.isSuccessful());
                            if (!task.isSuccessful()) {
                                Log.w(Constants.LOGGING_TAG, "signInAnonymously", task.getException());
                                Toast.makeText(BaseActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    abstract void initializeAuthListener();
}