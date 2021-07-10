package solonsky.signal.twitter.models;


import androidx.databinding.Bindable;

import org.joda.time.LocalDateTime;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.helpers.AppData;

/**
 * Created by neura on 23.05.17.
 */

public class FeedGifModel extends FeedModel {
    private String previewUrl;
    private String gifUrl;

    public FeedGifModel(long id, String text, String username, String iconUrl, String retweetText,
                        String previewUrl, String gifUrl,
                        LocalDateTime date, boolean isFavorite, boolean isRetweeted, boolean isHighlighted) {
        super(id, AppData.FEED_TYPE_GIF, text, username, iconUrl, retweetText, date, isFavorite, isRetweeted, isHighlighted);
        this.previewUrl = previewUrl;
        this.gifUrl = gifUrl;
    }

    @Override
    @Bindable
    public String getRetweetText() {
        return super.getRetweetText();
    }

    @Override
    @Bindable
    public String getIconUrl() {
        return super.getIconUrl();
    }

    @Override
    @Bindable
    public LocalDateTime getDate() {
        return super.getDate();
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

    @Bindable
    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
        notifyPropertyChanged(BR.previewUrl);
    }

    @Bindable
    public String getGifUrl() {
        return gifUrl;
    }

    public void setGifUrl(String gifUrl) {
        this.gifUrl = gifUrl;
        notifyPropertyChanged(BR.gifUrl);
    }
}
