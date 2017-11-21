package solonsky.signal.twitter.helpers.drafts;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import com.android.databinding.library.baseAdapters.BR;

import java.util.ArrayList;

/**
 * Created by kmoaz on 30.08.2017.
 */

public class DraftModel extends BaseObservable {

    public String id;
    public String message;
    public ArrayList<String> urls;

    public interface DraftClickHandler {
        void onItemClick(View view);
        void onMinusClick(View view);
    }

    public DraftModel(String id, String message, ArrayList<String> urls) {
        this.id = id;
        this.message = message;
        this.urls = urls;
    }

    @Bindable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }

    @Bindable
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        notifyPropertyChanged(BR.message);
    }

    @Bindable
    public ArrayList<String> getUrls() {
        return urls;
    }

    public void setUrls(ArrayList<String> urls) {
        this.urls = urls;
        notifyPropertyChanged(BR.urls);
    }
}
