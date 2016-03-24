package com.proyecto.enrique.osporthello;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.io.Serializable;

class MyFragmentPagerAdapter extends FragmentStatePagerAdapter implements Serializable{
    final int PAGE_COUNT = 2;
    private String tabTitles[] = null;

    public MyFragmentPagerAdapter(FragmentManager fm, String tab1, String tab2) {
        super(fm);
        tabTitles = new String[] { tab1, tab2};
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {

        Fragment f = null;

        switch(position) {
            case 0:
                f = new ActivitiesFriendsFragment();
                break;
            case 1:
                f = new ShowFriendsFragment();
                break;
        }

        return f;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
