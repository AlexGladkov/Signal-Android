package solonsky.signal.twitter.interfaces;

import android.content.Intent;

/**
 * Created by neura on 11.10.17.
 */

public interface ProfileListener {
    /**
     * Update header to fit height (with fancy animation)
     * @param diff - difference for change (0 for stats)
     * @param isAnimated - @true for change with fancy animation
     */
    void updateHeader(int diff, boolean isAnimated);

    /**
     * Setup avatar change behavior when scroll
     * @param scrollY - scroll y position
     */
    void updateAvatar(int scrollY);

    /**
     * Calls to fill bio
     */
    void updateInfo();

    /**
     * Calls to fill stats
     */
    void updateStats();

    /**
     * Show activity
     * @param intent - activity intent
     */
    void openActivity(Intent intent);

    /**
     * Open link in browser
     * @param string - link
     */
    void openLink(String string);
}
