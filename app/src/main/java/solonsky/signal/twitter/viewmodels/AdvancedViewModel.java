package solonsky.signal.twitter.viewmodels;

import android.content.Context;
import android.view.View;


import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.adapters.SettingsAdapter;
import solonsky.signal.twitter.helpers.ListConfig;

/**
 * Created by neura on 24.05.17.
 */

public class AdvancedViewModel extends BaseObservable {
    private ListConfig listConfig;

    public interface AdvancedClickHandler {
        void onDefaultClick(View v);
        void onClearClick(View v);
        void onBackClick(View v);
        void onCacheClick(View v);
    }

    public AdvancedViewModel(SettingsAdapter mAdapter, Context context) {
        this.listConfig = new ListConfig.Builder(mAdapter)
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
}
