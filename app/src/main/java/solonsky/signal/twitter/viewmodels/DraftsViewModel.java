package solonsky.signal.twitter.viewmodels;


import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;

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
