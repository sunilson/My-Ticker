package com.sunilson.pro4.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sunilson.pro4.fragments.FeedFragment;
import com.sunilson.pro4.fragments.ProfileFragment;
import com.sunilson.pro4.fragments.SearchFragment;

/**
 * @author Linus Weiss
 */

public class MainActivityFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context context;
    final int PAGE_COUNT = 3;

    public MainActivityFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return FeedFragment.newInstance();
            case 1:
                return SearchFragment.newInstance();
            case 2:
                return ProfileFragment.newInstance();
            default:
                return FeedFragment.newInstance();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Feed";
            case 1:
                return "Search";
            case 2:
                return "Profile";
            default:
                return "Feed";
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}