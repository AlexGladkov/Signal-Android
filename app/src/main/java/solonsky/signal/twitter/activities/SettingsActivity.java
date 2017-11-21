package solonsky.signal.twitter.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.databinding.ActivitySettingsBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.viewmodels.SettingsViewModel;

/**
 * Created by neura on 23.05.17.
 */

public class SettingsActivity extends AppCompatActivity {
    private SettingsViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        ActivitySettingsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(getResources().getColor(App.getInstance().isNightEnabled() ?
            android.R.color.transparent : R.color.light_status_bar_timeline_color));

        viewModel = new SettingsViewModel();

        binding.setModel(viewModel);
        binding.setClick(new SettingsViewModel.SettingsClickHandler() {
            @Override
            public void onAppearanceClick(View v) {
                startActivity(new Intent(getApplicationContext(), AppearanceActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onTimelineClick(View v) {
                startActivity(new Intent(getApplicationContext(), TimelineActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onGesturesClick(View v) {
                startActivity(new Intent(getApplicationContext(), GesturesActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onNotificationsClick(View v) {
                startActivity(new Intent(getApplicationContext(), UsersListActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onAdvancedClick(View v) {
                startActivity(new Intent(getApplicationContext(), AdvancedActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onHelpClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse(getString(R.string.help_link)));
                startActivity(intent);
            }

            @Override
            public void onAboutClick(View v) {
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onSupportClick(View v) {
                startActivity(new Intent(getApplicationContext(), SupportActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onBackClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }
}
