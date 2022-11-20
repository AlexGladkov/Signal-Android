package solonsky.signal.twitter.viewmodels;


import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 27.05.17.
 */

public class ProfileInfoViewModel extends BaseObservable {
    private String bio;
    private String link;
    private String location;

    public ProfileInfoViewModel(String bio, String link, String location) {
        this.bio = bio;
        this.link = link;
        this.location = location;
    }

    @Bindable
    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
        notifyPropertyChanged(BR.bio);
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
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        notifyPropertyChanged(BR.location);
    }
}
