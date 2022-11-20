package solonsky.signal.twitter.libs.tabBar;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * Created by neura on 25.05.17.
 */

public abstract class SimpleTabBarAdapter extends FragmentPagerAdapter {

    public SimpleTabBarAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public abstract int getCount();
    public abstract Fragment getItem(int position);
    public abstract CharSequence getPageTitle(int position);
    public abstract int getColorResource(int position);
    public abstract int getTextColorResource(int position);
    public abstract int getIconResource(int position);
    public abstract int getTintResource(int position);
}
