package solonsky.signal.twitter.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

import solonsky.signal.twitter.adapters.SettingsAdapter;
import solonsky.signal.twitter.helpers.ListConfig;

/**
 * Created by neura on 24.05.17.
 */
public class GesturesViewModel extends BaseObservable {
    private ListConfig listConfig;

    public GesturesViewModel(SettingsAdapter mAdapter, Context context) {
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
