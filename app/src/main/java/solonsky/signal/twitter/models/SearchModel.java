package solonsky.signal.twitter.models;

import android.text.TextUtils;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 26.05.17.
 */

public class SearchModel extends BaseObservable {
    private long id;
    private int count;
    private String title;
    private boolean isHighlighted;

    public interface SearchClickHandler {
        void onItemClick(View v);
        void onCloseClick(View v);
    }

    public SearchModel(long id, int count, String title, boolean isHighlighted) {
        this.id = id;
        this.count = count;
        this.title = title;
        this.isHighlighted = isHighlighted;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SearchModel)) return false;
        if (obj == this) return true;

        SearchModel searchModel = (SearchModel) obj;
        return TextUtils.equals(this.getTitle().toLowerCase(), searchModel.getTitle().toLowerCase());
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    @Bindable
    public int getCount() {
        return count;
    }
    public void setCount(int count) {
        this.count = count;
        notifyPropertyChanged(BR.count);
    }

    @Bindable
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public boolean isHighlighted() {
        return isHighlighted;
    }
    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
        notifyPropertyChanged(BR.highlighted);
    }
}
