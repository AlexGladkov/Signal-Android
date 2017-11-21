package solonsky.signal.twitter.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.SettingsAdapter;
import solonsky.signal.twitter.data.FeedData;
import solonsky.signal.twitter.databinding.ActivityTimelineBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.SettingsModel;
import solonsky.signal.twitter.models.SettingsSwitchModel;
import solonsky.signal.twitter.models.SettingsTextModel;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.viewmodels.TimelineViewModel;

/**
 * Created by neura on 23.05.17.
 */

public class TimelineActivity extends AppCompatActivity {
    private final String TAG = TimelineActivity.class.getSimpleName();
    private ArrayList<SettingsModel> mSettingsList;
    private SettingsAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        ActivityTimelineBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_timeline);
        binding.imgTimelineBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(getResources().getColor(App.getInstance().isNightEnabled() ?
                android.R.color.transparent : R.color.light_status_bar_timeline_color));

        mSettingsList = new ArrayList<>();

        mSettingsList.add(new SettingsSwitchModel(0, getString(R.string.settings_timeline_static_top), AppData.appConfiguration.isStaticTopBars()));
        mSettingsList.add(new SettingsSwitchModel(1, getString(R.string.settimgs_timeline_static_bottom), AppData.appConfiguration.isStaticBottomBar()));
//        mSettingsList.add(new SettingsSwitchModel(2, getString(R.string.settings_timeline_static_group_dialog), AppData.appConfiguration.isGroupDialogs()));
        mSettingsList.add(new SettingsSwitchModel(3, getString(R.string.settings_timeline_static_show_mentions), AppData.appConfiguration.isShowMentions()));
        mSettingsList.add(new SettingsSwitchModel(4, getString(R.string.settimgs_timeline_static_show_retweets), AppData.appConfiguration.isShowRetweets()));
//        mSettingsList.add(new SettingsSwitchModel(5, getString(R.string.settings_timeline_static_tweet_marker), AppData.appConfiguration.isTweetMarker()));
//        mSettingsList.add(new SettingsSwitchModel(6, getString(R.string.settings_timeline_static_wifi_stream), AppData.appConfiguration.isStreamOnWifi()));

        mAdapter = new SettingsAdapter(mSettingsList, getApplicationContext(), textClickListener, true);

        TimelineViewModel viewModel = new TimelineViewModel(mAdapter, getApplicationContext());
        binding.setModel(viewModel);
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
            switch (model.getId()) {
                case 3:
                    if (!model.isOn()) {
                        final Handler handler = new Handler();
                        if (FeedData.getInstance().getFeedStatuses().size() == 0) {
                            FeedData.getInstance().loadCache();
                        }

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                List<StatusModel> removeModels = new ArrayList<>();
                                for (StatusModel statusModel : FeedData.getInstance().getFeedStatuses()) {
                                    if (statusModel.getText().contains(AppData.ME.getScreenName())) {
                                        removeModels.add(statusModel);
                                    }
                                }

                                FeedData.getInstance().getFeedStatuses().removeAll(removeModels);

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        FeedData.getInstance().getUpdateHandler().onUpdate();
                                        FeedData.getInstance().saveCache("Settings Timeline");
                                    }
                                });
                            }
                        }).start();
                    }
                    break;

                case 4:
                    if (!model.isOn()) {
                        final Handler handler = new Handler();
                        if (FeedData.getInstance().getFeedStatuses().size() == 0) {
                            FeedData.getInstance().loadCache();
                        }

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                List<StatusModel> removeModels = new ArrayList<>();
                                for (StatusModel statusModel : FeedData.getInstance().getFeedStatuses()) {
                                    if (statusModel.isRetweet()) {
                                        removeModels.add(statusModel);
                                    }
                                }

                                FeedData.getInstance().getFeedStatuses().removeAll(removeModels);

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        FeedData.getInstance().getUpdateHandler().onUpdate();
                                        FeedData.getInstance().saveCache("Settings Timeline");
                                    }
                                });
                            }
                        }).start();
                    }
                    break;
            }
        }
    };
}
