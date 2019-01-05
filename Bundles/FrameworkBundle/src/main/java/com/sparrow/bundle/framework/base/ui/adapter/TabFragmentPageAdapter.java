package com.sparrow.bundle.framework.base.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * @author zhangshaopeng
 * @date 2018/8/12
 * @description
 */
public class TabFragmentPageAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> mList;
    private String[] mTitles;

    private List<String> mListTitles;

    public TabFragmentPageAdapter(FragmentManager fm, List<Fragment> list, String[] mTitles) {
        super(fm);
        this.mList = list;
        this.mTitles = mTitles;
    }

    public TabFragmentPageAdapter(FragmentManager fm, List<Fragment> list, List<String> mTitles) {
        super(fm);
        this.mList = list;
        this.mListTitles = mTitles;
    }

    public TabFragmentPageAdapter(FragmentManager fm, List<Fragment> list) {
        super(fm);
        this.mList = list;
    }

    @Override
    public Fragment getItem(int position) {
        return mList.get(position);

    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mTitles != null && mTitles[position] != null) {
            return mTitles[position];
        }

        if (mListTitles != null && mListTitles.get(position) != null) {
            return mListTitles.get(position);
        }
        return "";
    }

    public void setTitles(String[] titles) {
        this.mTitles = titles;
    }
}
