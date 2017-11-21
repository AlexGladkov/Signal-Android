package solonsky.signal.twitter.interfaces;

/**
 * Created by neura on 16.09.17.
 */

public interface FragmentCounterListener {
    void onScrollToTop();
    void onScrollToTopWithAnimation(int startDuration, int pauseDuration ,int endDuration);
    void onBackToPosition();
    void onUpdate();
}
