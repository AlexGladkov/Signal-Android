package solonsky.signal.twitter.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import com.squareup.picasso.Picasso;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.databinding.ActivityAboutBinding;
import solonsky.signal.twitter.draw.CirclePicasso;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.viewmodels.AboutViewModel;

/**
 * Created by neura on 23.05.17.
 */
public class AboutActivity extends AppCompatActivity {

    private final String TAG = "ABOUTACTIVITY";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final boolean isNight = App.getInstance().isNightEnabled();

        if (isNight) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        final ActivityAboutBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_about);

        final AboutActivity mActivity = this;
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.parseColor("#33000000"));

//        setSupportActionBar(binding.tbAbout);

        binding.imgAboutBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.llAboutWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.openLink("http://getsignal.co", mActivity);
            }
        });

        binding.llAboutTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(getApplicationContext(), MVPProfileActivity.class);
                profileIntent.putExtra(Flags.PROFILE_SCREEN_NAME, "@getsignaI");
                mActivity.startActivity(profileIntent);
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        binding.llAboutAuthor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(getApplicationContext(), MVPProfileActivity.class);
                profileIntent.putExtra(Flags.PROFILE_SCREEN_NAME, "@solonsky");
                mActivity.startActivity(profileIntent);
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        binding.llAboutDeveloper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(getApplicationContext(), MVPProfileActivity.class);
                profileIntent.putExtra(Flags.PROFILE_SCREEN_NAME, "@NeuraSC2");
                mActivity.startActivity(profileIntent);
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        binding.llAboutSaver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.openLink("http://saverapp.co", mActivity);
            }
        });

        binding.imgAboutClient.setBorderOverlay(!isNight);
        binding.imgAboutClient.setBorderColor(isNight ? Color.parseColor("#00000000") : Color.parseColor("#1A000000"));
        binding.imgAboutClient.setBorderWidth(isNight ? 0 : (int) Utilities.convertDpToPixel(0.5f, getApplicationContext()));

        binding.imgAboutAuthor.setBorderOverlay(!isNight);
        binding.imgAboutAuthor.setBorderColor(isNight ? Color.parseColor("#00000000") : Color.parseColor("#1A000000"));
        binding.imgAboutAuthor.setBorderWidth(isNight ? 0 : (int) Utilities.convertDpToPixel(0.5f, getApplicationContext()));

        binding.imgAboutApp.setBorderOverlay(!isNight);
        binding.imgAboutApp.setBorderColor(isNight ? Color.parseColor("#00000000") : Color.parseColor("#1A000000"));
        binding.imgAboutApp.setBorderWidth(isNight ? 0 : (int) Utilities.convertDpToPixel(0.5f, getApplicationContext()));

        Picasso.with(getApplicationContext()).load("https://pbs.twimg.com/profile_images/655028102314131456/1ct_DreL_400x400.png")
                .resize((int) Utilities.convertDpToPixel(40, getApplicationContext()),
                        (int) Utilities.convertDpToPixel(40, getApplicationContext()))
                .transform(new CirclePicasso(
                        Utilities.convertDpToPixel(4, getApplicationContext()),
                        Utilities.convertDpToPixel(1f, getApplicationContext()),
                        25, R.color.black))
                .into(binding.imgAboutClient);
        Picasso.with(getApplicationContext()).load("https://pp.userapi.com/c837520/v837520253/1b073/zqRW0Vkstzg.jpg")
                .resize((int) Utilities.convertDpToPixel(40, getApplicationContext()),
                        (int) Utilities.convertDpToPixel(40, getApplicationContext()))
                .transform(new CirclePicasso(
                        Utilities.convertDpToPixel(4, getApplicationContext()),
                        Utilities.convertDpToPixel(1f, getApplicationContext()),
                        25, R.color.black))
                .into(binding.imgAboutAuthor);
        Picasso.with(getApplicationContext()).load("https://pbs.twimg.com/profile_images/638436278141239296/lstgZC4g_400x400.png")
                .resize((int) Utilities.convertDpToPixel(40, getApplicationContext()),
                        (int) Utilities.convertDpToPixel(40, getApplicationContext()))
                .transform(new CirclePicasso(
                        Utilities.convertDpToPixel(4, getApplicationContext()),
                        Utilities.convertDpToPixel(1f, getApplicationContext()),
                        25, R.color.black))
                .into(binding.imgAboutApp);

        String version = "";
        int codeVersion = 0;
        try {
            PackageInfo pInfo = mActivity.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            codeVersion = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //Do nothing
        }

        AboutViewModel viewModel = new AboutViewModel(getApplicationContext(), "Signal " + version +
                " (" + codeVersion + ") © 2017 Alex Solonsky");
        binding.setModel(viewModel);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }
}
