package solonsky.signal.twitter.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;

import com.android.databinding.library.baseAdapters.BR;

import solonsky.signal.twitter.adapters.DetailStaggeredAdapter;
import solonsky.signal.twitter.adapters.ImageAdapter;
import solonsky.signal.twitter.helpers.ListConfig;

/**
 * Created by neura on 27.05.17.
 */

public class ProfileMediaViewModel extends BaseObservable {
    private ListConfig listConfig;
    private int listHeight;
    private int state;

    public ProfileMediaViewModel(Context context) {
        ListConfig.SimpleGridLayoutManagerProvider simpleGridLayoutManagerProvider =
                new ListConfig.SimpleGridLayoutManagerProvider(2);

//        this.listConfig = new ListConfig.Builder(mAdapter)
//                .setDefaultDividerEnabled(true)
//                .setHasNestedScroll(false)
//                .setHasFixedSize(false)
//                .setLayoutManagerProvider(simpleGridLayoutManagerProvider)
//                .build(context);
    }

    @Bindable
    public int getListHeight() {
        return listHeight;
    }

    public void setListHeight(int listHeight) {
        this.listHeight = listHeight;
        notifyPropertyChanged(BR.listHeight);
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
