package solonsky.signal.twitter.interfaces;

/**
 * Created by neura on 11.10.17.
 */

public interface ProfileRefreshHandler {
    void onAvatarUpdate();
    void onBannerUpdate();
    void onInfoUpdate();
}
