package solonsky.signal.twitter.models;

import android.databinding.Bindable;

import org.joda.time.LocalDateTime;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.helpers.AppData;

/**
 * Created by neura on 22.05.17.
 * @link FeedModel inherit class
 */
public class FeedQuoteModel extends FeedModel {
    private String quoteTitle;
    private String quoteText;
    private String quoteImageUrl;
    private boolean isQuotePreview;
    private int quoteType;
    private int systemUrl;

    public FeedQuoteModel(long id, String text, String username, String iconUrl, String retweetText,
                          LocalDateTime date, boolean isFavorite, boolean isRetweeted, boolean isHighlighted,
                          String quoteTitle, String quoteText, String quoteImageUrl, boolean isQuotePreview,
                          int quoteType, int systemUrl) {
        super(id, AppData.FEED_TYPE_QUOTE, text, username, iconUrl, retweetText, date, isFavorite, isRetweeted, isHighlighted);
        this.quoteTitle = quoteTitle;
        this.quoteText = quoteText;
        this.quoteImageUrl = quoteImageUrl;
        this.isQuotePreview = isQuotePreview;
        this.quoteType = quoteType;
        this.systemUrl = systemUrl;
    }

    @Bindable
    public String getQuoteImageUrl() {
        return quoteImageUrl;
    }
    public void setQuoteImageUrl(String quoteImageUrl) {
        this.quoteImageUrl = quoteImageUrl;
        notifyPropertyChanged(BR.quoteImageUrl);
    }

    @Bindable
    public boolean isQuotePreview() {
        return isQuotePreview;
    }
    public void setQuotePreview(boolean quotePreview) {
        isQuotePreview = quotePreview;
        notifyPropertyChanged(BR.quotePreview);
    }

    @Bindable
    public int getQuoteType() {
        return quoteType;
    }
    public void setQuoteType(int quoteType) {
        this.quoteType = quoteType;
        notifyPropertyChanged(BR.quoteType);
    }

    @Bindable
    public String getQuoteTitle() {
        return quoteTitle;
    }
    public void setQuoteTitle(String quoteTitle) {
        this.quoteTitle = quoteTitle;
        notifyPropertyChanged(BR.quoteTitle);
    }

    @Bindable
    public String getQuoteText() {
        return quoteText;
    }
    public void setQuoteText(String quoteText) {
        this.quoteText = quoteText;
        notifyPropertyChanged(BR.quoteText);
    }

    @Bindable
    public int getSystemUrl() {
        return systemUrl;
    }
    public void setSystemUrl(int systemUrl) {
        this.systemUrl = systemUrl;
        notifyPropertyChanged(BR.systemUrl);
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
