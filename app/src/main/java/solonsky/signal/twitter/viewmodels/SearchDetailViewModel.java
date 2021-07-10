package solonsky.signal.twitter.viewmodels;


import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.ListConfig;

/**
 * Created by neura on 03.06.17.
 */

public class SearchDetailViewModel extends BaseObservable {
    private ListConfig listConfig;
    private int state;

    public SearchDetailViewModel(ListConfig listConfig) {
        this.listConfig = listConfig;
        this.state = AppData.UI_STATE_VISIBLE;
    }

    @Bindable
    public ListConfig getListConfig() {
        return listConfig;
    }
    public void setListConfig(ListConfig listConfig) {
        this.listConfig = listConfig;
        notifyPropertyChanged(BR.listConfig);
    }

    @Bindable
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
        notifyPropertyChanged(BR.state);
    }
}
