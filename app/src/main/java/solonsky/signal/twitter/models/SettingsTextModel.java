package solonsky.signal.twitter.models;

import android.view.View;

import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.helpers.AppData;

/**
 * Created by neura on 24.05.17.
 */

public class SettingsTextModel extends SettingsModel {
    private String subtitle;

    public interface SettingsClickHandler {
        void onItemClick(View v);
    }

    public SettingsTextModel(String title, String subtitle) {
        super(title, AppData.SETTINGS_TYPE_TEXT);
        this.subtitle = subtitle;
    }

    @Bindable
    public String getSubtitle() {
        return subtitle;
    }
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        notifyPropertyChanged(BR.subtitle);
    }

    @Override
    @Bindable
    public String getTitle() {
        return super.getTitle();
    }
}
