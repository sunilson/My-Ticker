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
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.MainActivityFragmentPagerAdapter;
import com.sunilson.pro4.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    @BindView(R.id.mainActivity_loginButton)
    Button loginButton;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    private ViewPager viewPager;
    private TabLayout tabLayout;

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
        viewPager.setAdapter(new MainActivityFragmentPagerAdapter(getSupportFragmentManager(), MainActivity.this));
        tabLayout.setupWithViewPager(viewPager);

        //Setup onClick Listeners
        loginButton.setOnClickListener(this);

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
        }

        return false;
    }

    @Override
    protected void authChanged(FirebaseUser user) {
        if (user == null) {
            signInAnonymously();
        } else {
            if (user.isAnonymous()) {
                loginButton.setVisibility(View.VISIBLE);
                fab.setVisibility(GONE);
                Toast.makeText(MainActivity.this, "ANONYNMOUS", Toast.LENGTH_SHORT).show();
            } else {
                loginButton.setVisibility(GONE);
                fab.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mainActivity_loginButton:
                startActivity(new Intent(MainActivity.this, AuthenticationActivity.class));
                break;
        }
    }
}