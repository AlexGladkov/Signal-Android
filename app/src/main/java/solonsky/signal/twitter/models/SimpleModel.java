package solonsky.signal.twitter.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 26.05.17.
 */

public class SimpleModel extends BaseObservable {
    private long id;
    private String title;

    public interface SimpleClickHandler {
        void onItemClick(View v);
    }

    public SimpleModel(long id, String title) {
        this.id = id;
        this.title = title;
    }

    @Bindable
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }

    @Bindable
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }
}
