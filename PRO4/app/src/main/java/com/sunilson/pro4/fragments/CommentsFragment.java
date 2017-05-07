package com.sunilson.pro4.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.sunilson.pro4.adapters.CommentsRecyclerViewAdapter;
import com.sunilson.pro4.baseClasses.Comment;
import com.sunilson.pro4.utilities.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

    @BindView(R.id.fragment_comments_placeholder)
    TextView placeholder;

    @BindView(R.id.fragment_comments_recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.fragment_comments_input)
    EditText commentInput;

    @BindView(R.id.fragment_comments_loading_container)
    LinearLayout loadingContainer;

    @BindView(R.id.fragment_comments_container)
    LinearLayout container;

    @OnClick(R.id.fragment_comments_send_button)
    public void sendComment() {
        String commentText = commentInput.getText().toString().trim();
        if (!commentText.isEmpty()) {
            if (user != null && user.isEmailVerified()) {
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
                        Toast.makeText(getContext(), R.string.comment_post_failure, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

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
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        livetickerID = getArguments().getString("livetickerID");
        commentReference = FirebaseDatabase.getInstance().getReference("comments/" + livetickerID);

        initializeAuthListener();
        initializeCommentListener();
        initializeAddCommentListener();
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter = new CommentsRecyclerViewAdapter(recyclerView, getContext()));
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (commentListener != null) {
            commentReference.removeEventListener(commentListener);
        }
        if (authStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);

        }
    }

    private void initializeAddCommentListener() {
        addCommentResultListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    if (dataSnapshot.getChildrenCount() > 0) {
                        Log.i(Constants.LOGGING_TAG, dataSnapshot.getValue().toString());
                        if (dataSnapshot.child("state").getValue().toString().equals("success")) {
                            commentInput.setText("");
                            requestComments();
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
                if (user != null && user.isEmailVerified()) {
                    requestComments();
                }
            }
        };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.feed_menu_refresh:
                //requestFeed();
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
                    if(dataSnapshot.child("comments").getChildrenCount() > 0) {
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
            loadingContainer.setVisibility(View.VISIBLE);
        } else {
            container.setVisibility(View.VISIBLE);
            loadingContainer.setVisibility(View.GONE);
        }
    }

    private void requestComments() {

        loading(true);

        //Remove current result listener
        if (currentResultReference != null && commentListener != null) {
            currentResultReference.removeEventListener(commentListener);
        }

        Map<String, String> data = new HashMap<>();
        data.put("livetickerID", livetickerID);
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/request/" + user.getUid() + "/comments/").push();
        ref.setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                currentResultReference = FirebaseDatabase.getInstance().getReference("/result/" + user.getUid() + "/comments/" + ref.getKey());
                currentResultReference.addValueEventListener(commentListener);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //TODO Error Handling
            }
        });
    }
}
