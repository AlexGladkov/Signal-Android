package solonsky.signal.twitter.viewmodels;

import android.databinding.BaseObservable;
import android.view.View;

/**
 * Created by neura on 23.05.17.
 */

public class SettingsViewModel extends BaseObservable {

    public interface SettingsClickHandler {
        void onAppearanceClick(View v);
        void onTimelineClick(View v);
        void onGesturesClick(View v);
        void onNotificationsClick(View v);
        void onAdvancedClick(View v);
        void onHelpClick(View v);
        void onAboutClick(View v);
        void onSupportClick(View v);
        void onBackClick(View v);
    }
}
