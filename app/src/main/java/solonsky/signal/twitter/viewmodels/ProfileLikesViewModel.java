package solonsky.signal.twitter.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.ListConfig;

/**
 * Created by neura on 27.05.17.
 */

public class ProfileLikesViewModel extends BaseObservable {
    private int state;
    private ListConfig listConfig;

    public ProfileLikesViewModel(StatusAdapter mAdapter, Context context) {
        this.listConfig = new ListConfig.Builder(mAdapter)
                .setDefaultDividerEnabled(true)
                .setHasNestedScroll(false)
                .setHasFixedSize(true)
                .build(context);

        this.state = AppData.UI_STATE_VISIBLE ;
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
