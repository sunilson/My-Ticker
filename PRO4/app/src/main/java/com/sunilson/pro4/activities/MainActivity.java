package com.sunilson.pro4.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sunilson.pro4.R;
import com.sunilson.pro4.adapters.MainActivityFragmentPagerAdapter;
import com.sunilson.pro4.fragments.FeedFragment;
import com.sunilson.pro4.interfaces.FragmentAuthInterface;
import com.sunilson.pro4.utilities.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static android.view.View.GONE;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.search_bar_layout)
    RelativeLayout searchBarLayout;

    @BindView(R.id.feed_bar)
    LinearLayout feedBarLayout;

    @BindView(R.id.feed_bar_spinner)
    Spinner feedBarSpinner;

    @BindView(R.id.main_bar_author_image)
    ImageView authorImage;

    @BindView(R.id.main_bar_title)
    TextView title;

    @BindView(R.id.main_bar_author_name)
    TextView authorName;

    private MenuItem loginButton, logoutButton;

    private ViewPager viewPager;
    private ValueEventListener userListener;
    private DatabaseReference currentUserReference;
    private TabLayout tabLayout;
    private MainActivityFragmentPagerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //ButterKnife Binding
        ButterKnife.bind(this);

        //Set up the Viewpager and the Tab Layout, which are used to switch between the 3 Fragments of the Main Activity
        viewPager = (ViewPager) findViewById(R.id.mainActivityViewPager);
        tabLayout = (TabLayout) findViewById(R.id.mainActivityTabLayout);
        viewPager.setAdapter(adapter = new MainActivityFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this));
        viewPager.setOffscreenPageLimit(2);
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

        fab.setOnClickListener(this);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.feed_spinner_values, R.layout.spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        feedBarSpinner.setAdapter(arrayAdapter);

        initializeUserListener();
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
            menu.findItem(R.id.feed_menu_refresh).setVisible(false);
            searchBarLayout.setVisibility(View.VISIBLE);
            feedBarLayout.setVisibility(View.GONE);
        } else if (viewPager.getCurrentItem() == 2) {
            menu.findItem(R.id.feed_menu_refresh).setVisible(false);
            searchBarLayout.setVisibility(View.GONE);
            feedBarLayout.setVisibility(View.GONE);
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
                    fab.setVisibility(GONE);
                }
            } else {
                if (loginButton != null && logoutButton != null) {
                    loginButton.setVisible(false);
                    logoutButton.setVisible(true);
                    fab.setVisibility(View.VISIBLE);
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
                    Fragment fragment = adapter.getRegisteredFragment(0);
                    if (fragment != null) {
                        ((FeedFragment) fragment).requestFeed();
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                startActivityForResult(new Intent(MainActivity.this, AddLivetickerActivity.class), Constants.ADD_LIVETICKER_REQUEST_CODE);
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
                            DrawableRequestBuilder<Integer> placeholder = Glide.with(MainActivity.this).load(R.drawable.profile_placeholder).bitmapTransform(new CropCircleTransformation(MainActivity.this));
                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(profilePicture);
                            Glide.with(MainActivity.this).using(new FirebaseImageLoader()).load(storageReference).thumbnail(placeholder).bitmapTransform(new CropCircleTransformation(MainActivity.this)).crossFade().into(authorImage);
                        } else {
                            Glide.with(MainActivity.this).load(R.drawable.profile_placeholder).bitmapTransform(new CropCircleTransformation(MainActivity.this)).crossFade().into(authorImage);

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
}