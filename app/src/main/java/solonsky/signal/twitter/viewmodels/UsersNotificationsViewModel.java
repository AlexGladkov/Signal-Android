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

public class UsersNotificationsViewModel extends BaseObservable {
    private ListConfig listConfig;
    private ListConfig subListConfig;

    public UsersNotificationsViewModel(SettingsAdapter mAdapter, SettingsAdapter mSubAdapter, Context context) {
        this.listConfig = new ListConfig.Builder(mAdapter)
                .setDefaultDividerEnabled(true)
                .setHasFixedSize(true)
                .setHasNestedScroll(false)
                .build(context);

        this.subListConfig = new ListConfig.Builder(mSubAdapter)
                .setDefaultDividerEnabled(true)
                .setHasFixedSize(true)
                .setHasNestedScroll(false)
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

    @Bindable
    public ListConfig getSubListConfig() {
        return subListConfig;
    }
    public void setSubListConfig(ListConfig subListConfig) {
        this.subListConfig = subListConfig;
        notifyPropertyChanged(BR.subListConfig);
    }
}
