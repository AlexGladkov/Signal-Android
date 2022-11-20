package solonsky.signal.twitter.models;


import androidx.databinding.Bindable;

import org.joda.time.LocalDateTime;

import solonsky.signal.twitter.helpers.AppData;

/**
 * Created by neura on 23.05.17.
 */

public class FeedTextModel extends FeedModel {

    public FeedTextModel(long id, String text, String username, String iconUrl, String retweetText,
                         LocalDateTime date, boolean isFavorite, boolean isRetweeted, boolean isHighlighted) {
        super(id, AppData.FEED_TYPE_TEXT, text, username, iconUrl, retweetText, date, isFavorite, isRetweeted, isHighlighted);
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

    @Override
    @Bindable
    public boolean isHighlighted() {
        return super.isHighlighted();
    }

    @Override
    @Bindable
    public boolean isExpand() {
        return super.isExpand();
    }
}
