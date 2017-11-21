package solonsky.signal.twitter.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 24.05.17.
 */

public abstract class SettingsModel extends BaseObservable {
    private String title;
    private int type;

    public SettingsModel(String title, int type) {
        this.title = title;
        this.type = type;
    }

    @Bindable
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
}
