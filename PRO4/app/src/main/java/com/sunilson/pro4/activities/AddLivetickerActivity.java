package com.sunilson.pro4.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddLivetickerActivity extends AppCompatActivity implements View.OnClickListener {

    private ValueEventListener queueListener;
    private DatabaseReference mReference;
    private boolean finished;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private Calendar calendar;
    private ArrayList<DatabaseReference> references = new ArrayList<>();

    @BindView(R.id.add_liveticker_date)
    TextView dateTextView;

    @BindView(R.id.add_liveticker_time)
    TextView timeTextView;

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
        data.put("start", Long.toString(calendar.getTimeInMillis()));
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

        calendar = Calendar.getInstance();
        updateDateTime();

        dateTextView.setOnClickListener(this);
        timeTextView.setOnClickListener(this);
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

    private void loading(boolean loading) {
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
}
