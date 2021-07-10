package solonsky.signal.twitter.adapters;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;

import solonsky.signal.twitter.libs.tabBar.SimpleTabBarAdapter;
import solonsky.signal.twitter.models.TabModel;

/**
 * Created by neura on 25.05.17.
 */

public class TabPagerAdapter extends SimpleTabBarAdapter {
    private ArrayList<TabModel> tabModels;

    public TabPagerAdapter(FragmentManager fragmentManager, ArrayList<TabModel> tabModels) {
        super(fragmentManager);
        this.tabModels = tabModels;
    }

    @Override public int getCount() {
        return tabModels.size();
    }
    @Override public Fragment getItem(int position) {
        return tabModels.get(position).getFragment();
    }
    @Override public CharSequence getPageTitle(int position) {
        return tabModels.get(position).getTitle();
    }
    @Override public int getColorResource(int position) {
        return tabModels.get(position).getColorResource();
    }
    @Override public int getTextColorResource(int position) {
        return tabModels.get(position).getTextColorResource();
    }

    @Override public int getIconResource(int position) {
        return tabModels.get(position).getIconResource();
    }

    @Override public int getTintResource(int position) {
        return 0;
    }
}
