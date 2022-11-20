package solonsky.signal.twitter.interfaces;


import androidx.annotation.ColorRes;

import solonsky.signal.twitter.helpers.Flags;

/**
 * Created by neura on 15.09.17.
 */

public interface ActivityListener {
    void updateStatusType(Flags.STATUS_TYPE statusType);
    void updateToolbarState(int state, @ColorRes int statusBarColor);
    void updateCounter(int count);
    void updateBars(int dy);
    void updateSettings(int title, boolean isStaticTop, boolean isStaticBottom);
    void updateTitle(int title);

    void onStartSearch(String searchQuery);
    void onEndSearch();

    Flags.STATUS_TYPE checkState();
}
