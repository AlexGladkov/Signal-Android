package solonsky.signal.twitter.models;

import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.Flags;

/**
 * Created by neura on 25.05.17.
 */

public class NotificationDetailModel extends BaseObservable {
    public static final int NOTIFICATION_REPLY = 0;
    public static final int NOTIFICATION_LIKE = 1;
    public static final int NOTIFICATION_RETWEET = 2;
    public static final int NOTIFICATION_FOLLOW = 3;
    public static final int NOTIFICATION_QUOTED = 4;
    public static final int NOTIFICATION_MENTIONED = 5;

    private long id;
    private String title;
    private String subtitle;
    private String iconUrl;
    private String text;

    private User user = null;
    private StatusModel statusModel = null;

    private int type;
    private int tintColor;
    private int subIconResource;
    private boolean isFollowed;
    private boolean isHighlighted;
    private int divideState = Flags.DIVIDER_SHORT;

    public interface DetailClickHandler {
        void onItemClick(View v);
        void onFollowClick(View v);
    }

    public static NotificationDetailModel getReplyInstance(long id, String title, String subtitle,
                                                           String iconUrl, String text, boolean isFollowed,
                                                           boolean isHighlighted) {
        return new NotificationDetailModel(id, title, subtitle, iconUrl, text,
                App.getInstance().isNightEnabled() ? R.color.dark_reply_tint_color : R.color.light_reply_tint_color,
                R.drawable.ic_badges_activity_reply, NOTIFICATION_REPLY, isFollowed, isHighlighted);
    }

    public static NotificationDetailModel getLikeInstance(long id, String title, String subtitle,
                                                          String iconUrl, String text, boolean isFollowed,
                                                          boolean isHighlighted) {
        return new NotificationDetailModel(id, title, subtitle, iconUrl, text,
                App.getInstance().isNightEnabled() ? R.color.dark_like_tint_color : R.color.light_like_tint_color,
                R.drawable.ic_badges_activity_like, NOTIFICATION_LIKE, isFollowed, isHighlighted);
    }

    public static NotificationDetailModel getRetweetInstance(long id, String title, String subtitle,
                                                             String iconUrl, String text, boolean isFollowed,
                                                             boolean isHighlighted) {
        return new NotificationDetailModel(id, title, subtitle, iconUrl, text,
                App.getInstance().isNightEnabled() ? R.color.dark_rt_tint_color : R.color.light_rt_tint_color,
                R.drawable.ic_badges_activity_rt, NOTIFICATION_RETWEET, isFollowed, isHighlighted);
    }

    public static NotificationDetailModel getFollowInstance(long id, String title, String subtitle,
                                                            String iconUrl, String text, boolean isFollowed,
                                                            boolean isHighlighted) {
        return new NotificationDetailModel(id, title, subtitle, iconUrl, text,
                App.getInstance().isNightEnabled() ? R.color.dark_profile_tint_color : R.color.light_profile_tint_color,
                R.drawable.ic_badges_activity_profile, NOTIFICATION_FOLLOW, isFollowed, isHighlighted);
    }

    public static NotificationDetailModel getListedInstance(long id, String title, String subtitle,
                                                            String iconUrl, String text, boolean isFollowed,
                                                            boolean isHighlighted) {
        return new NotificationDetailModel(id, title, subtitle, iconUrl, text,
                App.getInstance().isNightEnabled() ? R.color.dark_profile_tint_color : R.color.light_profile_tint_color,
                R.drawable.ic_badges_activity_list, NOTIFICATION_FOLLOW, isFollowed, isHighlighted);
    }

    public static NotificationDetailModel getQuotedInstance(long id, String title, String subtitle,
                                                            String iconUrl, String text, boolean isFollowed,
                                                            boolean isHighlighted) {
        return new NotificationDetailModel(id, title, subtitle, iconUrl, text,
                App.getInstance().isNightEnabled() ? R.color.dark_rt_tint_color : R.color.light_rt_tint_color,
                R.drawable.ic_badges_activity_quote, NOTIFICATION_QUOTED, isFollowed, isHighlighted);
    }

    public static NotificationDetailModel getMentionedInstance(long id, String title, String subtitle,
                                                            String iconUrl, String text, boolean isFollowed,
                                                            boolean isHighlighted) {
        return new NotificationDetailModel(id, title, subtitle, iconUrl, text,
                App.getInstance().isNightEnabled() ? R.color.dark_reply_tint_color : R.color.light_reply_tint_color,
                R.drawable.ic_badges_activity_reply, NOTIFICATION_MENTIONED, isFollowed, isHighlighted);
    }

    public NotificationDetailModel(long id, String title, String subtitle, String iconUrl,
                                   String text, int tintColor, int subIconResource, int type, boolean isFollowed,
                                   boolean isHighlighted) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.iconUrl = iconUrl;
        this.text = text;
        this.tintColor = tintColor;
        this.subIconResource = subIconResource;
        this.type = type;
        this.isFollowed = isFollowed;
        this.isHighlighted = isHighlighted;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NotificationDetailModel)) return false;
        if (obj == this) return true;

        NotificationDetailModel notificationModel = (NotificationDetailModel) obj;
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
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getSubtitle() {
        return subtitle;
    }
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        notifyPropertyChanged(BR.subtitle);
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
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
        notifyPropertyChanged(BR.text);
    }

    @Bindable
    public int getTintColor() {
        return tintColor;
    }
    public void setTintColor(int tintColor) {
        this.tintColor = tintColor;
        notifyPropertyChanged(BR.tintColor);
    }

    @Bindable
    public int getSubIconResource() {
        return subIconResource;
    }
    public void setSubIconResource(int subIconResource) {
        this.subIconResource = subIconResource;
        notifyPropertyChanged(BR.subIconResource);
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
    public boolean isHighlighted() {
        return isHighlighted;
    }

    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
        notifyPropertyChanged(BR.highlighted);
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
