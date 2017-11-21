package solonsky.signal.twitter.models;

import android.databinding.Bindable;

import org.joda.time.LocalDateTime;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.helpers.AppData;

/**
 * Created by neura on 23.05.17.
 */
public class FeedVideoModel extends FeedModel {
    private String previewUrl;
    private String videoUrl;

    public FeedVideoModel(long id, String text, String username, String iconUrl, String retweetText,
                          String previewUrl, String videoUrl,
                          LocalDateTime date, boolean isFavorite, boolean isRetweeted, boolean isHighlighted) {
        super(id, AppData.FEED_TYPE_VIDEO, text, username, iconUrl, retweetText, date, isFavorite, isRetweeted, isHighlighted);
        this.previewUrl = previewUrl;
        this.videoUrl = videoUrl;
    }

    @Override
    @Bindable
    public String getUsername() {
        return super.getUsername();
    }

    @Override
    @Bindable
    public String getText() {
        return super.getText();
    }

    @Override
    @Bindable
    public LocalDateTime getDate() {
        return super.getDate();
    }

    @Override
    @Bindable
    public String getIconUrl() {
        return super.getIconUrl();
    }

    @Override
    @Bindable
    public String getRetweetText() {
        return super.getRetweetText();
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
    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
        notifyPropertyChanged(BR.videoUrl);
    }
}
