package com.example.deonew.car.Main;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.deonew.car.Fragment.AudioFragment;
import com.example.deonew.car.Fragment.VideoFragment;
import com.example.deonew.car.Fragment.TextFragment;

/**
 * Created by deonew on 17-4-4.
 */

public class MainViewPagerAdapter extends FragmentPagerAdapter {
    private String titles[] = new String[]{"Text","Audio","Video"};
    public MainViewPagerAdapter(FragmentManager fm, Context context){
        super(fm);
    }
    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return TextFragment.newInstance(0);
            case 1:
                return AudioFragment.newInstance(0);
            case 2:
                return VideoFragment.newInstance(0);
            default:
                return TextFragment.newInstance(0);
        }

    }


    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
//        return super.getPageTitle(position);
    }
}
