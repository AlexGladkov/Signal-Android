package solonsky.signal.twitter.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.anupcowkur.reservoir.Reservoir;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.SettingsAdapter;
import solonsky.signal.twitter.databinding.ActivityUsersNotificationBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.ConfigurationUserModel;
import solonsky.signal.twitter.models.SettingsModel;
import solonsky.signal.twitter.models.SettingsSwitchModel;
import solonsky.signal.twitter.models.SettingsTextModel;
import solonsky.signal.twitter.presenters.ConfigurationsPresenter;
import solonsky.signal.twitter.viewmodels.UsersNotificationsViewModel;
import solonsky.signal.twitter.views.ConfigurationView;

/**
 * Created by neura on 24.05.17.
 */

public class UsersNotificationActivity extends MvpAppCompatActivity implements ConfigurationView {
    private final String TAG = UsersNotificationActivity.class.getSimpleName();
    private ArrayList<SettingsModel> mSettingsList;
    private ArrayList<SettingsModel> mSubSettingsList;
    private SettingsAdapter mAdapter;
    private SettingsAdapter mSubAdapter;
    private UsersNotificationActivity mActivity;

    @InjectPresenter
    ConfigurationsPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        ActivityUsersNotificationBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_users_notification);
        binding.imgUserNotificationBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(getResources().getColor(App.getInstance().isNightEnabled() ?
                android.R.color.transparent : R.color.light_status_bar_timeline_color));

        mActivity = this;
        binding.txtUsersNotification.setText(AppData.NOTIFICATIONS_CURRENT_USER.getUsername());

        mSettingsList = new ArrayList<>();
        mSubSettingsList = new ArrayList<>();

        mSettingsList.add(new SettingsTextModel(getString(R.string.settings_users_notifications_mentions),
                AppData.userConfiguration.getMentions() == null ?
                        ConfigurationUserModel.Mentions.FROM_ALL.toString() : AppData.userConfiguration.getMentions().toString()));
        mSettingsList.add(new SettingsSwitchModel(1, getString(R.string.settings_users_notifications_messages), AppData.userConfiguration.isMessages()));
        mSettingsList.add(new SettingsSwitchModel(2, getString(R.string.settings_users_notifications_likes), AppData.userConfiguration.isLikes()));
        mSettingsList.add(new SettingsSwitchModel(3, getString(R.string.settings_users_notifications_retweets), AppData.userConfiguration.isRetweets()));
//        mSettingsList.add(new SettingsSwitchModel(4, getString(R.string.settings_users_notifications_quotes), AppData.userConfiguration.isQuotes()));
//        mSettingsList.add(new SettingsSwitchModel(5, getString(R.string.settings_users_notifications_followers), AppData.userConfiguration.isFollowers()));
//        mSettingsList.add(new SettingsSwitchModel(6, getString(R.string.settings_users_notifications_lists), AppData.userConfiguration.isLists()));

        mSubSettingsList.add(new SettingsSwitchModel(7, getString(R.string.settings_users_notifications_sound), AppData.userConfiguration.isSound()));
        mSubSettingsList.add(new SettingsSwitchModel(8, getString(R.string.settings_users_notifications_vibration), AppData.userConfiguration.isVibration()));

        mAdapter = new SettingsAdapter(mSettingsList, getApplicationContext(), textClickListener, false);
        mSubAdapter = new SettingsAdapter(mSubSettingsList, getApplicationContext(), textClickListener, true);

        UsersNotificationsViewModel viewModel = new UsersNotificationsViewModel(mAdapter, mSubAdapter, getApplicationContext());
        binding.setModel(viewModel);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }

    private SettingsAdapter.SettingsClickListener textClickListener = new SettingsAdapter.SettingsClickListener() {
        @Override
        public void onItemClick(final SettingsTextModel model, View v) {
            final int position = AppData.configurationUserModels.indexOf(AppData.userConfiguration);
            PopupMenu popupMenu = new PopupMenu(mActivity, v, 0, 0, R.style.popup_menu);
            MenuInflater menuInflater = popupMenu.getMenuInflater();
            menuInflater.inflate(R.menu.menu_notifications_users, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.notifications_all:
                            model.setSubtitle(getString(R.string.notifications_all));
//                            AppData.configurationUserModels.get(position).setMentions(ConfigurationUserModel.Mentions.FROM_ALL);
                            AppData.userConfiguration.setMentions(ConfigurationUserModel.Mentions.FROM_ALL);
                            break;

                        case R.id.notifications_follow:
                            model.setSubtitle(getString(R.string.notifications_follow));
//                            AppData.configurationUserModels.get(position).setMentions(ConfigurationUserModel.Mentions.FROM_FOLLOW);
                            AppData.userConfiguration.setMentions(ConfigurationUserModel.Mentions.FROM_FOLLOW);
                            break;

                        case R.id.notifications_off:
                            model.setSubtitle(getString(R.string.notifications_off));
//                            AppData.configurationUserModels.get(position).setMentions(ConfigurationUserModel.Mentions.OFF);
                            AppData.userConfiguration.setMentions(ConfigurationUserModel.Mentions.OFF);
                            break;
                    }

                    presenter.updateUserConfiguration(AppData.userConfiguration);
                    return false;
                }
            });

            popupMenu.show();
        }

        @Override
        public void onSwitchClick(SettingsSwitchModel model, View v) {
            int position = AppData.configurationUserModels.indexOf(AppData.userConfiguration);
            switch (model.getId()) {
                case 0:
                    break;

                case 1:
//                    AppData.configurationUserModels.get(position).setMessages(model.isOn());
                    AppData.userConfiguration.setMessages(model.isOn());
                    break;

                case 2:
//                    AppData.configurationUserModels.get(position).setLikes(model.isOn());
                    AppData.userConfiguration.setLikes(model.isOn());
                    break;

                case 3:
//                    AppData.configurationUserModels.get(position).setRetweets(model.isOn());
                    AppData.userConfiguration.setRetweets(model.isOn());
                    break;

                case 4:
//                    AppData.configurationUserModels.get(position).setQuotes(model.isOn());
                    AppData.userConfiguration.setQuotes(model.isOn());
                    break;

                case 5:
//                    AppData.configurationUserModels.get(position).setFollowers(model.isOn());
                    AppData.userConfiguration.setFollowers(model.isOn());
                    break;

                case 6:
//                    AppData.configurationUserModels.get(position).setLists(model.isOn());
                    AppData.userConfiguration.setLists(model.isOn());
                    break;

                case 7:
//                    AppData.configurationUserModels.get(position).setSound(model.isOn());
                    AppData.userConfiguration.setSound(model.isOn());
                    break;

                case 8:
//                    AppData.configurationUserModels.get(position).setVibration(model.isOn());
                    AppData.userConfiguration.setVibration(model.isOn());
                    break;
            }

            presenter.updateUserConfiguration(AppData.userConfiguration);
            try {
                Reservoir.put(Cache.UsersConfigurations, AppData.configurationUserModels);
            } catch (IOException e) {
                Log.e(TAG, "Error updating cache " + e.getLocalizedMessage());
            }
        }
    };

    // MARK: - View implementation
    @Override
    public void settingsUpdated() {

    }
}
