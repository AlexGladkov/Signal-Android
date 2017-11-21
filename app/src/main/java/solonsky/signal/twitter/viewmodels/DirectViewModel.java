package solonsky.signal.twitter.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

import solonsky.signal.twitter.adapters.DirectAdapter;
import solonsky.signal.twitter.helpers.ListConfig;

/**
 * Created by neura on 22.05.17.
 */

public class DirectViewModel extends BaseObservable {
    private ListConfig listConfig;
    private int state;

    public interface DirectClickHandler {

    }

    public DirectViewModel(DirectAdapter mAdapter, Context context, int state) {
        this.state = state;

        this.listConfig = new ListConfig.Builder(mAdapter)
                .setDefaultDividerEnabled(true)
                .setLayoutManagerProvider(new ListConfig.SimpleLinearLayoutManagerProvider())
                .setHasFixedSize(true)
                .setHasNestedScroll(true)
                .build(context);
    }

    @Bindable
    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
        notifyPropertyChanged(BR.state);
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
