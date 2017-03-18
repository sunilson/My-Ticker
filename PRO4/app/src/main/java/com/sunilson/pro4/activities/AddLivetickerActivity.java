package com.sunilson.pro4.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sunilson.pro4.R;
import com.sunilson.pro4.utilities.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddLivetickerActivity extends AppCompatActivity {

    private ValueEventListener queueListener;
    private DatabaseReference mReference;
    private boolean finished;
    private ArrayList<DatabaseReference> references = new ArrayList<>();

    @BindView(R.id.add_liveticker_title_edittext)
    EditText titleEditText;

    @BindView(R.id.add_liveticker_description_edittext)
    EditText descriptionEditText;

    @BindView(R.id.add_liveticker_loading_spinner)
    ProgressBar progressBar;

    @OnClick(R.id.add_liveticker_submit_button)
    public void submit(View view) {
        loading(true);

        Map<String, String> data = new HashMap<>();

        data.put("title", titleEditText.getText().toString());
        data.put("description", descriptionEditText.getText().toString());
        data.put("authorID", FirebaseAuth.getInstance().getCurrentUser().getUid());
        data.put("privacy", "public");

        final DatabaseReference ref = mReference.child("queue").child("livetickerQueue").child("tasks").push();
        ref.setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                DatabaseReference taskRef = mReference.child("queue").child("livetickerQueue").child("tasks").child(ref.getKey());
                references.add(taskRef);
                taskRef.addValueEventListener(queueListener);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_liveticker);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mReference = FirebaseDatabase.getInstance().getReference();
        initializeQueueListener();
    }

    @Override
    protected void onStop() {
        super.onStop();

        for (DatabaseReference ref : references) {
            if (queueListener != null) {
                ref.removeEventListener(queueListener);
                Log.i(Constants.LOGGING_TAG, "Removed Listener!");
            }
        }
    }

    private void initializeQueueListener() {
        queueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!finished) {
                    if (dataSnapshot.child("_state").getValue() != null) {
                        if (dataSnapshot.child("_state").getValue().toString().equals("livetickerAdded")) {
                            finished = true;
                            finish();
                        } else if (dataSnapshot.child("_state").getValue().toString().equals("error")) {
                            loading(false);
                            Toast.makeText(AddLivetickerActivity.this, dataSnapshot.child("_error_details").child("error").getValue().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void loading(boolean loading){
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            descriptionEditText.setVisibility(View.GONE);
            titleEditText.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            descriptionEditText.setVisibility(View.VISIBLE);
            titleEditText.setVisibility(View.VISIBLE);
        }
    }
}
