package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sunilson.pro4.R;
import com.sunilson.pro4.adapters.FeedRecyclerViewAdapter;
import com.sunilson.pro4.baseClasses.Liveticker;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class SearchFragment extends BaseFragment {

    private ValueEventListener searchResultListener;
    private DatabaseReference resultReference;
    private FeedRecyclerViewAdapter adapter;
    private boolean started;
    private ImageView closeSearch;
    private EditText searchBar;

    @BindView(R.id.fragment_search_recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.fragment_search_loading_container)
    LinearLayout loadingContainer;

    @BindView(R.id.fragment_search_placeholder)
    TextView placeholder;

    public void startSearch() {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            if (resultReference != null && searchResultListener != null) {
                resultReference.removeEventListener(searchResultListener);
            }

            loading(true);
            final DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("request/" + user.getUid() + "/search").push();
            dRef.setValue(searchBar.getText().toString()).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loading(false);
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    resultReference = FirebaseDatabase.getInstance().getReference("result/" + user.getUid() + "/search/" + dRef.getKey());
                    resultReference.addValueEventListener(searchResultListener);
                }
            });
        }
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeSearchListener();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (resultReference != null && searchResultListener != null) {
            resultReference.removeEventListener(searchResultListener);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        unbinder = ButterKnife.bind(this, view);

        initializeSearchBar();

        closeSearch = (ImageView) getActivity().findViewById(R.id.search_bar_close);
        closeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBar.setText("");
            }
        });

        if (!started) {
            started = true;
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(adapter = new FeedRecyclerViewAdapter(recyclerView, getContext()));
            recyclerView.setNestedScrollingEnabled(false);

        }
        return view;
    }

    public void initializeSearchBar() {
        searchBar = (EditText) getActivity().findViewById(R.id.search_bar);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (searchBar.getText().length() == 0) {
                    closeSearch.setVisibility(View.INVISIBLE);
                } else {
                    closeSearch.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    startSearch();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void initializeSearchListener() {
        searchResultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    if (dataSnapshot.child("state").getValue() != null) {
                        if (dataSnapshot.child("state").getValue().equals("success")) {
                            if (dataSnapshot.child("searchResults").getChildrenCount() == 0) {
                                loading(false);
                                placeholder.setText(getString(R.string.no_search_results));
                                placeholder.setVisibility(View.VISIBLE);
                            } else {
                                placeholder.setVisibility(View.GONE);
                                ArrayList<Liveticker> livetickerData = new ArrayList<>();
                                for (DataSnapshot childSnapshot : dataSnapshot.child("searchResults").getChildren()) {
                                    Liveticker liveticker = childSnapshot.getValue(Liveticker.class);
                                    livetickerData.add(liveticker);
                                }
                                loading(false);
                                adapter.setData(livetickerData);
                            }
                        } else if (dataSnapshot.child("state").getValue().equals("error")) {
                            Toast.makeText(getContext(), R.string.search_error, Toast.LENGTH_SHORT).show();
                            placeholder.setText(getString(R.string.start_search));
                            placeholder.setVisibility(View.VISIBLE);
                            loading(false);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void loading(boolean value) {
        if (value) {
            loadingContainer.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            placeholder.setVisibility(View.GONE);
        } else {
            loadingContainer.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}