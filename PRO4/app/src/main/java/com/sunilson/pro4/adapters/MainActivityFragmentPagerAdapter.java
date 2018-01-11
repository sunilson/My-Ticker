package com.sunilson.pro4.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.sunilson.pro4.fragments.FeedFragment;
import com.sunilson.pro4.fragments.LiveFragment;
import com.sunilson.pro4.fragments.ProfileFragment;
import com.sunilson.pro4.fragments.SearchFragment;

/**
 * @author Linus Weiss
 */

public class MainActivityFragmentPagerAdapter extends FragmentPagerAdapter {

    //All fragments in this adapter
    SparseArray<Fragment> registeredFragments = new SparseArray<>();

    private Context context;
    final int PAGE_COUNT = 4;

    public MainActivityFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return LiveFragment.newInstance();
            case 1:
                return FeedFragment.newInstance();
            case 2:
                return SearchFragment.newInstance();
            case 3:
                return ProfileFragment.newInstance();
            default:
                return FeedFragment.newInstance();
        }
    }

    /**
     * When fragment is instantiated, add it to the fragments array
     *
     * @param container
     * @param position
     * @return
     */
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

    /**
     * Title is set dynamically, so no need to set it in the adapter
     *
     * @param position
     * @return
     */
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "";
            case 1:
                return "";
            case 2:
                return "";
            case 3:
                return "";
            default:
                return "";
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    /**
     * Get a fragment from the adapter at a certain position
     *
     * @param position
     * @return Fragment
     */
    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    /**
     * Get all registered fragments from the adapter
     *
     * @return Array with Fragments
     */
    public SparseArray<Fragment> getRegisteredFragments() {
        return registeredFragments;
    }
}