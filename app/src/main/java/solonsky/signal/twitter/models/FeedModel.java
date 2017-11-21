package solonsky.signal.twitter.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import org.joda.time.LocalDateTime;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 22.05.17.
 * Factory class for tweet models in feed
 */
public class FeedModel extends BaseObservable {
    private long id;
    private int type;
    private String text;
    private String username;
    private String iconUrl;
    private String retweetText;
    private LocalDateTime date;
    private boolean isFavorite;
    private boolean isExpand;
    private boolean isHighlighted;
    private boolean isRetweeted;

    public interface FeedClickHandler {
        void onItemClick(View v);
        void onShareClick(View v);
        void onLikeClick(View v);
        void onRetweetClick(View v);
        void onReplyClick(View v);
        void onMoreClick(View v);
        void onContentClick(View v);
        boolean longUserClick(View v);
        boolean longItemClick(View v);
    }

    FeedModel(long id, int type, String text, String username, String iconUrl, String retweetText,
              LocalDateTime date, boolean isFavorite, boolean isRetweeted, boolean isHighlighted) {
        this.id = id;
        this.text = text;
        this.username = username;
        this.retweetText = retweetText;
        this.date = date;
        this.type = type;
        this.isFavorite = isFavorite;
        this.isRetweeted = isRetweeted;
        this.isHighlighted = isHighlighted;
        this.isExpand = false;
        this.iconUrl = iconUrl;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
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
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
        notifyPropertyChanged(BR.username);
    }

    @Bindable
    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
        notifyPropertyChanged(BR.date);
    }

    @Bindable
    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
        notifyPropertyChanged(BR.favorite);
    }

    @Bindable
    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
        notifyPropertyChanged(BR.expand);
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
    public String getRetweetText() {
        return retweetText;
    }

    public void setRetweetText(String retweetText) {
        this.retweetText = retweetText;
        notifyPropertyChanged(BR.retweetText);
    }

    @Bindable
    public boolean isRetweeted() {
        return isRetweeted;
    }

    public void setRetweeted(boolean retweeted) {
        isRetweeted = retweeted;
        notifyPropertyChanged(BR.retweeted);
    }

    @Bindable
    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
        notifyPropertyChanged(BR.highlighted);
    }
}
