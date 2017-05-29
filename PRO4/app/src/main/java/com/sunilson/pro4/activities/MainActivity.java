package com.sunilson.pro4.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sunilson.pro4.R;
import com.sunilson.pro4.adapters.MainActivityFragmentPagerAdapter;
import com.sunilson.pro4.dialogFragments.RegisterDialog;
import com.sunilson.pro4.fragments.FeedBaseFragment;
import com.sunilson.pro4.interfaces.FragmentAuthInterface;
import com.sunilson.pro4.utilities.Constants;
import com.sunilson.pro4.utilities.Utilities;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.search_bar_layout)
    RelativeLayout searchBarLayout;

    @BindView(R.id.feed_bar)
    LinearLayout feedBarLayout;

    @BindView(R.id.main_bar_author_image)
    ImageView authorImage;

    @BindView(R.id.main_bar_title)
    TextView title;

    @BindView(R.id.main_bar_author_name)
    TextView authorName;

    @BindView(R.id.main_liveticker_bar)
    LinearLayout livetickerBar;

    @BindView(R.id.main_liveticker_bar_shadow)
    FrameLayout livetickerBarShadow;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.fab2)
    FloatingActionButton fab2;

    @BindView(R.id.main_liveticker_bar_close)
    ImageView closeLivetickerBar;

    @BindView(R.id.main_liveticker_bar_title)
    TextView livetickerTitle;

    @BindView(R.id.main_liveticker_bar_button)
    Button livetickerBarButton;

    @BindView(R.id.main_liveticker_bar_status)
    ImageView livetickerBarStatus;

    private MenuItem loginButton, logoutButton;

    private ViewPager viewPager;
    private ValueEventListener userListener, livetickerListener;
    private DatabaseReference currentUserReference, livetickerReference;
    private TabLayout tabLayout;
    private MainActivityFragmentPagerAdapter adapter;
    private Animation enterAnimation, exitAnimation, scaleEnterAnimation, scaleOutAnimation;
    private String livetickerID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            checkIntent(getIntent());
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //ButterKnife Binding
        ButterKnife.bind(this);

        //Set up the Viewpager and the Tab Layout, which are used to switch between the 3 Fragments of the Main Activity
        viewPager = (ViewPager) findViewById(R.id.mainActivityViewPager);
        tabLayout = (TabLayout) findViewById(R.id.mainActivityTabLayout);
        viewPager.setAdapter(adapter = new MainActivityFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this));
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);

        //Listener to change title on fragment swap
        final ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                invalidateOptionsMenu();
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };
        viewPager.addOnPageChangeListener(onPageChangeListener);

        //Ensure correct title is set on startup
        viewPager.post(new Runnable() {
            @Override
            public void run() {
                onPageChangeListener.onPageSelected(viewPager.getCurrentItem());
            }
        });

        initializeUserListener();

        enterAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_from_bottom);
        exitAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_to_bottom);
        exitAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        scaleEnterAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_in);
        scaleOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_out);
        closeLivetickerBar.setOnClickListener(this);
        fab.setOnClickListener(this);
        fab2.setOnClickListener(this);
        livetickerBarButton.setOnClickListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntent(intent);
    }

    private void checkIntent(Intent intent) {
        //If started from notification
        if (intent.getStringExtra("type") != null && intent.getStringExtra("type").equals(Constants.INTENT_TYPE_NOTIFICATION)) {
            Intent i = new Intent(this, LivetickerActivity.class);
            i.putExtra("livetickerID", intent.getStringExtra("livetickerID"));
            startActivityForResult(i, Constants.LIVETICKER_ACTIVITY_REQUEST);
        } else {
            //If started from URL
            Uri data = intent.getData();
            if (data != null) {
                List<String> params = data.getPathSegments();
                if (params != null) {
                    if (params.get(1) != null && !params.get(1).isEmpty()) {
                        String id = params.get(1);
                        Intent i = new Intent(this, LivetickerActivity.class);
                        i.putExtra("livetickerID", id);
                        startActivityForResult(i, Constants.LIVETICKER_ACTIVITY_REQUEST);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (viewPager.getCurrentItem() == 0) {
            menu.findItem(R.id.feed_menu_refresh).setVisible(true);
            searchBarLayout.setVisibility(View.GONE);
            feedBarLayout.setVisibility(View.VISIBLE);
        } else if (viewPager.getCurrentItem() == 1) {
            menu.findItem(R.id.feed_menu_refresh).setVisible(true);
            searchBarLayout.setVisibility(View.GONE);
            feedBarLayout.setVisibility(View.VISIBLE);
        } else if (viewPager.getCurrentItem() == 2) {
            menu.findItem(R.id.feed_menu_refresh).setVisible(false);
            searchBarLayout.setVisibility(View.VISIBLE);
            feedBarLayout.setVisibility(View.GONE);
        } else if (viewPager.getCurrentItem() == 3) {
            menu.findItem(R.id.feed_menu_refresh).setVisible(false);
            searchBarLayout.setVisibility(View.GONE);
            feedBarLayout.setVisibility(View.VISIBLE);
        }

        loginButton = menu.findItem(R.id.main_menu_login);
        logoutButton = menu.findItem(R.id.main_menu_logOut);
        checkLoginStatus(FirebaseAuth.getInstance().getCurrentUser());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.main_menu_logOut) {
            mAuth.signOut();
            return true;
        } else if (id == R.id.feed_menu_refresh) {
            return false;
        } else if (id == R.id.main_menu_login) {
            Intent i = new Intent(this, AuthenticationActivity.class);
            startActivity(i);
        }

        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (currentUserReference != null) {
            currentUserReference.removeEventListener(userListener);
        }
    }

    @Override
    protected void authChanged(FirebaseUser user) {
        checkLoginStatus(user);
        if (user != null) {


            if (currentUserReference != null) {
                currentUserReference.removeEventListener(userListener);
            }

            currentUserReference = FirebaseDatabase.getInstance().getReference("users/" + user.getUid());
            currentUserReference.addValueEventListener(userListener);

            SparseArray<Fragment> sparseArray = adapter.getRegisteredFragments();
            for (int i = 0; i < sparseArray.size(); i++) {
                int key = sparseArray.keyAt(i);
                // get the object by the key.
                Object obj = sparseArray.get(key);

                if (obj instanceof FragmentAuthInterface) {
                    ((FragmentAuthInterface) obj).authChanged(user);
                }
            }

            if (!user.isAnonymous() && user.isEmailVerified()) {
                //Check for first login
                final DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("firstLogin/" + user.getUid());
                dRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null) {
                            dRef.setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Intent i = new Intent(MainActivity.this, ChannelActivity.class);
                                    i.putExtra("type", "firstLogin");
                                    startActivity(i);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private void checkLoginStatus(FirebaseUser user) {
        if (user != null) {
            if (user.isAnonymous()) {
                if (loginButton != null && logoutButton != null) {
                    loginButton.setVisible(true);
                    logoutButton.setVisible(false);
                }
            } else {
                if (loginButton != null && logoutButton != null) {
                    loginButton.setVisible(false);
                    logoutButton.setVisible(true);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.ADD_LIVETICKER_REQUEST_CODE:
                if (resultCode == Constants.ADD_LIVETICKER_RESULT_CODE) {
                    Fragment fragment1 = adapter.getRegisteredFragment(0);
                    Fragment fragment2 = adapter.getRegisteredFragment(1);
                    if (fragment1 != null) {
                        ((FeedBaseFragment) fragment1).requestFeed();
                    }
                    if (fragment2 != null && !getCurrentUser().isAnonymous()) {
                        ((FeedBaseFragment) fragment2).requestFeed();
                    }
                }
                break;
            case Constants.LIVETICKER_ACTIVITY_REQUEST:
                if (resultCode == RESULT_OK) {
                    livetickerID = data.getStringExtra("livetickerID");
                    if (livetickerID != null) {
                        FirebaseDatabase.getInstance().getReference("liveticker/" + livetickerID + "/title").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                livetickerTitle.setText(dataSnapshot.getValue().toString());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        FirebaseDatabase.getInstance().getReference("liveticker/" + livetickerID + "/state").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue().equals(Constants.LIVETICKER_STARTED_STATE)) {
                                    livetickerBarStatus.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.state_started));
                                } else if (dataSnapshot.getValue().equals(Constants.LIVETICKER_NOT_STARTED_STATE)) {
                                    livetickerBarStatus.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.state_not_started));
                                } else {
                                    livetickerBarStatus.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.state_finished));
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
                showLivetickerBar(true);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
            case R.id.fab2:
                if (!getCurrentUser().isAnonymous() && getCurrentUser().isEmailVerified()) {
                    startActivityForResult(new Intent(MainActivity.this, AddLivetickerActivity.class), Constants.ADD_LIVETICKER_REQUEST_CODE);
                } else {
                    DialogFragment registerDialog = RegisterDialog.newInstance();
                    registerDialog.show(getSupportFragmentManager(), "dialog");
                }
                break;
            case R.id.main_liveticker_bar_button:
                if (livetickerID != null) {
                    Intent i = new Intent(this, LivetickerActivity.class);
                    i.putExtra("livetickerID", livetickerID);
                    startActivityForResult(i, Constants.LIVETICKER_ACTIVITY_REQUEST);
                    break;
                }
            case R.id.main_liveticker_bar_close:
                showLivetickerBar(false);
                break;

        }
    }

    private void initializeUserListener() {
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    if (dataSnapshot.child("userName") != null && dataSnapshot.child("profilePicture") != null) {
                        title.setVisibility(GONE);
                        authorImage.setVisibility(View.VISIBLE);
                        authorName.setVisibility(View.VISIBLE);
                        String profilePicture = dataSnapshot.child("profilePicture").getValue(String.class);

                        if (profilePicture != null && !profilePicture.isEmpty()) {
                            Utilities.setupRoundImageViewWithPlaceholder(authorImage, MainActivity.this, profilePicture, R.drawable.profile_placeholder);
                        } else {
                            Utilities.setupRoundImageViewOnlyPlaceholder(authorImage, MainActivity.this, R.drawable.profile_placeholder);
                        }
                        authorName.setText(dataSnapshot.child("userName").getValue(String.class));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void showLivetickerBar(boolean value) {
        if (value) {
            livetickerBar.startAnimation(enterAnimation);
            fab2.startAnimation(scaleEnterAnimation);
            livetickerBarShadow.startAnimation(enterAnimation);
            livetickerBarShadow.setVisibility(View.VISIBLE);
            livetickerBar.setVisibility(View.VISIBLE);
            fab2.setVisibility(View.VISIBLE);
            fab.setVisibility(GONE);
        } else {
            livetickerBar.startAnimation(exitAnimation);
            fab2.startAnimation(scaleOutAnimation);
            fab.startAnimation(scaleEnterAnimation);
            livetickerBarShadow.startAnimation(exitAnimation);
            livetickerBarShadow.setVisibility(View.GONE);
            fab2.setVisibility(View.GONE);
            livetickerBar.setVisibility(GONE);
            fab.setVisibility(View.VISIBLE);
        }
    }
}