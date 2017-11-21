package solonsky.signal.twitter.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 28.05.17.
 */

public class ProfileSettingsViewModel extends BaseObservable {
    private final String TAG = ProfileSettingsViewModel.class.getSimpleName();
    private String username;
    private String location;
    private String link;
    private String about;
    private String avatar;
    private String backdrop;
    private boolean isApply;
    private boolean isNewAvatar;
    private boolean isNewBanner;

    private String currentAbout;
    private String currentLink;
    private String currentLocation;
    private String currentName;

    public interface ProfileSettingsClickHandler {
        void onBackClick(View v);
        void onSaveClick(View v);
        void onAvatarClick(View v);
        void onBannerClick(View v);
    }

    public ProfileSettingsViewModel(String username, String location, String link, String about,
                                    String avatar, String backdrop) {
        this.username = username;
        this.location = location;
        this.link = link;
        this.about = about;
        this.currentName = username;
        this.currentLocation = location;
        this.currentLink = link;
        this.currentAbout = about;
        this.avatar = avatar;
        this.backdrop = backdrop;
        this.isApply = false;
        this.isNewAvatar = false;
        this.isNewBanner = false;
    }

    public void onNameChanged(CharSequence s, int start, int before, int count) {
        currentName = s.toString();
        setApply(checkApply());
    }

    public void onLocationChanged(CharSequence s, int start, int before, int count) {
        currentLocation = s.toString();
        this.isApply = checkApply();
    }

    public void onAboutChanged(CharSequence s, int start, int before, int count) {
        currentAbout = s.toString();
        this.isApply = checkApply();
    }

    public void onLinkChanged(CharSequence s, int start, int before, int count) {
        currentLink = s.toString();
        this.isApply = checkApply();
    }

    private boolean checkApply() {
        if (currentAbout.equals(about) || currentAbout.length() > 160) {
            if (currentName.equals(username) || currentName.length() > 20) {
                if (currentLocation.equals(location) || currentLocation.length() > 30) {
                    if (currentLink.equals(link) || currentLink.length() > 100) {
                        if (isNewBanner || isNewAvatar) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public boolean isNewAvatar() {
        return isNewAvatar;
    }
    public void setNewAvatar(boolean newAvatar) {
        isNewAvatar = newAvatar;
    }
    public boolean isNewBanner() {
        return isNewBanner;
    }
    public void setNewBanner(boolean newBanner) {
        isNewBanner = newBanner;
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
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
        notifyPropertyChanged(BR.location);
    }

    @Bindable
    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
        notifyPropertyChanged(BR.link);
    }

    @Bindable
    public String getAbout() {
        return about;
    }
    public void setAbout(String about) {
        this.about = about;
        notifyPropertyChanged(BR.about);
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
    public boolean isApply() {
        return isApply;
    }
    public void setApply(boolean apply) {
        isApply = apply;
        notifyPropertyChanged(BR.apply);
    }

    public String getBackdrop() {
        return backdrop;
    }

    public void setBackdrop(String backdrop) {
        this.backdrop = backdrop;
        notifyPropertyChanged(BR.backdrop);
    }

    public String getCurrentAbout() {
        return currentAbout;
    }

    public String getCurrentLink() {
        return currentLink;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public String getCurrentName() {
        return currentName;
    }
}
