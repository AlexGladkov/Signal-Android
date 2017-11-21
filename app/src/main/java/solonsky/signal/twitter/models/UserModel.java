package solonsky.signal.twitter.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 24.05.17.
 */

public class UserModel extends BaseObservable {
    private final String TAG = UserModel.class.getSimpleName();
    private long id;
    private int avatarRes;
    private String avatarUrl;
    private String username;
    private String twitterName;
    private User user;
    private boolean isFollowed;
    private boolean isActive;
    private boolean isBullet;
    private boolean isEnabled;

    public interface UserClickHandler {
        void onItemClick(View v);
        void onFollowClick(View v);
    }

    public UserModel(long id, String avatarUrl, String username, String twitterName, boolean isFollowed,
                     boolean isActive, boolean isBullet) {
        this.id = id;
        this.avatarRes = 0;
        this.avatarUrl = avatarUrl;
        this.username = username;
        this.twitterName = twitterName;
        this.isFollowed = isFollowed;
        this.isActive = isActive;
        this.isBullet = isBullet;
        this.isEnabled = true;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserModel)) return false;
        if (obj == this) return true;

        UserModel userModel = (UserModel) obj;
        return this.id == userModel.getId() || TextUtils.equals(this.twitterName.toLowerCase(),
                userModel.getTwitterName().toLowerCase());
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
    public String getTwitterName() {
        return twitterName;
    }
    public void setTwitterName(String twitterName) {
        this.twitterName = twitterName;
        notifyPropertyChanged(BR.twitterName);
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
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        notifyPropertyChanged(BR.active);
    }

    @Bindable
    public boolean isBullet() {
        return isBullet;
    }

    public void setBullet(boolean bullet) {
        isBullet = bullet;
        notifyPropertyChanged(BR.bullet);
    }

    @Bindable
    public int getAvatarRes() {
        return avatarRes;
    }

    public void setAvatarRes(int avatarRes) {
        this.avatarRes = avatarRes;
        notifyPropertyChanged(BR.avatarRes);
    }

    @Bindable
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        notifyPropertyChanged(BR.enabled);
    }
}
