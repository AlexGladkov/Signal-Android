package solonsky.signal.twitter.models;

import android.view.View;

/**
 * Created by neura on 29.05.17.
 */

public class DialogModel {
    private int id;
    private String title;
    private View.OnClickListener onClickListener;

    public DialogModel(int id, String title, View.OnClickListener onClickListener) {
        this.id = id;
        this.title = title;
        this.onClickListener = onClickListener;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
