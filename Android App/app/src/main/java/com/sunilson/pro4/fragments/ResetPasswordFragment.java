package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.sunilson.pro4.R;
import com.sunilson.pro4.utilities.Utilities;
import com.sunilson.pro4.views.SubmitButtonBig;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Linus Weiss
 */

public class ResetPasswordFragment extends BaseFragment {

    @BindView(R.id.fragment_reset_password_email)
    EditText email;

    @BindView(R.id.submit_button_view)
    SubmitButtonBig submitButton;

    @OnClick(R.id.submit_button)
    public void reset() {
        submitButton.loading(true);
        String emailString = email.getText().toString();
        if (!emailString.isEmpty() && Utilities.isValidEmail(emailString)) {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getContext(), R.string.reset_email_sent, Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), R.string.reset_email_failure, Toast.LENGTH_SHORT).show();
                    submitButton.loading(false);
                }
            });
        } else {
            Toast.makeText(getContext(), R.string.email_invalid, Toast.LENGTH_SHORT).show();
            submitButton.loading(false);
        }
    }

    public static ResetPasswordFragment newInstance() {
        return new ResetPasswordFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);
        unbinder = ButterKnife.bind(this, view);
        submitButton.setText(getString(R.string.send), getString(R.string.sending));
        return view;
    }



}
