package solonsky.signal.twitter.models;

import android.databinding.Bindable;
import android.view.View;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.helpers.AppData;

/**
 * Created by neura on 24.05.17.
 */

public class SettingsSwitchModel extends SettingsModel {
    private boolean isOn;
    private int id;

    public interface SwitchClickHandler {
        void onSwitcherClick(View v, boolean isOn);
    }

    public SettingsSwitchModel(int id, String title, boolean isOn) {
        super(title, AppData.SETTINGS_TYPE_SWITCH);
        this.isOn = isOn;
        this.id = id;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    @Bindable
    public boolean isOn() {
        return isOn;
    }
    public void setOn(boolean on) {
        isOn = on;
        notifyPropertyChanged(BR.on);
    }

    @Override
    @Bindable
    public String getTitle() {
        return super.getTitle();
    }
}
