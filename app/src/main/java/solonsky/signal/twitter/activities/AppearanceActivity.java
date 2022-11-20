package solonsky.signal.twitter.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.squareup.picasso.Picasso;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;

import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.SettingsAdapter;
import solonsky.signal.twitter.databinding.ActivityAppearanceBinding;
import solonsky.signal.twitter.draw.CirclePicasso;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.FileNames;
import solonsky.signal.twitter.helpers.FileWork;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Styling;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkMode;
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkOnClickListener;
import solonsky.signal.twitter.models.ConfigurationModel;
import solonsky.signal.twitter.models.SettingsModel;
import solonsky.signal.twitter.models.SettingsSwitchModel;
import solonsky.signal.twitter.models.SettingsTextModel;
import solonsky.signal.twitter.presenters.ConfigurationsPresenter;
import solonsky.signal.twitter.viewmodels.AppearanceViewModel;
import solonsky.signal.twitter.views.ConfigurationView;

/**
 * Created by neura on 23.05.17.
 */

public class AppearanceActivity extends MvpAppCompatActivity implements ConfigurationView {
    private static final String TAG = AppearanceActivity.class.getSimpleName();
    private ArrayList<SettingsModel> mSettingsList;
    private SettingsAdapter mAdapter;
    private AppearanceActivity mActivity;
    private ActivityAppearanceBinding binding;

    @InjectPresenter

    ConfigurationsPresenter presenter;

    private final String PREVIEW_URL = "http://getsignal.co/images/app/promo-image.png";
    private final String AVATAR_URL_SMALL = "http://getsignal.co/images/app/twitter-profile-a.png";
    private final String AVATAR_URL_BIG = "http://getsignal.co/images/app/twitter-profile-b.png";
//    private final String AVATAR_URL = "https://www.w3schools.com/css/img_fjords.jpg";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_appearance);
        binding.imgAppearanceBack.setOnClickListener(new View.OnClickListener() {
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
        mSettingsList = new ArrayList<>();

        switchAvatar();
        switchPreview();
        applyStyling();

        binding.appearanceViewPreviewBig.setVisibility(AppData.appConfiguration.getThumbnails() == ConfigurationModel.THUMB_BIG ?
                View.VISIBLE : View.GONE);
        binding.appearanceViewPreviewSmall.setVisibility(AppData.appConfiguration.getThumbnails() == ConfigurationModel.THUMB_SMALL ?
                View.VISIBLE : View.GONE);
        binding.txtAppearanceCreatedAt.setText(AppData.appConfiguration.isRelativeDates() ?
                "2m" : new LocalDateTime().toString("dd.MM.yy, HH:mm"));

        binding.appearanceTweetText.addAutoLinkMode(
                AutoLinkMode.MODE_HASHTAG,
                AutoLinkMode.MODE_MENTION,
                AutoLinkMode.MODE_URL,
                AutoLinkMode.MODE_SHORT
        );

        boolean isNight = App.getInstance().isNightEnabled();
        binding.appearanceTweetText.setHashtagModeColor(ContextCompat.getColor(getApplicationContext(), isNight ?
                R.color.dark_tag_color : R.color.light_tag_color));
        binding.appearanceTweetText.setMentionModeColor(ContextCompat.getColor(getApplicationContext(), isNight ?
                R.color.dark_highlight_color : R.color.light_highlight_color));
        binding.appearanceTweetText.setUrlModeColor(ContextCompat.getColor(getApplicationContext(), isNight ?
                R.color.dark_highlight_color : R.color.light_highlight_color));
        binding.appearanceTweetText.setSelectedStateColor(ContextCompat.getColor(getApplicationContext(), isNight ?
                R.color.dark_reply_tint_color : R.color.light_reply_tint_color));

        String[] urls = new String[1];
        urls[0] = "getsignal.co";

        binding.appearanceTweetText.setShortUrls(urls);
        binding.appearanceTweetText.setAutoLinkText(getString(R.string.settings_appearance_text));
        binding.appearanceTweetText.setAutoLinkOnClickListener(new AutoLinkOnClickListener() {
            @Override
            public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                Utilities.openLink(matchedText, mActivity);
            }

            @Override
            public void onAutoLinkLongTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                Utilities.openLink(matchedText, mActivity);
            }
        });

        String appearanceFontSize = AppData.appConfiguration.getFontSize() == ConfigurationModel.FONT_TINY
                ? getString(R.string.font_tiny) : AppData.appConfiguration.getFontSize() == ConfigurationModel.FONT_SMALL ?
                getString(R.string.font_small) : AppData.appConfiguration.getFontSize() == ConfigurationModel.FONT_REGULAR ?
                getString(R.string.font_regular) : AppData.appConfiguration.getFontSize() == ConfigurationModel.FONT_BIG ?
                getString(R.string.font_big) : getString(R.string.font_huge);

        String appearanceMediaPreview = AppData.appConfiguration.getThumbnails() == ConfigurationModel.THUMB_SMALL ?
                getString(R.string.appearance_media_menu_small) : AppData.appConfiguration.getThumbnails() == ConfigurationModel.THUMB_BIG ?
                getString(R.string.appearance_media_menu_big) : getString(R.string.appearance_media_menu_off);

        String appearanceNightMode = AppData.appConfiguration.getDarkMode() == ConfigurationModel.DARK_ALWAYS ?
                getString(R.string.appearance_night_menu_always) : AppData.appConfiguration.getDarkMode() == ConfigurationModel.DARK_AT_NIGHT ?
                getString(R.string.appearance_night_menu_night) : getString(R.string.appearance_night_menu_off);

        binding.txtAppearanceUsername.setText(AppData.appConfiguration.isRealNames() ? "Signal for Twitter" : "Signal");


        mSettingsList.add(new SettingsTextModel(getString(R.string.settings_appearance_font_size), appearanceFontSize));
        mSettingsList.add(new SettingsTextModel(getString(R.string.settings_appearance_media_preview), appearanceMediaPreview));
        mSettingsList.add(new SettingsTextModel(getString(R.string.settings_appearance_night_mode), appearanceNightMode));
        mSettingsList.add(new SettingsSwitchModel(0, getString(R.string.settings_appearance_real_names), AppData.appConfiguration.isRealNames()));
        mSettingsList.add(new SettingsSwitchModel(1, getString(R.string.settings_appearance_round_avatars), AppData.appConfiguration.isRoundAvatars()));
        mSettingsList.add(new SettingsSwitchModel(2, getString(R.string.settings_appearance_relative_dates), AppData.appConfiguration.isRelativeDates()));

        mAdapter = new SettingsAdapter(mSettingsList, getApplicationContext(), textClickListener, true);

        AppearanceViewModel viewModel = new AppearanceViewModel(mAdapter, getApplicationContext());
        binding.setModel(viewModel);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }

    private void applyStyling() {
        Styling styling = new Styling(getApplicationContext(), Styling.convertFontToStyle(AppData.appConfiguration.getFontSize()));
        binding.appearanceTweetText.setLineSpacing(styling.getTextExtra(), 1);
        binding.appearanceTweetText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.getTextSize());
        binding.txtAppearanceUsername.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.getTextSize());
        binding.txtAppearanceCreatedAt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.getCreatedAtSize());

        /* Set Text margin Top */
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.appearanceLlText.getLayoutParams();
        params.topMargin = styling.getTextMarginTop();
        binding.appearanceLlText.setLayoutParams(params);

        /* Set base margin elements */
        binding.llAppearancePreview.setPadding(0, 0, 0, styling.getBaseMargin());

        // Square avatar
        LinearLayout.LayoutParams baseParams = (LinearLayout.LayoutParams) binding.appearanceImgAvatar.getLayoutParams();
        baseParams.bottomMargin = styling.getBaseMargin();
        baseParams.topMargin = styling.getSquareAvatarMarginTop();
        binding.appearanceImgAvatar.setLayoutParams(baseParams);

        // Round avatar
        baseParams = (LinearLayout.LayoutParams) binding.appearanceCivAvatar.getLayoutParams();
        baseParams.topMargin = styling.getBaseMargin();
        baseParams.bottomMargin = styling.getBaseMargin();
        binding.appearanceCivAvatar.setLayoutParams(baseParams);

        // Big preview
        baseParams = (LinearLayout.LayoutParams) binding.appearanceViewPreviewBig.getLayoutParams();
        baseParams.topMargin = styling.getBigImageMarginTop();
        binding.appearanceViewPreviewBig.setLayoutParams(baseParams);
    }

    private void switchPreview() {
        if (AppData.appConfiguration.getThumbnails() == ConfigurationModel.THUMB_BIG) {
            binding.appearanceViewPreviewSmall.setVisibility(View.GONE);
            binding.appearanceViewPreviewBig.setVisibility(View.VISIBLE);

            int width = (int) (Utilities.getScreenWidth(mActivity) - Utilities.convertDpToPixel(88, getApplicationContext()));
            int height = (int) (width * 0.617);

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.appearanceViewPreviewBig.getLayoutParams();
            params.width = width;
            params.height = height;

            Picasso.get()
                    .load(PREVIEW_URL)
                    .resize(width, height)
                    .centerCrop()
                    .transform(new CirclePicasso(
                            Utilities.convertDpToPixel(4, getApplicationContext()),
                            Utilities.convertDpToPixel(0.5f, getApplicationContext()),
                            25, R.color.black))
                    .into(binding.appearanceViewPreviewBig);
            return;
        }

        if (AppData.appConfiguration.getThumbnails() == ConfigurationModel.THUMB_SMALL) {
            binding.appearanceViewPreviewSmall.setVisibility(View.VISIBLE);
            binding.appearanceViewPreviewBig.setVisibility(View.GONE);

            Picasso.get()
                    .load(PREVIEW_URL)
                    .resize((int) Utilities.convertDpToPixel(64, getApplicationContext()),
                            (int) Utilities.convertDpToPixel(64, getApplicationContext()))
                    .centerCrop()
                    .transform(new CirclePicasso(
                            Utilities.convertDpToPixel(4, getApplicationContext()),
                            Utilities.convertDpToPixel(0.5f, getApplicationContext()),
                            25, R.color.black))
                    .into(binding.appearanceViewPreviewSmall);

            return;
        }

        if (AppData.appConfiguration.getThumbnails() == ConfigurationModel.THUMB_OFF) {
            binding.appearanceViewPreviewBig.setVisibility(View.GONE);
            binding.appearanceViewPreviewSmall.setVisibility(View.GONE);
            Log.e(TAG, "Thumb is none");
        }
    }

    private void switchAvatar() {
        if (AppData.appConfiguration.isRoundAvatars()) {
            binding.appearanceCivAvatar.setVisibility(View.VISIBLE);
            binding.appearanceImgAvatar.setVisibility(View.GONE);

            boolean isNight = App.getInstance().isNightEnabled();
            binding.appearanceCivAvatar.setBorderOverlay(!isNight);
            binding.appearanceCivAvatar.setBorderColor(isNight ? Color.parseColor("#00000000") : Color.parseColor("#1A000000"));
            binding.appearanceCivAvatar.setBorderWidth(isNight ? 0 : (int) Utilities.convertDpToPixel(0.5f, getApplicationContext()));

            Picasso.get()
                    .load(AVATAR_URL_SMALL)
                    .into(binding.appearanceCivAvatar);
        } else {
            binding.appearanceCivAvatar.setVisibility(View.GONE);
            binding.appearanceImgAvatar.setVisibility(View.VISIBLE);

            Picasso.get()
                    .load(AVATAR_URL_BIG)
                    .resize((int) Utilities.convertDpToPixel(40, getApplicationContext()),
                            (int) Utilities.convertDpToPixel(40, getApplicationContext()))
                    .centerCrop()
                    .transform(new CirclePicasso(
                            Utilities.convertDpToPixel(4, getApplicationContext()),
                            Utilities.convertDpToPixel(0.5f, getApplicationContext()),
                            25, R.color.black))
                    .into(binding.appearanceImgAvatar);
        }
    }

    private SettingsAdapter.SettingsClickListener textClickListener = new SettingsAdapter.SettingsClickListener() {
        @Override
        public void onItemClick(final SettingsTextModel model, View v) {
            PopupMenu popupMenu = new PopupMenu(mActivity, v, 0, 0, R.style.popup_menu);
            MenuInflater menuInflater = popupMenu.getMenuInflater();

            if (model.getTitle().equalsIgnoreCase(getString(R.string.settings_appearance_font_size))) {
                menuInflater.inflate(R.menu.menu_appearance_font, popupMenu.getMenu());
            } else if (model.getTitle().equalsIgnoreCase(getString(R.string.settings_appearance_media_preview))) {
                menuInflater.inflate(R.menu.menu_appearance_media, popupMenu.getMenu());
            } else if (model.getTitle().equalsIgnoreCase(getString(R.string.settings_appearance_night_mode))) {
                menuInflater.inflate(R.menu.menu_appearance_night, popupMenu.getMenu());
            }

            final FileWork fileWork = new FileWork(getApplicationContext());

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.font_12:
                            model.setSubtitle(getString(R.string.font_tiny));
                            AppData.appConfiguration.setFontSize(ConfigurationModel.FONT_TINY);
                            Flags.needsToRedrawFeed = true;
                            Flags.needsToRedrawDirect = true;
                            break;

                        case R.id.font_14:
                            model.setSubtitle(getString(R.string.font_small));
                            AppData.appConfiguration.setFontSize(ConfigurationModel.FONT_SMALL);
                            Flags.needsToRedrawFeed = true;
                            Flags.needsToRedrawDirect = true;
                            break;

                        case R.id.font_16:
                            model.setSubtitle(getString(R.string.font_regular));
                            AppData.appConfiguration.setFontSize(ConfigurationModel.FONT_REGULAR);
                            Flags.needsToRedrawFeed = true;
                            Flags.needsToRedrawDirect = true;
                            break;

                        case R.id.font_18:
                            model.setSubtitle(getString(R.string.font_big));
                            AppData.appConfiguration.setFontSize(ConfigurationModel.FONT_BIG);
                            Flags.needsToRedrawFeed = true;
                            Flags.needsToRedrawDirect = true;
                            break;

                        case R.id.font_20:
                            model.setSubtitle(getString(R.string.font_huge));
                            AppData.appConfiguration.setFontSize(ConfigurationModel.FONT_HUGE);
                            Flags.needsToRedrawFeed = true;
                            Flags.needsToRedrawDirect = true;
                            break;

                        case R.id.media_huge:
                            model.setSubtitle(getString(R.string.appearance_media_menu_big));
                            AppData.appConfiguration.setThumbnails(ConfigurationModel.THUMB_BIG);
                            switchPreview();

                            binding.appearanceViewPreviewSmall.setVisibility(View.GONE);
                            binding.appearanceViewPreviewBig.setVisibility(View.VISIBLE);
                            Flags.needsToRedrawFeed = true;
                            break;

                        case R.id.media_off:
                            model.setSubtitle(getString(R.string.appearance_media_menu_off));
                            AppData.appConfiguration.setThumbnails(ConfigurationModel.THUMB_OFF);
                            switchPreview();

                            binding.appearanceViewPreviewBig.setVisibility(View.GONE);
                            binding.appearanceViewPreviewSmall.setVisibility(View.GONE);
                            Flags.needsToRedrawFeed = true;
                            break;

                        case R.id.media_small:
                            model.setSubtitle(getString(R.string.appearance_media_menu_small));
                            AppData.appConfiguration.setThumbnails(ConfigurationModel.THUMB_SMALL);
                            switchPreview();

                            binding.appearanceViewPreviewBig.setVisibility(View.GONE);
                            binding.appearanceViewPreviewSmall.setVisibility(View.VISIBLE);
                            Flags.needsToRedrawFeed = true;
                            break;

                        case R.id.night_auto:
                            model.setSubtitle(getString(R.string.appearance_night_menu_night));
                            AppData.appConfiguration.setDarkMode(ConfigurationModel.DARK_AT_NIGHT);
                            break;

                        case R.id.night_off:
                            model.setSubtitle(getString(R.string.appearance_night_menu_off));
                            AppData.appConfiguration.setDarkMode(ConfigurationModel.DARK_OFF);
                            break;

                        case R.id.night_on:
                            model.setSubtitle(getString(R.string.appearance_night_menu_always));
                            AppData.appConfiguration.setDarkMode(ConfigurationModel.DARK_ALWAYS);
                            break;
                    }

                    fileWork.writeToFile(AppData.appConfiguration.exportConfiguration().toString(), FileNames.APP_CONFIGURATION);
                    presenter.updateAppSettings(AppData.appConfiguration);
                    applyStyling();
                    return false;
                }
            });

            popupMenu.show();
        }

        @Override
        public void onSwitchClick(SettingsSwitchModel model, View v) {
            presenter.updateAppSettings(AppData.appConfiguration);
            switch (model.getId()) {
                case 0:
                    binding.txtAppearanceUsername.setText(model.isOn() ? "Signal for Twitter" : "Signal");
                    Flags.needsToRedrawFeed = true;
                    Flags.needsToRedrawDirect = true;
                    break;

                case 1:
                    Flags.needsToRedrawFeed = true;
                    Flags.needsToRedrawDirect = true;
                    switchAvatar();
                    break;

                case 2:
                    binding.txtAppearanceCreatedAt.setText(model.isOn() ? "2m" : new LocalDateTime().toString("dd.MM.yy, HH:mm"));
                    Flags.needsToRedrawFeed = true;
                    break;
            }
        }
    };

    // MARK: - View implementation
    @Override
    public void settingsUpdated() {
        Log.e(TAG, "Settings updated");
    }
}
