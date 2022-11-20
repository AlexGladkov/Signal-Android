package solonsky.signal.twitter.viewmodels;

import android.content.Context;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.adapters.UserAdapter;
import solonsky.signal.twitter.adapters.UserDetailAdapter;
import solonsky.signal.twitter.helpers.ListConfig;

/**
 * Created by neura on 29.05.17.
 */

public class ChatSelectViewModel extends BaseObservable {
    private ListConfig listConfig;

    public ChatSelectViewModel(UserDetailAdapter mAdapter, Context mContext) {

        this.listConfig = new ListConfig.Builder(mAdapter)
                .setHasFixedSize(false)
                .setHasNestedScroll(true)
                .setDefaultDividerEnabled(true)
                .build(mContext);
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
