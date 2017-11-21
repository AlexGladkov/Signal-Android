package solonsky.signal.twitter.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import org.joda.time.LocalDateTime;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 25.05.17.
 */

public class TweetViewModel extends BaseObservable {
    private int mediaSource;
    private String iconUrl;
    private String username;
    private String twitterName;
    private String locationName;
    private String source;
    private LocalDateTime dateTime;
    private boolean isFollowed;
    private boolean hasPreview;

    public interface TweetClickHandler {

    }

    public TweetViewModel(int mediaSource, String iconUrl, String username, String twitterName,
                          String locationName, String source, LocalDateTime dateTime, boolean isFollowed,
                          boolean hasPreview) {
        this.mediaSource = mediaSource;
        this.iconUrl = iconUrl;
        this.username = username;
        this.twitterName = twitterName;
        this.locationName = locationName;
        this.source = source;
        this.dateTime = dateTime;
        this.isFollowed = isFollowed;
        this.hasPreview = hasPreview;
    }

    @Bindable
    public int getMediaSource() {
        return mediaSource;
    }
    public void setMediaSource(int mediaSource) {
        this.mediaSource = mediaSource;
        notifyPropertyChanged(BR.mediaSource);
    }

    @Bindable
    public String getIconUrl() {
        return iconUrl;
    }
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        notifyPropertyChanged(BR.iconUrl);
    }

    @Bindable
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
        notifyPropertyChanged(BR.username);
    }

    @Bindable
    public String getTwitterName() {
        return twitterName;
    }
    public void setTwitterName(String twitterName) {
        this.twitterName = twitterName;
        notifyPropertyChanged(BR.twitterName);
    }

    @Bindable
    public String getLocationName() {
        return locationName;
    }
    public void setLocationName(String locationName) {
        this.locationName = locationName;
        notifyPropertyChanged(BR.locationName);
    }

    @Bindable
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
        notifyPropertyChanged(BR.source);
    }

    @Bindable
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
        notifyPropertyChanged(BR.dateTime);
    }

    @Bindable
    public boolean isFollowed() {
        return isFollowed;
    }
    public void setFollowed(boolean followed) {
        isFollowed = followed;
        notifyPropertyChanged(BR.followed);
    }

    @Bindable
    public boolean isHasPreview() {
        return hasPreview;
    }
    public void setHasPreview(boolean hasPreview) {
        this.hasPreview = hasPreview;
        notifyPropertyChanged(BR.hasPreview);
    }
}
