package solonsky.signal.twitter.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 24.05.17.
 */

public class AboutViewModel extends BaseObservable {
    private Context mContext;
    private String version;

    public AboutViewModel(Context mContext, String version) {
        this.mContext = mContext;
        this.version = version;
    }

    @Bindable
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
        notifyPropertyChanged(BR.version);
    }
}
