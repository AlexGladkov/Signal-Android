package solonsky.signal.twitter.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;

import java.io.IOException;
import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.SettingsAdapter;
import solonsky.signal.twitter.databinding.ActivityAdvancedBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.FileNames;
import solonsky.signal.twitter.helpers.FileWork;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.ConfigurationModel;
import solonsky.signal.twitter.models.SettingsModel;
import solonsky.signal.twitter.models.SettingsSwitchModel;
import solonsky.signal.twitter.models.SettingsTextModel;
import solonsky.signal.twitter.viewmodels.AdvancedViewModel;

/**
 * Created by neura on 23.05.17.
 */

public class AdvancedActivity extends AppCompatActivity {
    private final String TAG = AdvancedActivity.class.getSimpleName();
    private ArrayList<SettingsModel> mSettingsList;
    private SettingsAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        ActivityAdvancedBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_advanced);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(getResources().getColor(App.getInstance().isNightEnabled() ?
                android.R.color.transparent : R.color.light_status_bar_timeline_color));

        mSettingsList = new ArrayList<>();

//        mSettingsList.add(new SettingsSwitchModel(0, getString(R.string.settings_advanced_dim_media), AppData.appConfiguration.isDimMediaAtNight()));
//        mSettingsList.add(new SettingsSwitchModel(1, getString(R.string.settings_advanced_group_push), AppData.appConfiguration.isGroupPushNotifications()));
//        mSettingsList.add(new SettingsSwitchModel(2, getString(R.string.settings_advanced_pin_top), AppData.appConfiguration.isPinToTopOnStreaming()));
        mSettingsList.add(new SettingsSwitchModel(3, getString(R.string.settings_advanced_sounds), AppData.appConfiguration.isSounds()));

        mAdapter = new SettingsAdapter(mSettingsList, getApplicationContext(), textClickListener, true);

        AdvancedViewModel viewModel = new AdvancedViewModel(mAdapter, getApplicationContext());
        binding.setModel(viewModel);
        binding.setClick(new AdvancedViewModel.AdvancedClickHandler() {
            @Override
            public void onDefaultClick(View v) {
                AppData.appConfiguration = ConfigurationModel.defaultSettings();
                new FileWork(getApplicationContext()).writeToFile(AppData.appConfiguration.
                        exportConfiguration().toString(), FileNames.APP_CONFIGURATION);
                Toast.makeText(getApplicationContext(), getString(R.string.success_reseted), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClearClick(View v) {

            }

            @Override
            public void onBackClick(View v) {
                onBackPressed();
            }

            @Override
            public void onCacheClick(View v) {
                try {
                    Reservoir.clear();
                    FileWork fileWork = new FileWork(getApplicationContext());
                    fileWork.writeToFile("", FileNames.CLIENT_SECRET);
                    fileWork.writeToFile("", FileNames.CLIENT_TOKEN);
                    Toast.makeText(getApplicationContext(), R.string.success_cache_clear, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Log.e(TAG, "Error clearing cache " + e.getLocalizedMessage());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }

    private SettingsAdapter.SettingsClickListener textClickListener = new SettingsAdapter.SettingsClickListener() {
        @Override
        public void onItemClick(SettingsTextModel model, View v) {

        }

        @Override
        public void onSwitchClick(SettingsSwitchModel model, View v) {

        }
    };
}
