package com.example.deonew.car;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by deonew on 17-4-4.
 */

public class MainViewPagerAdapter extends FragmentPagerAdapter {
    private String titles[] = new String[]{"Text","Audio"};
    public MainViewPagerAdapter(FragmentManager fm, Context context){
        super(fm);
    }
    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 1:
                return TextFragment.newInstance(0);
            case 2:
                return TextFragment.newInstance(0);

        }
        return TextFragment.newInstance(0);
    }


    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
//        return super.getPageTitle(position);
    }
}
