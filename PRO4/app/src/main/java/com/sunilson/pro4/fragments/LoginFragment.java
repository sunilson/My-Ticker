package com.sunilson.pro4.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sunilson.pro4.R;
import com.sunilson.pro4.interfaces.CanChangeFragment;
import com.sunilson.pro4.utilities.Constants;
import com.sunilson.pro4.views.SubmitButtonBig;

import java.util.HashSet;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class LoginFragment extends BaseFragment implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @BindView(R.id.loginFragment_email)
    AutoCompleteTextView emailEditText;

    @BindView(R.id.loginFragment_password)
    EditText passwordEditText;

    @BindView(R.id.submit_button)
    Button loginButton;

    @BindView(R.id.submit_button_view)
    SubmitButtonBig loginButtonView;

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
        loginButtonView.setText(getString(R.string.login), getString(R.string.logging_in));
        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        loading(false);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Constants.SHARED_PREF_TAG, Context.MODE_PRIVATE);

        Set<String> set = sharedPreferences.getStringSet(Constants.SHARED_PREF_KEY_EMAILS, null);
        if (set != null) {
            String[] array = set.toArray(new String[set.size()]);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, array);
            emailEditText.setAdapter(adapter);
        }

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
            case R.id.submit_button:
                loading(true);
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
                        if (user.isEmailVerified()) {
                            DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("registrationTokens/" + user.getUid()).push();
                            dRef.setValue(FirebaseInstanceId.getInstance().getToken()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    getActivity().finish();
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), R.string.not_verified_yet, Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                    }
                }
            }
        };
    }

    private void emailLogin(String email, String password) {
        //TODO Passwort und Email prüfen

        if (email.isEmpty()) {
            //emailLayout.setError("Email can't be empty");
            loading(false);
        } else if (password.isEmpty()) {
            //passwordLayout.setError("Password can't be empty!");
            loading(false);
        } else {

            SharedPreferences sharedPreferences = getContext().getSharedPreferences(Constants.SHARED_PREF_TAG, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            Set<String> set = sharedPreferences.getStringSet(Constants.SHARED_PREF_KEY_EMAILS, null);
            if (set == null) {
                set = new HashSet<>();
            }

            set.add(email);
            editor.putStringSet(Constants.SHARED_PREF_KEY_EMAILS, set).commit();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Login failed", Toast.LENGTH_SHORT).show();
                        loading(false);
                    }
                }
            });
        }
    }

    private void loading(boolean loading) {
        if (loading) {
            loginButtonView.loading(true);
        } else {
            loginButtonView.loading(false);
        }
    }
}
