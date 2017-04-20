package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.R;
import com.sunilson.pro4.interfaces.CanChangeFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class LoginFragment extends BaseFragment implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @BindView(R.id.loginFragment_email_layout)
    TextInputLayout emailLayout;

    @BindView(R.id.loginFragment_password_layout)
    TextInputLayout passwordLayout;

    @BindView(R.id.loginFragment_email)
    EditText emailEditText;

    @BindView(R.id.loginFragment_password)
    EditText passwordEditText;

    @BindView(R.id.loginFragment_Submit)
    Button loginButton;

    @BindView(R.id.loginFragment_register)
    Button registerButton;

    /**
     * Create new Login Fragment
     *
     * @return Instance of LoginFragment
     */
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        unbinder = ButterKnife.bind(this, view);
        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        initializeAuthListener();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginFragment_Submit:
                emailLogin(emailEditText.getText().toString(), passwordEditText.getText().toString());
                break;
            case R.id.loginFragment_register:
                ((CanChangeFragment)getActivity()).replaceFragment(RegisterFragment.newInstance(), "register");
                break;
        }
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

    private void initializeAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    if (!user.isAnonymous()) {
                        getActivity().finish();
                    }
                }
            }
        };
    }

    private void emailLogin(String email, String password) {
        //TODO Passwort und Email prüfen

        if (email.isEmpty()) {
            emailLayout.setError("Email can't be empty");
        } else if (password.isEmpty()) {
            passwordLayout.setError("Password can't be empty!");
        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
