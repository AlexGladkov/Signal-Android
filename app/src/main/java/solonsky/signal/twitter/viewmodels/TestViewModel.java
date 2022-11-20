package solonsky.signal.twitter.viewmodels;


import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.util.ArrayList;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.helpers.ListConfig;
import solonsky.signal.twitter.models.StatusModel;

/**
 * Created by neura on 04.09.17.
 */

public class TestViewModel extends BaseObservable {
    private ArrayList<StatusModel> threadSource;
    private StatusAdapter threadAdapter;
    private ListConfig threadConfig;

    @Bindable
    public ArrayList<StatusModel> getThreadSource() {
        return threadSource;
    }

    public void setThreadSource(ArrayList<StatusModel> threadSource) {
        this.threadSource = threadSource;
        notifyPropertyChanged(BR.threadSource);
    }

    @Bindable
    public StatusAdapter getThreadAdapter() {
        return threadAdapter;
    }

    public void setThreadAdapter(StatusAdapter threadAdapter) {
        this.threadAdapter = threadAdapter;
    }

    public ListConfig getThreadConfig() {
        return threadConfig;
    }

    public void setThreadConfig(ListConfig threadConfig) {
        this.threadConfig = threadConfig;
    }
}
