package solonsky.signal.twitter.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 31.08.17.
 */

public class MediaViewModel extends BaseObservable {
    private String timeStart;
    private String timeEnd;
    private boolean isPlay;
    private boolean isOverlay;

    public interface MediaClickHandler {
        void onPlayClick(View v);
        void onRewindClick(View v);
        void onForwardClick(View v);
        void onPauseClick(View v);
        void onBackClick(View v);
        void onSaveClick(View v);
        void onShareClick(View v);
        void onChromeClick(View v);
        void onContentClick(View v);
    }

    public MediaViewModel(String timeStart, String timeEnd, boolean isPlay, boolean isOverlay) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.isPlay = isPlay;
        this.isOverlay = isOverlay;
    }

    @Bindable
    public String getTimeStart() {
        return timeStart;
    }
    public void setTimeStart(String timeStart) {
        this.timeStart = timeStart;
        notifyPropertyChanged(BR.timeStart);
    }

    @Bindable
    public String getTimeEnd() {
        return timeEnd;
    }
    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
        notifyPropertyChanged(BR.timeEnd);
    }

    @Bindable
    public boolean isPlay() {
        return isPlay;
    }
    public void setPlay(boolean play) {
        isPlay = play;
        notifyPropertyChanged(BR.play);
    }

    @Bindable
    public boolean isOverlay() {
        return isOverlay;
    }

    public void setOverlay(boolean overlay) {
        isOverlay = overlay;
        notifyPropertyChanged(BR.overlay);
    }
}
