package com.sunilson.pro4.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

public class MainActivity extends BaseActivity {

    @BindView(R.id.fab)
    FloatingActionButton fab;

    private MenuItem loginButton, logoutButton;

    private ViewPager viewPager;
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
        tabLayout.setupWithViewPager(viewPager);

        //Listener to change title on fragment swap
        final ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                invalidateOptionsMenu();
            }

            @Override
            public void onPageSelected(int position) {
                setTitle(adapter.getPageTitle(position));
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

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddLivetickerActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if (viewPager.getCurrentItem() == 0) {
            menu.findItem(R.id.feed_menu_refresh).setVisible(true);
            menu.findItem(R.id.feed_menu_search).setVisible(false);
        } else if (viewPager.getCurrentItem() == 1) {
            menu.findItem(R.id.feed_menu_refresh).setVisible(false);
            menu.findItem(R.id.feed_menu_search).setVisible(true);
        } else if (viewPager.getCurrentItem() == 2) {
            menu.findItem(R.id.feed_menu_refresh).setVisible(false);
            menu.findItem(R.id.feed_menu_search).setVisible(false);
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
    protected void authChanged(FirebaseUser user) {
        checkLoginStatus(user);
        if (user != null) {
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
}