package com.sunilson.pro4.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.sunilson.pro4.R;
import com.sunilson.pro4.fragments.LivetickerFragment;
import com.sunilson.pro4.interfaces.CanChangeFragment;
import com.sunilson.pro4.utilities.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

public class LivetickerActivity extends BaseActivity implements CanChangeFragment {

    private String livetickerID;
    public String currentFragment;
    private boolean started;
    private FirebaseUser user;
    private Menu menu;

    @BindView(R.id.liveticker_appbar)
    AppBarLayout appBarLayout;

    @BindView(R.id.liveticker_status_image)
    ImageView statusImage;

    @BindView(R.id.title)
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liveticker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.liveticker_toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        if (savedInstanceState == null) {
            Intent i = getIntent();

            if (i.getStringExtra(Constants.LIVETICKER_ID) != null) {
                //Started from within app
                livetickerID = i.getStringExtra(Constants.LIVETICKER_ID);
            }

            //Listen to changes of the fragment backstack
            getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    FragmentManager manager = getSupportFragmentManager();
                    //If current fragment is Liveticker fragment, notify it about the change
                    if (manager != null && currentFragment != null && currentFragment.equals(Constants.FRAGMENT_LIVETICKER_TAG)) {
                        LivetickerFragment fragment = (LivetickerFragment) manager.findFragmentByTag(currentFragment);
                        fragment.onFragmentResumeFromBackstack();
                    }
                }
            });

            currentFragment = Constants.FRAGMENT_LIVETICKER_TAG;
            getSupportFragmentManager().beginTransaction().replace(R.id.content_liveticker, LivetickerFragment.newInstance(livetickerID), Constants.FRAGMENT_LIVETICKER_TAG).commit();
        }
    }

    @Override
    protected void authChanged(FirebaseUser user) {
        this.user = user;
        if (user != null && livetickerID != null) {
            if (!started) {
                started = true;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (menu != null) {
            this.menu = menu;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void replaceFragment(Fragment fragment, String tag) {
        currentFragment = tag;

        //Change bottom input layout depending on current fragment
        findViewById(R.id.fragment_liveticker_input_layout).setVisibility(GONE);
        findViewById(R.id.fragment_comments_input_layout).setVisibility(GONE);

        //Use custom animations depending on new fragment
        if (tag.equals(Constants.FRAGMENT_COMMENTS_TAG)) {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.exit_to_bottom, R.anim.exit_to_bottom).add(R.id.content_liveticker, fragment, tag).addToBackStack(null).commit();
        } else {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right).replace(R.id.content_liveticker, fragment, tag).commit();
        }

    }

    /**
     * Change state in Toolbar
     *
     * @param value new state
     */
    public void updateLivetickerState(String value) {
        if (value.equals(Constants.LIVETICKER_NOT_STARTED_STATE)) {
            statusImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.state_not_started));
        } else if (value.equals(Constants.LIVETICKER_STARTED_STATE)) {
            statusImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.state_started));
        } else {
            statusImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.state_finished));
        }
    }

    public void updateLivetickerTitle(String title) {
        this.title.setText(title);
    }

    @Override
    public void onBackPressed() {
        if (currentFragment.equals(Constants.FRAGMENT_COMMENTS_TAG)) {
            currentFragment = Constants.FRAGMENT_LIVETICKER_TAG;
            findViewById(R.id.fragment_comments_input_layout).setVisibility(GONE);
        }

        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //Listen for back press when the current fragment is the Liveticker fragment
        if ((keyCode == KeyEvent.KEYCODE_BACK) && currentFragment.equals(Constants.FRAGMENT_LIVETICKER_TAG)) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("livetickerID", livetickerID);
            setResult(Activity.RESULT_OK, returnIntent);
        }
        return super.onKeyDown(keyCode, event);
    }
}
