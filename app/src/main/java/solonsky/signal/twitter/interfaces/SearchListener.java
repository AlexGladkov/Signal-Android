package solonsky.signal.twitter.interfaces;

import android.support.v7.widget.PopupMenu;

/**
 * Created by neura on 14.10.17.
 */

public interface SearchListener {
    void updatePosition(int position);
    void updateBar(int scrollY);
    void startSearch(String searchText);
}
