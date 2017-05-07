package com.sunilson.pro4.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.sunilson.pro4.R;
import com.sunilson.pro4.fragments.ChannelArchiveFragment;
import com.sunilson.pro4.fragments.ChannelLiveFragment;
import com.sunilson.pro4.views.ChannelViewPager;

/**
 * @author Linus Weiss
 */

public class ChannelFragmentPagerAdapter extends FragmentPagerAdapter {

    SparseArray<Fragment> registeredFragments = new SparseArray<>();

    private Context context;
    private String authorID;
    private int mCurrentPosition = -1;
    final int PAGE_COUNT = 3;

    public ChannelFragmentPagerAdapter(FragmentManager fm, Context context, String authorID) {
        super(fm);
        this.context = context;
        this.authorID = authorID;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ChannelLiveFragment.newInstance(authorID);
            case 1:
                return ChannelArchiveFragment.newInstance(authorID);
            case 2:
                return ChannelLiveFragment.newInstance(authorID);
            default:
                return ChannelLiveFragment.newInstance(authorID);
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
                return context.getString(R.string.channel_live_tickers);
            case 1:
                return context.getString(R.string.channel_past_tickers);
            case 2:
                return context.getString(R.string.channel_info);
            default:
                return context.getString(R.string.channel_live_tickers);
        }
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (position != mCurrentPosition) {
            Fragment fragment = (Fragment) object;
            ChannelViewPager pager = (ChannelViewPager) container;
            if (fragment != null && fragment.getView() != null) {
                mCurrentPosition = position;
                pager.measureCurrentView(fragment.getView());
            }
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
