package com.sunilson.pro4.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.sunilson.pro4.BaseApplication;
import com.sunilson.pro4.R;
import com.sunilson.pro4.adapters.CommentsRecyclerViewAdapter;
import com.sunilson.pro4.baseClasses.Comment;
import com.sunilson.pro4.utilities.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Linus Weiss
 */

public class CommentsFragment extends BaseFragment {

    private FirebaseAuth.AuthStateListener authStateListener;
    private String livetickerID;
    private DatabaseReference commentReference, currentResultReference, currentAddCommentResultReference;
    private ValueEventListener commentListener, addCommentResultListener;
    private FirebaseUser user;
    private CommentsRecyclerViewAdapter adapter;
    private EditText commentInput;
    private ImageButton sendButton;
    private RelativeLayout commentInputLoading;

    @BindView(R.id.fragment_comments_placeholder)
    TextView placeholder;

    @BindView(R.id.fragment_comments_recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.fragment_comments_container)
    LinearLayout container;

    @BindView(R.id.fragment_comments_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;


    public static CommentsFragment newInstance(String livetickerID) {
        CommentsFragment commentsFragment = new CommentsFragment();
        Bundle args = new Bundle();
        args.putString("livetickerID", livetickerID);
        commentsFragment.setArguments(args);
        return commentsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        unbinder = ButterKnife.bind(this, view);

        commentInput = (EditText) getActivity().findViewById(R.id.fragment_comments_input);
        commentInputLoading = (RelativeLayout) getActivity().findViewById(R.id.fragment_comments_input_loading_container);
        getActivity().findViewById(R.id.fragment_liveticker_input_layout).setVisibility(View.GONE);
        getActivity().findViewById(R.id.fragment_comments_input_layout).setVisibility(View.VISIBLE);
        sendButton = (ImageButton) getActivity().findViewById(R.id.fragment_comments_send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendComment();
            }
        });

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorAccent, R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestComments();
            }
        });

        getActivity().invalidateOptionsMenu();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        livetickerID = getArguments().getString("livetickerID");
        commentReference = FirebaseDatabase.getInstance().getReference("comments/" + livetickerID);

        initializeAuthListener();
        initializeCommentListener();
        initializeAddCommentListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter = new CommentsRecyclerViewAdapter(recyclerView, getContext()));
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (commentListener != null) {
            commentReference.removeEventListener(commentListener);
        }

        if (addCommentResultListener != null && currentAddCommentResultReference != null) {
            currentAddCommentResultReference.removeEventListener(addCommentResultListener);
        }

        if (authStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().findViewById(R.id.fragment_comments_input_layout).setVisibility(View.GONE);
    }

    private void initializeAddCommentListener() {
        addCommentResultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (dataSnapshot.getChildrenCount() > 0) {
                        Log.i(Constants.LOGGING_TAG, dataSnapshot.getValue().toString());
                        if (dataSnapshot.child("state").getValue().toString().equals("success")) {
                            loadAddingComment(false);
                            commentInput.setText("");
                            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(commentInput.getWindowToken(), 0);
                            requestComments();
                        } else if (dataSnapshot.child("state").getValue().toString().equals("error")) {
                            Toast.makeText(getContext(), R.string.error_adding_comment, Toast.LENGTH_LONG);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void initializeAuthListener() {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    requestComments();
                }
            }
        };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_comments, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.comment_menu_refresh:
                requestComments();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeCommentListener() {
        commentListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    loading(false);
                    if (dataSnapshot.child("comments").getChildrenCount() > 0) {
                        ArrayList<Comment> commentData = new ArrayList<>();
                        for (DataSnapshot childSnapshot : dataSnapshot.child("comments").getChildren()) {
                            Comment comment = childSnapshot.getValue(Comment.class);
                            commentData.add(comment);
                        }
                        adapter.setData(commentData);
                        hasContent(true);
                    } else {
                        hasContent(false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void hasContent(boolean value) {
        if (value) {
            placeholder.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            placeholder.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void loading(boolean loading) {
        if (loading) {
            container.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(true);
        } else {
            container.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void loadAddingComment(boolean loading) {
        if (loading) {
            commentInputLoading.setVisibility(View.VISIBLE);
            commentInput.setEnabled(false);
            sendButton.setEnabled(false);
        } else {
            commentInputLoading.setVisibility(View.GONE);
            commentInput.setEnabled(true);
            sendButton.setEnabled(true);
        }
    }

    private void requestComments() {

        if (!((BaseApplication) getActivity().getApplication()).getInternetConnected()) {
            Toast.makeText(getContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
            return;
        }

        loading(true);

        //Remove current result listener
        if (currentResultReference != null && commentListener != null) {
            currentResultReference.removeEventListener(commentListener);
        }

        Map<String, String> data = new HashMap<>();
        data.put("livetickerID", livetickerID);
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/request/" + user.getUid() + "/comments/").push();
        ref.setValue(data).addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                currentResultReference = FirebaseDatabase.getInstance().getReference("/result/" + user.getUid() + "/comments/" + ref.getKey());
                currentResultReference.addValueEventListener(commentListener);
            }
        }).addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), R.string.fetch_comments_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendComment() {
        String commentText = commentInput.getText().toString().trim();
        if (!commentText.isEmpty()) {
            if (user != null) {
                loadAddingComment(true);
                final DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("request/" + user.getUid() + "/addComment").push();
                Map<String, Object> map = new HashMap<>();
                map.put("authorID", user.getUid());
                map.put("content", commentText);
                map.put("livetickerID", livetickerID);
                dRef.setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (currentAddCommentResultReference != null) {
                            currentAddCommentResultReference.removeEventListener(addCommentResultListener);
                        }
                        currentAddCommentResultReference = FirebaseDatabase.getInstance().getReference("result/" + user.getUid() + "/addComment/" + dRef.getKey());
                        currentAddCommentResultReference.addValueEventListener(addCommentResultListener);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadAddingComment(false);
                        Toast.makeText(getContext(), R.string.comment_post_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
