package solonsky.signal.twitter.viewmodels;

import android.databinding.BaseObservable;
import android.view.View;

/**
 * Created by neura on 16.06.17.
 */

public class LoginViewModel extends BaseObservable {

    public interface LoginClickHandler {
        void onLoginClick(View v);
    }
}
