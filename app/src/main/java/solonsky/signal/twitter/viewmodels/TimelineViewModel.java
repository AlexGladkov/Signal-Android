package solonsky.signal.twitter.viewmodels;

import android.content.Context;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.adapters.SettingsAdapter;
import solonsky.signal.twitter.helpers.ListConfig;

/**
 * Created by neura on 24.05.17.
 */

public class TimelineViewModel extends BaseObservable {
    private ListConfig listConfig;

    public TimelineViewModel(SettingsAdapter mAdapter, Context context) {
        this.listConfig = new ListConfig.Builder(mAdapter)
                .setDefaultDividerEnabled(true)
                .setHasFixedSize(true)
                .setHasNestedScroll(true)
                .build(context);
    }

    @Bindable
    public ListConfig getListConfig() {
        return listConfig;
    }
    public void setListConfig(ListConfig listConfig) {
        this.listConfig = listConfig;
        notifyPropertyChanged(BR.listConfig);
    }
}
