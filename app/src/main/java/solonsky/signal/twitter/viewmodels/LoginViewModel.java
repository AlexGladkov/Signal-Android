package solonsky.signal.twitter.viewmodels;

import android.view.View;

import androidx.databinding.BaseObservable;

/**
 * Created by neura on 16.06.17.
 */

public class LoginViewModel extends BaseObservable {

    public interface LoginClickHandler {
        void onLoginClick(View v);
    }
}
