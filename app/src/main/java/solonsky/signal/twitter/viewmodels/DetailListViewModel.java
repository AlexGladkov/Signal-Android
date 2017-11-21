package solonsky.signal.twitter.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

import solonsky.signal.twitter.helpers.ListConfig;

/**
 * Created by neura on 04.06.17.
 */

public class DetailListViewModel extends BaseObservable {
    private ListConfig listConfig;
    private int state;

    public DetailListViewModel(ListConfig listConfig) {
        this.listConfig = listConfig;
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
