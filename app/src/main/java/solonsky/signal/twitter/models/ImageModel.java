package solonsky.signal.twitter.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.helpers.Flags;

/**
 * Created by neura on 28.05.17.
 */

public class ImageModel extends BaseObservable {
    private String imageUrl;
    private String previewUrl = "";
    private Flags.MEDIA_TYPE mediaType;
    private boolean isSelected;
    private int selectNumber;

    public interface ImageClickHandler {
        void onItemClick(View v);
        boolean onLongItemClick(View v);
    }

    public ImageModel(String imageUrl) {
        this.imageUrl = imageUrl;
        this.isSelected = false;
        this.selectNumber = 1;
        this.mediaType = Flags.MEDIA_TYPE.IMAGE;
    }

    @Bindable
    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
        notifyPropertyChanged(BR.previewUrl);
    }

    @Bindable
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        notifyPropertyChanged(BR.imageUrl);
    }

    @Bindable
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        notifyPropertyChanged(BR.selected);
    }

    @Bindable
    public int getSelectNumber() {
        return selectNumber;
    }

    public void setSelectNumber(int selectNumber) {
        this.selectNumber = selectNumber;
        notifyPropertyChanged(BR.selectNumber);
    }

    @Bindable
    public Flags.MEDIA_TYPE getMediaType() {
        return mediaType;
    }

    public void setMediaType(Flags.MEDIA_TYPE mediaType) {
        this.mediaType = mediaType;
        notifyPropertyChanged(BR.mediaType);
    }
}
