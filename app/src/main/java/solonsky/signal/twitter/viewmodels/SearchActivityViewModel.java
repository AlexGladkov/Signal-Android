package solonsky.signal.twitter.viewmodels;

import android.view.View;

import androidx.databinding.BaseObservable;

/**
 * Created by neura on 13.10.17.
 */

public class SearchActivityViewModel extends BaseObservable {

    public interface SearchActivityClickHandler {
        void onBackClick(View v);
    }
}
