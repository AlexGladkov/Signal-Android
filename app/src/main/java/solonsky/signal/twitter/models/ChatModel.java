package solonsky.signal.twitter.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

import java.util.ArrayList;

import solonsky.signal.twitter.helpers.Flags;

/**
 * Created by neura on 29.05.17.
 */

public class ChatModel extends BaseObservable {
    private long id;
    private long senderId;
    private int type;
    private String text;
    private String imageUrl;
    private String avatarUrl;
    private String time;
    private Flags.MEDIA_TYPE mediaType;
    private boolean showAvatar;
    private boolean showArrow;
    private boolean isAvatar;
    private ArrayList<String> shortUrls;
    private float alpha;

    public ChatModel(long id, long senderId, int type, String text, String imageUrl, String avatarUrl, String time,
                     boolean showAvatar, boolean showArrow) {
        this.id = id;
        this.senderId = senderId;
        this.type = type;
        this.text = text;
        this.time = time;
        this.imageUrl = imageUrl;
        this.avatarUrl = avatarUrl;
        this.showAvatar = showAvatar;
        this.showArrow = showArrow;
        this.mediaType = Flags.MEDIA_TYPE.IMAGE;
        this.shortUrls = new ArrayList<>();
        this.alpha = 0.3f;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        notifyPropertyChanged(BR.imageUrl);
    }

    @Bindable
    public boolean isShowAvatar() {
        return showAvatar;
    }

    public void setShowAvatar(boolean showAvatar) {
        this.showAvatar = showAvatar;
        notifyPropertyChanged(BR.showAvatar);
    }

    @Bindable
    public boolean isShowArrow() {
        return showArrow;
    }

    public void setShowArrow(boolean showArrow) {
        this.showArrow = showArrow;
        notifyPropertyChanged(BR.showArrow);
    }

    @Bindable
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
        notifyPropertyChanged(BR.type);
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
    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        notifyPropertyChanged(BR.alpha);
    }

    public Flags.MEDIA_TYPE getMediaType() {
        return mediaType;
    }

    public void setMediaType(Flags.MEDIA_TYPE mediaType) {
        this.mediaType = mediaType;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String[] getShortUrls() {
        String[] urlArray = new String[shortUrls.size()];
        for (int i = 0; i < shortUrls.size(); i++)
            urlArray[i] = shortUrls.get(i);
        return urlArray;
    }

    public void setShortUrls(ArrayList<String> shortUrls) {
        this.shortUrls = shortUrls;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }
}
