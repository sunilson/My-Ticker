package com.sunilson.pro4.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sunilson.pro4.R;
import com.sunilson.pro4.baseClasses.Liveticker;
import com.sunilson.pro4.exceptions.LivetickerSetException;
import com.sunilson.pro4.utilities.Utilities;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddLivetickerActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private ValueEventListener resultListener;
    private DatabaseReference mReference, currentResultReference;
    private boolean finished, startNow;
    private String privacy = "public";
    private Calendar calendar;
    private ArrayList<DatabaseReference> references = new ArrayList<>();
    private CompoundButton.OnCheckedChangeListener switchListener;

    @BindView(R.id.add_liveticker_date)
    TextView dateTextView;

    @BindView(R.id.add_liveticker_time)
    TextView timeTextView;

    @BindView(R.id.add_liveticker_title_edittext)
    EditText titleEditText;

    @BindView(R.id.add_liveticker_description_edittext)
    EditText descriptionEditText;

    @BindView(R.id.add_liveticker_status_edittext)
    EditText statusEditText;

    @BindView(R.id.add_liveticker_loading_spinner)
    ProgressBar progressBar;

    @BindView(R.id.add_liveticker_start_switch)
    Switch dateSwitch;

    @BindView(R.id.add_liveticker_privacy_switch)
    Switch privacySwitch;

    @BindView(R.id.add_liveticker_date_layout)
    LinearLayout dateLayout;

    @BindView(R.id.progress_overlay)
    FrameLayout progressOverlay;

    @OnClick(R.id.add_liveticker_submit_button)
    public void submit(View view) {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || user.isAnonymous()) {
            Toast.makeText(this, R.string.add_liveticker_user_failure, Toast.LENGTH_SHORT).show();
            return;
        }
        
        final Liveticker liveticker = new Liveticker();

        try {
            liveticker.setTitle(titleEditText.getText().toString());
            liveticker.setDescription(descriptionEditText.getText().toString());
            liveticker.setAuthorID(user.getUid());
            liveticker.setStartDate(calendar.getTimeInMillis());
            liveticker.setPrivacy(privacy);
            liveticker.setStatus(statusEditText.getText().toString());
        } catch (LivetickerSetException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        loading(true);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/request/" + user.getUid() + "/addLiveticker/").push();
        ref.setValue(liveticker).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Remove Event Listener from Queue, if it has been started
                if (currentResultReference != null && resultListener != null) {
                    currentResultReference.removeEventListener(resultListener);
                }
                //Listen for results from Queue
                DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("/result/" + user.getUid() + "/addLiveticker/" + ref.getKey());

                //Add Listener to Reference and store Reference so we can later detach Listener
                taskRef.addValueEventListener(resultListener);
                currentResultReference = taskRef;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mReference = FirebaseDatabase.getInstance().getReference();

        initializeQueueListener();

        calendar = Calendar.getInstance();
        updateDateTime();

        dateTextView.setOnClickListener(this);
        timeTextView.setOnClickListener(this);
        dateSwitch.setOnCheckedChangeListener(this);
        privacySwitch.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Remove Event Listener from Queue, if it has been started
        if (currentResultReference != null && resultListener != null) {
            currentResultReference.removeEventListener(resultListener);
        }
    }

    @Override
    protected void authChanged(FirebaseUser user) {
        if (user.isAnonymous()) {
            Intent i = new Intent(AddLivetickerActivity.this, MainActivity.class);
            startActivity(i);
            Toast.makeText(this, R.string.no_access_permission, Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeQueueListener() {
        resultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!finished) {
                    if (dataSnapshot.child("state").getValue() != null) {
                        if (dataSnapshot.child("state").getValue().toString().equals("success")) {
                            finished = true;
                            finish();
                        } else if (dataSnapshot.child("state").getValue().toString().equals("error")) {
                            loading(false);
                            Toast.makeText(AddLivetickerActivity.this, dataSnapshot.child("errorDetails").getValue().toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void loading(boolean loading) {
        if (loading) {
            Utilities.animateView(progressOverlay, View.VISIBLE, 0.4f, 200);
        } else {
            Utilities.animateView(progressOverlay, View.GONE, 0, 200);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_liveticker_date:
                showDateDialog();
                break;
            case R.id.add_liveticker_time:
                showTimeDialog();
                break;
        }
    }

    private void showDateDialog() {
        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateTime();
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() + 432000000);
        datePickerDialog.show();
    }

    private void showTimeDialog() {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                updateDateTime();
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();

    }

    private void updateDateTime() {
        dateTextView.setText(calendar.get(Calendar.DAY_OF_MONTH) + "." + calendar.get(Calendar.MONTH) + "." + calendar.get(Calendar.YEAR));
        timeTextView.setText(calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.add_liveticker_start_switch:
                startNow = !startNow;
                if (b) {
                    dateLayout.setVisibility(View.GONE);
                } else {
                    dateLayout.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.add_liveticker_privacy_switch:
                if (b) {
                    privacy = "public";
                } else {
                    privacy = "private";
                }
                break;
        }
    }

    private void initializeSwitchListener() {
        switchListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        };
    }
}
