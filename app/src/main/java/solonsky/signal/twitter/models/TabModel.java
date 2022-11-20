package solonsky.signal.twitter.models;


import androidx.fragment.app.Fragment;

/**
 * Created by neura on 25.05.17.
 */

public class TabModel {
    private Fragment fragment;
    private String title;
    private int textColorResource;
    private int iconResource;
    private int colorResource;
    private int tintResource;

    public TabModel(Fragment fragment, String title, int textColorResource, int iconResource,
                    int colorResource, int tintResource) {
        this.fragment = fragment;
        this.title = title;
        this.textColorResource = textColorResource;
        this.iconResource = iconResource;
        this.colorResource = colorResource;
        this.tintResource = tintResource;
    }

    public Fragment getFragment() {
        return fragment;
    }
    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getTextColorResource() {
        return textColorResource;
    }
    public void setTextColorResource(int textColorResource) {
        this.textColorResource = textColorResource;
    }
    public int getIconResource() {
        return iconResource;
    }
    public void setIconResource(int iconResource) {
        this.iconResource = iconResource;
    }
    public int getColorResource() {
        return colorResource;
    }
    public void setColorResource(int colorResource) {
        this.colorResource = colorResource;
    }
    public int getTintResource() {
        return tintResource;
    }
    public void setTintResource(int tintResource) {
        this.tintResource = tintResource;
    }
}
