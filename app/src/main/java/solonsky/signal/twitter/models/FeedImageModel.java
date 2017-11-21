package solonsky.signal.twitter.models;

import android.databinding.Bindable;

import org.joda.time.LocalDateTime;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.helpers.AppData;

/**
 * Created by neura on 22.05.17.
 * @link FeedModel inherit class
 */
public class FeedImageModel extends FeedModel {
    private String imageUrl;

    public FeedImageModel(long id, String text, String username, String imageUrl, String iconUrl, String retweetText,
                          LocalDateTime date, boolean isFavorite, boolean isRetweeted, boolean isHighlighted) {
        super(id, AppData.FEED_TYPE_IMAGE, text, username, iconUrl, retweetText, date, isFavorite, isRetweeted, isHighlighted);
        this.imageUrl = imageUrl;
    }

    @Bindable
    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        notifyPropertyChanged(BR.imageUrl);
    }

    @Override
    @Bindable
    public String getText() {
        return super.getText();
    }

    @Override
    @Bindable
    public String getUsername() {
        return super.getUsername();
    }

    @Override
    @Bindable
    public LocalDateTime getDate() {
        return super.getDate();
    }

    @Override
    @Bindable
    public boolean isFavorite() {
        return super.isFavorite();
    }

    @Override
    @Bindable
    public String getIconUrl() {
        return super.getIconUrl();
    }

    @Override
    @Bindable
    public boolean isExpand() {
        return super.isExpand();
    }

    @Override
    @Bindable
    public String getRetweetText() {
        return super.getRetweetText();
    }

    @Override
    @Bindable
    public boolean isRetweeted() {
        return super.isRetweeted();
    }

    @Override
    @Bindable
    public boolean isHighlighted() {
        return super.isHighlighted();
    }
}
