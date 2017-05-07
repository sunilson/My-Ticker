package com.sunilson.pro4.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.sunilson.pro4.R;
import com.sunilson.pro4.fragments.FeedFragment;
import com.sunilson.pro4.fragments.ProfileFragment;
import com.sunilson.pro4.fragments.SearchFragment;

/**
 * @author Linus Weiss
 */

public class MainActivityFragmentPagerAdapter extends FragmentPagerAdapter {

    SparseArray<Fragment> registeredFragments = new SparseArray<>();

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
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.feed_fragment_title);
            case 1:
                return context.getString(R.string.search_fragment_title);
            case 2:
                return context.getString(R.string.profile_fragment_title);
            default:
                return context.getString(R.string.feed_fragment_title);
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}