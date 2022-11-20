package solonsky.signal.twitter.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.UserAdapter;
import solonsky.signal.twitter.databinding.ActivityUsersListBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.ConfigurationUserModel;
import solonsky.signal.twitter.models.User;
import solonsky.signal.twitter.models.UserModel;
import solonsky.signal.twitter.viewmodels.UsersListViewModel;

/**
 * Created by neura on 23.05.17.
 */

public class UsersListActivity extends AppCompatActivity {
    private final String TAG = UsersListActivity.class.getSimpleName();
    private ArrayList<UserModel> mUsersList;
    private UserAdapter mAdapter;

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";
    private ActivityUsersListBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDarkNoAnimation);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_users_list);
        binding.imgUserListBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(getResources().getColor(App.getInstance().isNightEnabled() ?
                android.R.color.transparent : R.color.light_status_bar_timeline_color));

        mUsersList = new ArrayList<>();
        Log.e(TAG, "list size " + mUsersList.size());
        for (ConfigurationUserModel configurationUserModel : AppData.configurationUserModels) {
            User user = configurationUserModel.getUser();
            mUsersList.add(new UserModel(user.getId(), user.getProfileImageUrl(), user.getName(),
                    "@" + user.getScreenName(), true, false, false));
        }

        mAdapter = new UserAdapter(mUsersList, getApplicationContext(), this, itemClickListener);
        binding.switchNotificationsIntercept.setChecked(isNotificationServiceEnabled());
        binding.switchNotificationsIntercept.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
            }
        });

        UsersListViewModel viewModel = new UsersListViewModel(mAdapter, getApplicationContext());
        binding.setModel(viewModel);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.switchNotificationsIntercept.setOnCheckedChangeListener(null);
        binding.switchNotificationsIntercept.setChecked(isNotificationServiceEnabled());
        binding.switchNotificationsIntercept.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }

    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     * @return True if eanbled, false otherwise.
     */
    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private UserAdapter.UserClickHandler itemClickListener = new UserAdapter.UserClickHandler() {
        @Override
        public void onItemClick(UserModel model, View v) {
            AppData.NOTIFICATIONS_CURRENT_USER = (model);
            startActivity(new Intent(getApplicationContext(), UsersNotificationActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    };
}
