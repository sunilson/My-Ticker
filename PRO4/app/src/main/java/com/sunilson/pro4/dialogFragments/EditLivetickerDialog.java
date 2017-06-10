package com.sunilson.pro4.dialogFragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sunilson.pro4.R;
import com.sunilson.pro4.views.SubmitButtonBig;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Linus Weiss
 */

public class EditLivetickerDialog extends BaseDialog {

    private ValueEventListener resultListener;
    private DatabaseReference resultReference;
    private SubmitButtonBig bigButton;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        initializeResultListener();

        View view = inflater.inflate(R.layout.edit_liveticker_dialog, null);
        final EditText title = (EditText) view.findViewById(R.id.edit_liveticker_dialog_title);
        final EditText description = (EditText) view.findViewById(R.id.edit_liveticker_dialog_description);
        final EditText status = (EditText) view.findViewById(R.id.edit_liveticker_dialog_status);
        final String[] values = getArguments().getStringArray("values");
        Button button = (Button) view.findViewById(R.id.submit_button);
        bigButton = (SubmitButtonBig) view.findViewById(R.id.submit_button_view);

        bigButton.setText(getString(R.string.save), getString(R.string.loading));

        title.setText(values[0]);
        description.setText(values[1]);
        status.setText(values[2]);

        builder.setView(view);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bigButton.loading(true);
                final DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("request/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/editLiveticker/").push();
                Map<String, String> map = new HashMap<>();
                map.put("livetickerID", getArguments().getString("livetickerID"));
                map.put("title", title.getText().toString());
                map.put("description", description.getText().toString());
                map.put("status", status.getText().toString());
                dRef.setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        resultReference = FirebaseDatabase.getInstance().getReference("result/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/editLiveticker/" + dRef.getKey());
                        resultReference.addValueEventListener(resultListener);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        bigButton.loading(false);
                        Toast.makeText(activity, R.string.connect_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return builder.create();
    }

    private void initializeResultListener() {
        resultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    if (dataSnapshot.child("state") != null && dataSnapshot.child("state").getValue() != null) {
                        if (dataSnapshot.child("state").getValue().toString().equals("success")) {
                            getDialog().dismiss();
                        } else if (dataSnapshot.child("state").getValue().toString().equals("error")) {
                            Toast.makeText(activity, R.string.connect_failure, Toast.LENGTH_SHORT).show();
                            bigButton.loading(false);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (resultReference != null && resultListener != null) {
            resultReference.removeEventListener(resultListener);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (resultReference != null && resultListener != null) {
            resultReference.removeEventListener(resultListener);
        }
    }

    public static EditLivetickerDialog newInstance(String livetickerID, String title, String description, String status) {
        Bundle args = new Bundle();
        String[] strings = {title, description, status};
        args.putStringArray("values", strings);
        args.putString("livetickerID", livetickerID);
        EditLivetickerDialog dialog = new EditLivetickerDialog();
        dialog.setArguments(args);
        return dialog;
    }
}
