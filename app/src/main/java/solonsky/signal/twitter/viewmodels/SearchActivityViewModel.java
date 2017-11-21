package solonsky.signal.twitter.viewmodels;

import android.databinding.BaseObservable;
import android.view.View;

/**
 * Created by neura on 13.10.17.
 */

public class SearchActivityViewModel extends BaseObservable {

    public interface SearchActivityClickHandler {
        void onBackClick(View v);
    }
}
