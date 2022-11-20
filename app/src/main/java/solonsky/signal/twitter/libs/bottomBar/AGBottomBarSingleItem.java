package solonsky.signal.twitter.libs.bottomBar;


import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

/**
 * Created by neura on 21.05.17.
 */

public class AGBottomBarSingleItem extends AGBottomBarItem {
    private @ColorRes
    int captionBackgroundColor;
    private @DrawableRes
    int iconUrl;

    public AGBottomBarSingleItem(int id, boolean hasNew, boolean isStart,
                                 int captionBackgroundColor, int iconUrl) {
        super(id, hasNew, isStart);
        this.captionBackgroundColor = captionBackgroundColor;
        this.iconUrl = iconUrl;
    }

    public int getCaptionBackgroundColor() {
        return captionBackgroundColor;
    }

    public void setCaptionBackgroundColor(int captionBackgroundColor) {
        this.captionBackgroundColor = captionBackgroundColor;
    }

    public int getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(int iconUrl) {
        this.iconUrl = iconUrl;
    }
}

