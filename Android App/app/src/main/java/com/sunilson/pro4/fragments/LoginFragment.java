package com.sunilson.pro4.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sunilson.pro4.R;
import com.sunilson.pro4.interfaces.CanChangeFragment;
import com.sunilson.pro4.utilities.Constants;
import com.sunilson.pro4.utilities.Utilities;
import com.sunilson.pro4.views.SubmitButtonBig;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class LoginFragment extends BaseFragment implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;


    @BindView(R.id.loginFragment_email)
    AutoCompleteTextView emailEditText;

    @BindView(R.id.loginFragment_password)
    EditText passwordEditText;

    @BindView(R.id.submit_button)
    Button loginButton;

    @BindView(R.id.submit_button_view)
    SubmitButtonBig loginButtonView;

    @BindView(R.id.loginFragment_google)
    Button googleLogin;

    @BindView(R.id.loginFragment_register)
    TextView registerButton;

    @BindView(R.id.loginFragment_resetPassword)
    TextView resetPassword;

    @BindView(R.id.loginFragment_google_text)
    TextView googleText;

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
        googleLogin.setOnClickListener(this);
        resetPassword.setOnClickListener(this);
        loading(false);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Constants.SHARED_PREF_TAG, Context.MODE_PRIVATE);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                int result = actionId & EditorInfo.IME_MASK_ACTION;
                switch(result) {
                    case EditorInfo.IME_ACTION_DONE:
                        loading(true);
                        emailLogin(emailEditText.getText().toString(), passwordEditText.getText().toString());
                        break;
                }

                return true;
            }
        });

        Set<String> set = sharedPreferences.getStringSet(Constants.SHARED_PREF_KEY_EMAILS, null);
        if (set != null) {
            Log.i(Constants.LOGGING_TAG, set.toString());
            String[] array = set.toArray(new String[set.size()]);
            Log.i(Constants.LOGGING_TAG, array.toString());
            Log.i(Constants.LOGGING_TAG, Integer.toString(array.length));
            Log.i(Constants.LOGGING_TAG, array[0].toString());
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

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit_button:
                loading(true);
                emailLogin(emailEditText.getText().toString(), passwordEditText.getText().toString());
                break;
            case R.id.loginFragment_register:
                ((CanChangeFragment) getActivity()).replaceFragment(RegisterFragment.newInstance(), Constants.FRAGMENT_REGISTER_TAG);
                break;
            case R.id.loginFragment_google:
                googleLogin();
                break;
            case R.id.loginFragment_resetPassword:
                ((CanChangeFragment) getActivity()).replaceFragment(ResetPasswordFragment.newInstance(), Constants.FRAGMENT_RESET_PASSWORD_TAG);
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
                            String token = FirebaseInstanceId.getInstance().getToken();
                            FirebaseDatabase.getInstance().getReference("registrationTokens/" + user.getUid() + "/" + token).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(getContext(), "Logged in - Welcome!", Toast.LENGTH_SHORT).show();
                                    getActivity().finish();
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), R.string.not_verified_yet, Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                            loading(false);
                        }
                    }
                }
            }
        };
    }

    private void googleLogin() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, Constants.GOOGLE_SIGN_IN_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.GOOGLE_SIGN_IN_REQUEST) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                googleText.setText(getString(R.string.signing_in));
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                googleText.setText(getString(R.string.register_button_google));
                Toast.makeText(getContext(), R.string.login_failure, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    googleText.setText(getString(R.string.register_button_google));
                    Toast.makeText(getContext(), R.string.login_failure, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void emailLogin(String email, String password) {
        if (email.isEmpty()) {
            //emailLayout.setError("Email can't be empty");
            loading(false);
        } else if (password.isEmpty()) {
            //passwordLayout.setError("Password can't be empty!");
            loading(false);
        } else {
            Utilities.storeSuggestionEmail(getContext(), email);

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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
