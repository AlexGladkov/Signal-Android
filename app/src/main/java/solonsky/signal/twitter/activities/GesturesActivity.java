package solonsky.signal.twitter.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.SettingsAdapter;
import solonsky.signal.twitter.databinding.ActivityGesturesBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.FileNames;
import solonsky.signal.twitter.helpers.FileWork;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.ConfigurationModel;
import solonsky.signal.twitter.models.SettingsModel;
import solonsky.signal.twitter.models.SettingsSwitchModel;
import solonsky.signal.twitter.models.SettingsTextModel;
import solonsky.signal.twitter.viewmodels.GesturesViewModel;

/**
 * Created by neura on 23.05.17.
 */

public class GesturesActivity extends AppCompatActivity {
    private ArrayList<SettingsModel> mSettingsList;
    private SettingsAdapter mAdapter;
    private GesturesActivity mActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(getResources().getColor(App.getInstance().isNightEnabled() ?
                android.R.color.transparent : R.color.light_status_bar_timeline_color));

        ActivityGesturesBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_gestures);
        binding.imgGesturesBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mActivity = this;
        mSettingsList = new ArrayList<>();

        String shortGesture = AppData.appConfiguration.getShortTap() == ConfigurationModel.TAP_SHOW_ACTIONS ?
                getString(R.string.gestures_show) : AppData.appConfiguration.getShortTap() == ConfigurationModel.TAP_VIEW_DETAILS ?
                getString(R.string.gestures_details) : AppData.appConfiguration.getShortTap() == ConfigurationModel.TAP_OPEN_MEDIA ?
                getString(R.string.gestures_media) : getString(R.string.gestures_link);

        String longGesture = AppData.appConfiguration.getLongTap() == ConfigurationModel.TAP_LAST_SHARING ?
                getString(R.string.gestures_last) : AppData.appConfiguration.getLongTap() == ConfigurationModel.TAP_READ_LATER ?
                getString(R.string.gestures_later) : AppData.appConfiguration.getLongTap() == ConfigurationModel.TAP_TRANSLATE ?
                getString(R.string.gestures_translate) : getString(R.string.gestures_share);

        String doubleGesture = AppData.appConfiguration.getDoubleTap() == ConfigurationModel.TAP_REPLY ?
                getString(R.string.gestures_reply) : AppData.appConfiguration.getDoubleTap() == ConfigurationModel.TAP_QUOTE ?
                getString(R.string.gestures_quote) : AppData.appConfiguration.getDoubleTap() == ConfigurationModel.TAP_RETWEET ?
                getString(R.string.gestures_retweet) : getString(R.string.gestures_like);

        mSettingsList.add(new SettingsTextModel(getString(R.string.settings_gestures_short), shortGesture));
        mSettingsList.add(new SettingsTextModel(getString(R.string.settings_gestures_long), longGesture));
        mSettingsList.add(new SettingsTextModel(getString(R.string.settings_gestures_double), doubleGesture));

        mAdapter = new SettingsAdapter(mSettingsList, getApplicationContext(), textClickListener, true);

        GesturesViewModel viewModel = new GesturesViewModel(mAdapter, getApplicationContext());
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
            PopupMenu popupMenu = new PopupMenu(mActivity, v, 0, 0, R.style.popup_menu);
            MenuInflater menuInflater = popupMenu.getMenuInflater();

            if (model.getTitle().equalsIgnoreCase(getString(R.string.settings_gestures_short))) {
                menuInflater.inflate(R.menu.menu_gestures_single, popupMenu.getMenu());
            } else if (model.getTitle().equalsIgnoreCase(getString(R.string.settings_gestures_long))) {
                menuInflater.inflate(R.menu.menu_gestures_long, popupMenu.getMenu());
            } else if (model.getTitle().equalsIgnoreCase(getString(R.string.settings_gestures_double))) {
                menuInflater.inflate(R.menu.menu_gestures_double, popupMenu.getMenu());
            }

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.gestures_last:
                            model.setSubtitle(getString(R.string.gestures_last));
                            AppData.appConfiguration.setLongTap(ConfigurationModel.TAP_LAST_SHARING);
                            break;

                        case R.id.gestures_reply:
                            model.setSubtitle(getString(R.string.gestures_reply));
                            AppData.appConfiguration.setDoubleTap(ConfigurationModel.TAP_REPLY);
                            break;

                        case R.id.gestures_show:
                            model.setSubtitle(getString(R.string.gestures_show));
                            AppData.appConfiguration.setShortTap(ConfigurationModel.TAP_SHOW_ACTIONS);
                            break;

                        case R.id.gestures_details:
                            model.setSubtitle(getString(R.string.gestures_details));
                            AppData.appConfiguration.setShortTap(ConfigurationModel.TAP_VIEW_DETAILS);
                            break;

                        case R.id.gestures_later:
                            model.setSubtitle(getString(R.string.gestures_later));
                            AppData.appConfiguration.setLongTap(ConfigurationModel.TAP_READ_LATER);
                            break;

                        case R.id.gestures_like:
                            model.setSubtitle(getString(R.string.gestures_like));
                            AppData.appConfiguration.setDoubleTap(ConfigurationModel.TAP_LIKE);
                            break;

                        case R.id.gestures_link:
                            model.setSubtitle(getString(R.string.gestures_link));
                            AppData.appConfiguration.setShortTap(ConfigurationModel.TAP_GO_TO_LINK);
                            break;

                        case R.id.gestures_media:
                            model.setSubtitle(getString(R.string.gestures_media));
                            AppData.appConfiguration.setShortTap(ConfigurationModel.TAP_OPEN_MEDIA);
                            break;

                        case R.id.gestures_quote:
                            model.setSubtitle(getString(R.string.gestures_quote));
                            AppData.appConfiguration.setDoubleTap(ConfigurationModel.TAP_QUOTE);
                            break;

                        case R.id.gestures_retweet:
                            model.setSubtitle(getString(R.string.gestures_retweet));
                            AppData.appConfiguration.setDoubleTap(ConfigurationModel.TAP_RETWEET);
                            break;

                        case R.id.gestures_share:
                            model.setSubtitle(getString(R.string.gestures_share));
                            AppData.appConfiguration.setLongTap(ConfigurationModel.TAP_SHARE);
                            break;

                        case R.id.gestures_translate:
                            model.setSubtitle(getString(R.string.gestures_translate));
                            AppData.appConfiguration.setLongTap(ConfigurationModel.TAP_TRANSLATE);
                            break;
                    }

                    new FileWork(getApplicationContext()).writeToFile(
                            AppData.appConfiguration.exportConfiguration().toString(), FileNames.APP_CONFIGURATION);
                    return false;
                }
            });

            popupMenu.show();
        }

        @Override
        public void onSwitchClick(SettingsSwitchModel model, View v) {

        }
    };
}
