package solonsky.signal.twitter.viewmodels;

import android.util.Log;
import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 29.05.17.
 */

public class MuteAddViewModel extends BaseObservable {
    private String title;
    private boolean isBottom;
    private boolean isAction;
    private boolean isList;
    private String hashtag;
    private String keyword;

    public interface MuteAddClickHandler {
        void onActionClick(View v);
    }

    public MuteAddViewModel(String title, boolean isBottom) {
        this.isBottom = isBottom;
        this.isAction = false;
        this.isList = true;
        this.title = title;
    }

    public void onUserChanged(CharSequence s, int start, int before, int count) {
        Log.e("User", s.toString());
    }

    public void onKeywordChanged(CharSequence s, int start, int before, int count) {
        setKeyword(s.toString());
    }

    public void onHashTagChanged(CharSequence s, int start, int before, int count) {
        setHashtag(s.toString());
    }

    public void onClientChanged(CharSequence s, int start, int before, int count) {
        
    }

    @Bindable
    public boolean isBottom() {
        return isBottom;
    }

    public void setBottom(boolean bottom) {
        isBottom = bottom;
        notifyPropertyChanged(BR.bottom);
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
    public boolean isAction() {
        return isAction;
    }

    public void setAction(boolean action) {
        isAction = action;
        notifyPropertyChanged(BR.action);
    }

    @Bindable
    public boolean isList() {
        return isList;
    }

    public void setList(boolean list) {
        isList = list;
        notifyPropertyChanged(BR.list);
    }

    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
