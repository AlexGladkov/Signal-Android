package solonsky.signal.twitter.viewmodels;

import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import org.joda.time.LocalDateTime;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 01.06.17.
 */

public class ContentViewModel extends BaseObservable {
    private String followersCount;
    private String avatarUrl;
    private String username;
    private String text;
    private String counter;

    private boolean favorited;
    private boolean overlayed;
    private boolean collapsed;

    private LocalDateTime localDateTime;

    public interface ContentClickHandler {
        void onReplyClick(View v);
        void onRtClick(View v);
        void onLikeClick(View v);
        void onShareClick(View v);
        void onShareTweetClick(View v);
        void onMoreClick(View v);
        void onBackClick(View v);
        void onBrowserClick(View v);
        void onSaveClick(View v);
        void onBottomClick(View v);
    }

    public ContentViewModel(String avatarUrl, String username, String followersCount, String text,
                            String counter, boolean favorited, LocalDateTime localDateTime) {
        this.avatarUrl = avatarUrl;
        this.username = username;
        this.followersCount = followersCount;
        this.favorited = favorited;
        this.overlayed = true;
        this.collapsed = false;
        this.text = text;
        this.counter = counter;
        this.localDateTime = localDateTime;
    }

    @Bindable
    public String getAvatarUrl() {
        return avatarUrl;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        notifyPropertyChanged(BR.avatarUrl);
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
    public String getFollowersCount() {
        return followersCount;
    }
    public void setFollowersCount(String followersCount) {
        this.followersCount = followersCount;
        notifyPropertyChanged(BR.followersCount);
    }

    @Bindable
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
        notifyPropertyChanged(BR.text);
    }

    @Bindable
    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }
    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
        notifyPropertyChanged(BR.localDateTime);
    }

    @Bindable
    public String getCounter() {
        return counter;
    }
    public void setCounter(String counter) {
        this.counter = counter;
        notifyPropertyChanged(BR.counter);
    }

    @Bindable
    public boolean isFavorited() {
        return favorited;
    }
    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
        notifyPropertyChanged(BR.favorited);
    }

    @Bindable
    public boolean isOverlayed() {
        return overlayed;
    }
    public void setOverlayed(boolean overlayed) {
        this.overlayed = overlayed;
        notifyPropertyChanged(BR.overlayed);
    }

    @Bindable
    public boolean isCollapsed() {
        return collapsed;
    }
    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        notifyPropertyChanged(BR.collapsed);
    }
}
