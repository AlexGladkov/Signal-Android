package solonsky.signal.twitter.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

/**
 * Created by neura on 22.05.17.
 */

public class DraftsViewModel extends BaseObservable {
    private int state;

    public DraftsViewModel(int state) {
        this.state = state;
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
