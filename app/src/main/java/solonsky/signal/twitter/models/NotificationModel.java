package solonsky.signal.twitter.models;

import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import org.joda.time.LocalDateTime;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;

/**
 * Created by neura on 25.05.17.
 */

public class NotificationModel extends BaseObservable {
    public final static int TYPE_REPLY = 0;
    public final static int TYPE_RT = 1;
    public final static int TYPE_LIKE = 2;
    public final static int TYPE_FOLLOW = 3;
    public final static int TYPE_LIST = 4;
    public final static int TYPE_QUOTE = 5;

    private long id;
    private int type;
    private String username;
    private String text;
    private String iconUrl;

    private User user = null;
    private StatusModel statusModel = null;

    private int subline;
    private int colorResource;
    private int drawableResource;
    private int divideState;
    private LocalDateTime dateTime;
    boolean isHighlighted;

    public interface NotificationClickHandler {
        void onItemClick(View v);
    }

    public NotificationModel(long id, int type, String username, String text, String iconUrl,
                              int subline, int colorResource,
                              int drawableResource, LocalDateTime dateTime, boolean isHighlighted) {
        this.id = id;
        this.type = type;
        this.username = username;
        this.text = text;
        this.iconUrl = iconUrl;
        this.subline = subline;
        this.colorResource = colorResource;
        this.drawableResource = drawableResource;
        this.dateTime = dateTime;
        this.isHighlighted = isHighlighted;
        this.divideState = AppData.DIVIDER_SHORT;
    }

    public static NotificationModel getReplyInstance(long id, String username, String text, String iconUrl,
                                                     LocalDateTime dateTime, boolean isHighlighted) {
        return new NotificationModel(id, TYPE_REPLY, username, text, iconUrl, R.string.notifications_replied,
                App.getInstance().isNightEnabled() ? R.color.dark_reply_tint_color : R.color.light_reply_tint_color,
                R.drawable.ic_badges_activity_reply, dateTime, isHighlighted);
    }

    public static NotificationModel getMentionInstance(long id, String username, String text, String iconUrl,
                                                     LocalDateTime dateTime, boolean isHighlighted) {
        return new NotificationModel(id, TYPE_REPLY, username, text, iconUrl, R.string.notifications_mentioned,
                App.getInstance().isNightEnabled() ? R.color.dark_reply_tint_color : R.color.light_reply_tint_color,
                R.drawable.ic_badges_activity_reply, dateTime, isHighlighted);
    }

    public static NotificationModel getRetweetInstance(long id, String username, String text, String iconUrl,
                                                       LocalDateTime dateTime, boolean isHighlighted) {
        return new NotificationModel(id, TYPE_RT, username, text, iconUrl, R.string.notifications_retweeted,
                App.getInstance().isNightEnabled() ? R.color.dark_rt_tint_color : R.color.light_rt_tint_color,
                R.drawable.ic_badges_activity_rt, dateTime, isHighlighted);
    }

    public static NotificationModel getLikedInstance(long id, String username, String text, String iconUrl,
                                                     LocalDateTime dateTime, boolean isHighlighted) {
        return new NotificationModel(id, TYPE_LIKE, username, text, iconUrl, R.string.notifications_liked,
                App.getInstance().isNightEnabled() ? R.color.dark_like_tint_color : R.color.light_like_tint_color,
                R.drawable.ic_badges_activity_like, dateTime, isHighlighted);
    }

    public static NotificationModel getFollowInstance(long id, String username, String text, String iconUrl,
                                                      LocalDateTime dateTime, boolean isHighlighted) {
        return new NotificationModel(id, TYPE_FOLLOW, username, text, iconUrl, R.string.notifications_followed,
                App.getInstance().isNightEnabled() ? R.color.dark_profile_tint_color : R.color.light_profile_tint_color,
                R.drawable.ic_badges_activity_profile, dateTime, isHighlighted);
    }

    public static NotificationModel getListInstance(long id, String username, String text, String iconUrl,
                                                    LocalDateTime dateTime, boolean isHighlighted) {
        return new NotificationModel(id, TYPE_LIST, username, text, iconUrl, R.string.notifications_listed,
                App.getInstance().isNightEnabled() ? R.color.dark_profile_tint_color : R.color.light_profile_tint_color,
                R.drawable.ic_badges_activity_list, dateTime, isHighlighted);
    }

    public static NotificationModel getQuoteInstance(long id, String username, String text, String iconUrl,
                                                     LocalDateTime dateTime, boolean isHighlighted) {
        return new NotificationModel(id, TYPE_QUOTE, username, text, iconUrl, R.string.notifications_quoted,
                App.getInstance().isNightEnabled() ? R.color.dark_rt_tint_color : R.color.light_rt_tint_color,
                R.drawable.ic_badges_activity_quote, dateTime, isHighlighted);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NotificationModel)) return false;
        if (obj == this) return true;

        NotificationModel notificationModel = (NotificationModel) obj;
        return this.id == notificationModel.getId();
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public StatusModel getStatusModel() {
        return statusModel;
    }

    public void setStatusModel(StatusModel statusModel) {
        this.statusModel = statusModel;
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
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
        notifyPropertyChanged(BR.text);
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
    public String getIconUrl() {
        return iconUrl;
    }
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        notifyPropertyChanged(BR.iconUrl);
    }

    @Bindable
    public int getColorResource() {
        return colorResource;
    }
    public void setColorResource(int colorResource) {
        this.colorResource = colorResource;
        notifyPropertyChanged(BR.colorResource);
    }

    @Bindable
    public int getDrawableResource() {
        return drawableResource;
    }
    public void setDrawableResource(int drawableResource) {
        this.drawableResource = drawableResource;
        notifyPropertyChanged(BR.drawableResource);
    }

    @Bindable
    public boolean isHighlighted() {
        return isHighlighted;
    }
    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
        notifyPropertyChanged(BR.highlighted);
    }

    @Bindable
    public int getSubline() {
        return subline;
    }
    public void setSubline(int subline) {
        this.subline = subline;
        notifyPropertyChanged(BR.subline);
    }

    @Bindable
    public int getDivideState() {
        return divideState;
    }

    public void setDivideState(int divideState) {
        this.divideState = divideState;
        notifyPropertyChanged(BR.divideState);
    }
}
