package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.R;
import com.sunilson.pro4.interfaces.CanChangeFragment;
import com.sunilson.pro4.utilities.Utilities;

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

    @BindView(R.id.registerFragment_password2)
    EditText passwordEditText2;

    @BindView(R.id.submit_button)
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
        registerButton.setOnClickListener(this);
        loading(false);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit_button:
                loading(true);
                createUser(emailEditText.getText().toString(), passwordEditText.getText().toString(), passwordEditText2.getText().toString());
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

        if (password.isEmpty() || password2.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), R.string.fields_empty, Toast.LENGTH_SHORT).show();
            loading(false);
        } else if (!Utilities.isValidEmail(email)) {
            Toast.makeText(getContext(), R.string.email_invalid, Toast.LENGTH_SHORT).show();
            loading(false);
        } else if (!password.equals(password2)) {
            Toast.makeText(getContext(), R.string.passwords_must_match, Toast.LENGTH_SHORT).show();
            loading(false);
        } else {
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);
            FirebaseAuth.getInstance().getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(getContext(), task.getResult().toString(),
                                        Toast.LENGTH_SHORT).show();
                                loading(false);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loading(false);
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void loading(boolean loading) {
        if (loading) {
            //progressOverlay.setVisibility(View.VISIBLE);
        } else {
            //progressOverlay.setVisibility(View.GONE);
        }
    }
}
