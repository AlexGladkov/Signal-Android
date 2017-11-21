package solonsky.signal.twitter.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by neura on 01.06.17.
 */

public class SimplePagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragments;

    public SimplePagerAdapter(ArrayList<Fragment> fragments, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.fragments = fragments;
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }
}
