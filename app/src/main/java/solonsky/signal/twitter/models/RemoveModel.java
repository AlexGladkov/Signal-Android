package solonsky.signal.twitter.models;

import android.text.TextUtils;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 26.05.17.
 */

public class RemoveModel extends BaseObservable {
    private long id;
    private String title;

    public interface RemoveClickHandler {
        void onItemClick(View view);
        void onMinusClick(View view);
    }

    public RemoveModel(long id, String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RemoveModel)) return false;
        if (obj == this) return true;

        RemoveModel removeModel = (RemoveModel) obj;
        return TextUtils.equals(this.title.toLowerCase(), removeModel.getTitle().toLowerCase());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
