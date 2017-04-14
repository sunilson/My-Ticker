package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.R;
import com.sunilson.pro4.interfaces.CanChangeFragment;
import com.sunilson.pro4.utilities.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class RegisterFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.registerFragment_email)
    EditText emailEditText;

    @BindView(R.id.registerFragment_password)
    EditText passwordEditText;

    @BindView(R.id.registerFragment_login)
    Button loginButton;

    @BindView(R.id.registerFragment_submit)
    Button registerButton;

    private String registerUsername = "Default Username";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private AuthCredential credential;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        initializeAuthListener();
    }


    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        unbinder = ButterKnife.bind(this, view);
        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.registerFragment_login:
                ((CanChangeFragment) getActivity()).replaceFragment(LoginFragment.newInstance(), "egal");
                break;
            case R.id.registerFragment_submit:
                createUser(emailEditText.getText().toString(), passwordEditText.getText().toString(), passwordEditText.getText().toString());
                break;
        }
    }


    /**
     * Creates new user from Anonymous user
     *
     * @param email
     * @param password
     * @param password2
     */
    private void createUser(String email, String password, String password2) {

        //TODO Test Username and Password/Email

        if (password.isEmpty()) {

        } else if (password2.isEmpty()) {

        } else if (!password.equals(password2)) {

        } else {
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);
            FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(Constants.LOGGING_TAG, "linkWithCredential:onComplete:" + task.isSuccessful());
                            if (!task.isSuccessful()) {
                                Toast.makeText(getContext(), task.getResult().toString(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void initializeAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (!user.isAnonymous()) {
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getContext(), R.string.verification_email_sent, Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                            }
                        });
                    } else {
                        ((CanChangeFragment) getActivity()).replaceFragment(LoginFragment.newInstance(), "egal");
                    }
                } else {
                    ((CanChangeFragment) getActivity()).replaceFragment(LoginFragment.newInstance(), "egal");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
