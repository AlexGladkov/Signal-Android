package solonsky.signal.twitter.libs.bottomBar;


import androidx.annotation.DrawableRes;

/**
 * Created by neura on 26.05.17.
 */

public class AGBottomBarMultipleItem extends AGBottomBarItem {
    private @DrawableRes int iconUrl;
    private @DrawableRes
    int multipleUrl;

    public AGBottomBarMultipleItem(int id, boolean hasNew, boolean isStart,
                                   int iconUrl, int multipleUrl) {
        super(id, hasNew, isStart);
        this.iconUrl = iconUrl;
        this.multipleUrl = multipleUrl;
    }

    public int getIconUrl() {
        return iconUrl;
    }
    public void setIconUrl(int iconUrl) {
        this.iconUrl = iconUrl;
    }
    public int getMultipleUrl() {
        return multipleUrl;
    }
    public void setMultipleUrl(int multipleUrl) {
        this.multipleUrl = multipleUrl;
    }
}
