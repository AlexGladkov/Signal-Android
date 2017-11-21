package solonsky.signal.twitter.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import solonsky.signal.twitter.databinding.CellSettingsSwitchBinding;
import solonsky.signal.twitter.databinding.CellSettingsTextBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.FileNames;
import solonsky.signal.twitter.helpers.FileWork;
import solonsky.signal.twitter.helpers.Localization;
import solonsky.signal.twitter.models.SettingsModel;
import solonsky.signal.twitter.models.SettingsSwitchModel;
import solonsky.signal.twitter.models.SettingsTextModel;

/**
 * Created by neura on 24.05.17.
 * Adapter for settings menus
 */
public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = "FEEDADAPTER";
    private final ArrayList<SettingsModel> mSettingsModels;
    private final Context mContext;
    private final SettingsClickListener clickListener;
    private final boolean hasTop;

    public interface SettingsClickListener {
        void onItemClick(SettingsTextModel model, View v);
        void onSwitchClick(SettingsSwitchModel model, View v);
    }

    public SettingsAdapter(ArrayList<SettingsModel> settingsModels, Context context,
                           SettingsClickListener clickListener, boolean hasTop) {
        this.mSettingsModels = settingsModels;
        this.mContext = context;
        this.clickListener = clickListener;
        this.hasTop = hasTop;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == AppData.SETTINGS_TYPE_SWITCH) {
            CellSettingsSwitchBinding binding = CellSettingsSwitchBinding.inflate(inflater, parent, false);
            return new SwitchViewHolder(binding.getRoot());
        } else {
            CellSettingsTextBinding binding = CellSettingsTextBinding.inflate(inflater, parent, false);
            return new TextViewHolder(binding.getRoot());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mSettingsModels.get(position).getType();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final SettingsModel settingsModel = mSettingsModels.get(position);
        switch (settingsModel.getType()) {
            case AppData.SETTINGS_TYPE_SWITCH:
                ((SwitchViewHolder) holder).mBinding.settingsDividerBottom.setVisibility(
                        position == mSettingsModels.size() - 1 ? View.GONE : View.VISIBLE);
                ((SwitchViewHolder) holder).mBinding.settingsDividerTop.setVisibility(position == 0 ?
                        hasTop ? View.VISIBLE : View.GONE : View.GONE);
                ((SwitchViewHolder) holder).mBinding.setModel((SettingsSwitchModel) settingsModel);
                ((SwitchViewHolder) holder).mBinding.setClick(new SettingsSwitchModel.SwitchClickHandler() {
                    @Override
                    public void onSwitcherClick(View v, boolean isOn) {
                        ((SettingsSwitchModel) settingsModel).setOn(isOn);
                        switch (settingsModel.getTitle()) {
                            case Localization.REAL_NAMES:
                                AppData.appConfiguration.setRealNames(isOn);
                                break;

                            case Localization.ROUND_AVATARS:
                                AppData.appConfiguration.setRoundAvatars(isOn);
                                break;

                            case Localization.RELATIVE_DATES:
                                AppData.appConfiguration.setRelativeDates(isOn);
                                break;

                            case Localization.STATIC_TOP_BAR:
                                AppData.appConfiguration.setStaticTopBars(isOn);
                                break;

                            case Localization.STATIC_BOTTOM_BAR:
                                AppData.appConfiguration.setStaticBottomBar(isOn);
                                break;

                            case Localization.GROUP_DIALOG:
                                AppData.appConfiguration.setGroupDialogs(isOn);
                                break;

                            case Localization.TWEET_MARKER:
                                AppData.appConfiguration.setTweetMarker(isOn);
                                break;

                            case Localization.STREAM_ON_WI_FI:
                                AppData.appConfiguration.setStreamOnWifi(isOn);
                                break;

                            case Localization.SHOW_MENTIONS:
                                AppData.appConfiguration.setShowMentions(isOn);
                                break;

                            case Localization.SHOW_RETWEETS:
                                AppData.appConfiguration.setShowRetweets(isOn);
                                break;

                            case Localization.DIM_MEDIA_AT_NIGHT:
                                AppData.appConfiguration.setDimMediaAtNight(isOn);
                                break;

                            case Localization.GROUP_PUSH_NOTIFICATIONS:
                                AppData.appConfiguration.setGroupPushNotifications(isOn);
                                break;

                            case Localization.PIN_TO_TOP_ON_STREAMING:
                                AppData.appConfiguration.setPinToTopOnStreaming(isOn);
                                break;

                            case Localization.SOUNDS:
                                AppData.appConfiguration.setSounds(isOn);
                                break;
                        }

                        clickListener.onSwitchClick((SettingsSwitchModel) settingsModel, v);
                        new FileWork(mContext).writeToFile(AppData.appConfiguration.exportConfiguration()
                                .toString(), FileNames.APP_CONFIGURATION);
                    }
                });
                break;

            case AppData.SETTINGS_TYPE_TEXT:
                ((TextViewHolder) holder).mBinding.settingsDividerBottom.setVisibility(
                        position == mSettingsModels.size() - 1 ? View.GONE : View.VISIBLE);
                ((TextViewHolder) holder).mBinding.settingsDividerTop.setVisibility(position == 0 ?
                        hasTop ? View.VISIBLE : View.GONE : View.GONE);
                ((TextViewHolder) holder).mBinding.setModel((SettingsTextModel) settingsModel);
                ((TextViewHolder) holder).mBinding.setClick(new SettingsTextModel.SettingsClickHandler() {
                    @Override
                    public void onItemClick(View v) {
                        clickListener.onItemClick((SettingsTextModel) settingsModel, v);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mSettingsModels.size();
    }

    private static class SwitchViewHolder extends RecyclerView.ViewHolder {
        private final CellSettingsSwitchBinding mBinding;

        SwitchViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }

    private static class TextViewHolder extends RecyclerView.ViewHolder {
        private final CellSettingsTextBinding mBinding;

        TextViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);
        }
    }
}
