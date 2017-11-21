package solonsky.signal.twitter.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

import solonsky.signal.twitter.adapters.NotificationsDetailAdapter;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.ListConfig;

/**
 * Created by neura on 25.05.17.
 */

public class NotificationsDetailViewModel extends BaseObservable {
    private ListConfig listConfig;
    private Context context;
    private int state;

    public NotificationsDetailViewModel(NotificationsDetailAdapter mAdapter, Context context) {
        this.context = context;
        this.state = AppData.UI_STATE_LOADING;

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

    @Bindable
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
        notifyPropertyChanged(BR.state);
    }
}
