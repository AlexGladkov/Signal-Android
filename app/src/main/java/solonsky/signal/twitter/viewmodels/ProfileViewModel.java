package solonsky.signal.twitter.viewmodels;

import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 27.05.17.
 */

public class ProfileViewModel extends BaseObservable {
    private String backdrop;
    private String avatar;
    private String username;
    private String twitterName;
    private String selectorName;
    private String followers;
    private boolean isVerified;
    private boolean isLoading;
    private boolean homeUser;

    public interface ProfileClickHandler {
        void onBackClick(View v);
        void onSettingsClick(View v);
        void onMoreClick(View v);
        void onLinkClick(View v);
        void onMenuClick(View v);
        void onAvatarClick(View v);
        void onBackDropClick(View v);
    }

    public ProfileViewModel(String backdrop, String avatar, String username, String twitterName,
                            String followers, boolean isVerified) {
        this.backdrop = backdrop;
        this.avatar = avatar;
        this.username = username;
        this.twitterName = twitterName;
        this.followers = followers;
        this.selectorName = "Tweets";
        this.isLoading = true;
        this.isVerified = isVerified;
    }

    @Bindable
    public boolean isHomeUser() {
        return homeUser;
    }

    public void setHomeUser(boolean homeUser) {
        this.homeUser = homeUser;
        notifyPropertyChanged(BR. homeUser);
    }

    @Bindable
    public String getBackdrop() {
        return backdrop;
    }
    public void setBackdrop(String backdrop) {
        this.backdrop = backdrop;
        notifyPropertyChanged(BR.backdrop);
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
        notifyPropertyChanged(BR.avatar);
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
    public String getSelectorName() {
        return selectorName;
    }
    public void setSelectorName(String selectorName) {
        this.selectorName = selectorName;
        notifyPropertyChanged(BR.selectorName);
    }

    @Bindable
    public boolean isVerified() {
        return isVerified;
    }
    public void setVerified(boolean verified) {
        isVerified = verified;
        notifyPropertyChanged(BR.verified);
    }

    @Bindable
    public String getFollowers() {
        return followers;
    }
    public void setFollowers(String followers) {
        this.followers = followers;
        notifyPropertyChanged(BR.followers);
    }

    @Bindable
    public boolean isLoading() {
        return isLoading;
    }
    public void setLoading(boolean loading) {
        isLoading = loading;
        notifyPropertyChanged(BR.loading);
    }
}
