package solonsky.signal.twitter.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import java.io.File;
import java.util.List;

import pl.aprilapps.easyphotopicker.EasyImage;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.data.ProfileRefreshData;
import solonsky.signal.twitter.databinding.ActivityProfileSettingsBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.FileNames;
import solonsky.signal.twitter.helpers.FileWork;
import solonsky.signal.twitter.helpers.Permission;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.viewmodels.ProfileSettingsViewModel;
import twitter4j.AsyncTwitter;
import twitter4j.ResponseList;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.User;

/**
 * Created by neura on 26.06.17.
 */

public class ProfileSettingsActivity extends AppCompatActivity {
    private int CURRENT_POSITION = 0;

    private static final int REQUEST_AVATAR_CODE = 0;
    private static final int REQUEST_BANNER_CODE = 1;

    private File avatarFile = null;
    private File bannerFile = null;

    private ProfileSettingsViewModel viewModel;
    private final String TAG = "PROFILEFRAGMENT";
    private ActivityProfileSettingsBinding binding;
    private ProfileSettingsActivity mActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile_settings);
        mActivity = this;

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        EasyImage.configuration(this)
                .setAllowMultiplePickInGallery(false)
                .setImagesFolderName(getString(R.string.app_name))
                .setCopyTakenPhotosToPublicGalleryAppFolder(true);

        viewModel = new ProfileSettingsViewModel(AppData.ME.getName(),
                AppData.ME.getLocation(),
                AppData.ME.getUrlEntity() == null ? "" : AppData.ME.getUrlEntity().get("displayURL").getAsString(),
                AppData.ME.getDescription(),
                AppData.ME.getOriginalProfileImageURL(), AppData.ME.getProfileBannerImageUrl());
        binding.setModel(viewModel);
        binding.setClick(new ProfileSettingsViewModel.ProfileSettingsClickHandler() {
            @Override
            public void onBackClick(View v) {
                onBackPressed();
            }

            @Override
            public void onSaveClick(View v) {
                if (viewModel.isApply()) {
                    final Gson gson = new GsonBuilder()
                            .setPrettyPrinting()
                            .create();
                    final FileWork fileWork = new FileWork(getApplicationContext());
                    final android.os.Handler handler = new android.os.Handler();

                    final AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
                    asyncTwitter.addListener(new TwitterAdapter() {
                        @Override
                        public void updatedProfile(User user) {
                            super.updatedProfile(user);
                            AppData.ME = (solonsky.signal.twitter.models.User.getFromUserInstance(user));
                            fileWork.writeToFile(gson.toJson(AppData.ME), FileNames.USER);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (ProfileRefreshData.getInstance().getUpdateHandler() != null)
                                        ProfileRefreshData.getInstance().getUpdateHandler().onInfoUpdate();
                                }
                            });
                        }

                        @Override
                        public void updatedProfileImage(User user) {
                            super.updatedProfileImage(user);
                            AppData.ME = (solonsky.signal.twitter.models.User.getFromUserInstance(user));
                            fileWork.writeToFile(gson.toJson(AppData.ME), FileNames.USER);

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (ProfileRefreshData.getInstance().getUpdateHandler() != null)
                                        ProfileRefreshData.getInstance().getUpdateHandler().onAvatarUpdate();
                                }
                            });
                        }

                        @Override
                        public void updatedProfileBanner() {
                            super.updatedProfileBanner();
                            asyncTwitter.lookupUsers(AppData.ME.getId());
                        }

                        @Override
                        public void lookedupUsers(ResponseList<User> users) {
                            super.lookedupUsers(users);
                            if (users.size() > 0) {
                                AppData.ME = (solonsky.signal.twitter.models.User.getFromUserInstance(users.get(0)));
                                fileWork.writeToFile(gson.toJson(AppData.ME), FileNames.USER);

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (ProfileRefreshData.getInstance().getUpdateHandler() != null)
                                            ProfileRefreshData.getInstance().getUpdateHandler().onBannerUpdate();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onException(final TwitterException te, TwitterMethod method) {
                            super.onException(te, method);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (te.getErrorMessage() != null)
                                        Toast.makeText(getApplicationContext(), "Error updating profile " + te.getErrorMessage(),
                                                Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    asyncTwitter.updateProfile(viewModel.getCurrentName(), viewModel.getCurrentLink(),
                            viewModel.getCurrentLocation(), viewModel.getCurrentAbout());
                    if (viewModel.isNewAvatar())
                        asyncTwitter.updateProfileImage(avatarFile);
                    if (viewModel.isNewBanner())
                        asyncTwitter.updateProfileBanner(bannerFile);
                    onBackPressed();
                }
            }

            @Override
            public void onAvatarClick(View v) {
                if (Permission.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Matisse.from(ProfileSettingsActivity.this)
                            .choose(MimeType.ofImage())
                            .countable(false)
                            .maxSelectable(1)
                            .imageEngine(new PicassoEngine())
                            .forResult(REQUEST_AVATAR_CODE);
                } else {
                    ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Permission.PHOTO_REQUEST);
                }
            }

            @Override
            public void onBannerClick(View v) {
                if (Permission.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Matisse.from(ProfileSettingsActivity.this)
                            .choose(MimeType.ofImage())
                            .countable(false)
                            .maxSelectable(1)
                            .imageEngine(new PicassoEngine())
                            .forResult(REQUEST_BANNER_CODE);
                } else {
                    ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Permission.PHOTO_REQUEST);
                }
            }
        });

        Picasso.with(getApplicationContext())
                .load(AppData.ME.getProfileBannerImageUrl())
                .resize(Utilities.getScreenWidth(ProfileSettingsActivity.this),
                        (int) Utilities.convertDpToPixel(176, getApplicationContext()))
                .centerCrop()
                .into(binding.imgProfileSettingsBackdrop);


        binding.txtProfileSettingsName.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.txtProfileSettingsName.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                binding.txtProfileSettingsName.setSelection(binding.txtProfileSettingsName.getText().toString().length());
                binding.txtProfileSettingsWebsite.setSelection(binding.txtProfileSettingsWebsite.getText().toString().length());
                binding.txtProfileSettingsLocation.setSelection(binding.txtProfileSettingsLocation.getText().toString().length());
                binding.txtProfileSettingsAbout.setSelection(binding.txtProfileSettingsAbout.getText().toString().length());
            }
        });
    }

    @Override
    public void onBackPressed() {
        Utilities.hideKeyboard(ProfileSettingsActivity.this);
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_AVATAR_CODE:
                    List<String> images = Matisse.obtainPathResult(data);
                    if (images.size() > 0) {
                        File f = new File(images.get(0));
                        Picasso.with(getApplicationContext()).load(f).into(binding.imgProfileSettingsAvatar);
                        viewModel.setNewAvatar(true);
                        viewModel.setApply(true);
                        avatarFile = f;
                    }
                    break;

                case REQUEST_BANNER_CODE:
                    List<String> banners = Matisse.obtainPathResult(data);
                    if (banners.size() > 0) {
                        File f = new File(banners.get(0));
                        Picasso.with(getApplicationContext()).load(f)
                                .resize(Utilities.getScreenWidth(mActivity), (int) Utilities.convertDpToPixel(176, getApplicationContext()))
                                .centerCrop()
                                .into(binding.imgProfileSettingsBackdrop);
                        viewModel.setNewBanner(true);
                        viewModel.setApply(true);
                        bannerFile = f;
                    }
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }
}
