package solonsky.signal.twitter.overlays;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ru.terrakok.cicerone.Router;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.activities.SplashActivity;
import solonsky.signal.twitter.adapters.SelectorAdapter;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.FileNames;
import solonsky.signal.twitter.helpers.FileWork;
import solonsky.signal.twitter.helpers.ScreenKeys;
import solonsky.signal.twitter.models.ConfigurationUserModel;
import solonsky.signal.twitter.models.User;
import solonsky.signal.twitter.models.UserModel;

/**
 * Created by neura on 10.10.17.
 */

public class AccountSwitcherOverlay {
    private final String TAG = AccountSwitcherOverlay.class.getSimpleName();
    private final int TIME_LIMIT = 30;
    private final Router router;
    private LoggedActivity mActivity;
    private SelectorAdapter mAdapter;
    private boolean isStart = false;
    private ArrayList<UserModel> mUsersList;

    public AccountSwitcherOverlay(LoggedActivity loggedActivity, Router router) {
        this.mActivity = loggedActivity;
        this.router = router;
    }

    public void createSwitcher() {
        mUsersList = new ArrayList<>();
        for (ConfigurationUserModel configurationUserModel : AppData.configurationUserModels) {
            User user = configurationUserModel.getUser();
            UserModel userModel = new UserModel(user.getId(), user.getOriginalProfileImageURL(), user.getName(),
                    "@" + user.getScreenName(), true, false, false);
            userModel.setActive(user.getId() == AppData.userConfiguration.getUser().getId());
            userModel.setEnabled(user.getId() == AppData.userConfiguration.getUser().getId());
            mUsersList.add(userModel);
        }

        Collections.sort(mUsersList, new Comparator<UserModel>() {
            @Override
            public int compare(UserModel o1, UserModel o2) {
                return o1.getUsername().compareTo(o2.getUsername());
            }
        });

        Collections.sort(mUsersList, new Comparator<UserModel>() {
            @Override
            public int compare(UserModel o1, UserModel o2) {
                return o1.isActive() ? -1 : 0;
            }
        });

        mAdapter = new SelectorAdapter(mUsersList, mActivity.getApplicationContext(), mActivity, changeClickListener);
    }

    private void updateConstraints(boolean isShow) {
        mActivity.getBinding().txtLoggedSwitchLimit.setVisibility(isShow ? View.VISIBLE : View.GONE);
        long diff = TIME_LIMIT - ((System.currentTimeMillis() - AppData.lastSwitchTime) / 1000);
        mActivity.getBinding().txtLoggedSwitchLimit.setText(mActivity.getString(R.string.error_switch_limit)
                .replace("[TIME]", String.valueOf(diff)) + "s");

        Log.e(TAG, "my id " + AppData.ME.getId());
        if (isShow) {
            for (UserModel userModel : mUsersList) {
                if (userModel.getId() != AppData.ME.getId()) {
                    userModel.setEnabled(false);
                }
            }
        } else {
            for (UserModel userModel : mUsersList) {
                if (userModel.getId() != AppData.ME.getId()) {
                    userModel.setEnabled(true);
                }
            }
        }
    }

    public void showOverlay() {
        mActivity.getViewModel().setToolbarState(AppData.TOOLBAR_LOOGED_CHOOSE);
        mActivity.getBinding().tbLoggedChoose.setVisibility(View.VISIBLE);
        mActivity.getBinding().viewLoggedScrim.setVisibility(View.VISIBLE);
        mActivity.getBinding().recyclerLoggedUser.setVisibility(View.VISIBLE);

        updateConstraints(false);
        if (((System.currentTimeMillis() - AppData.lastSwitchTime) / 1000) > (TIME_LIMIT)) {
            //Do nothing
        } else {
            isStart = true;
            count();
        }
    }

    private void count() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isStart) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            long diff = TIME_LIMIT - ((System.currentTimeMillis() - AppData.lastSwitchTime) / 1000);
                            if (diff > 0) {
                                mActivity.getBinding().txtLoggedSwitchLimit.setText(mActivity.getString(R.string.error_switch_limit)
                                        .replace("[TIME]", String.valueOf(TIME_LIMIT - ((System.currentTimeMillis() - AppData.lastSwitchTime) / 1000)) + "s"));
                            } else {
                                mActivity.getBinding().txtLoggedSwitchLimit.setVisibility(View.GONE);
                                for (UserModel userModel : mUsersList) {
                                    userModel.setEnabled(true);
                                }
                                isStart = false;
                            }
                        }
                    });

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Do nothing
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            count();
                        }
                    });
                }
            }
        }).start();
    }

    public void hideOverlay() {
        if (!mActivity.getViewModel().isAdding()) {
            mActivity.getViewModel().setToolbarState(AppData.TOOLBAR_LOGGED_MAIN);
            mActivity.getBinding().viewLoggedScrim.setVisibility(View.GONE);
            mActivity.getBinding().tbLoggedChoose.setVisibility(View.GONE);
            mActivity.getBinding().recyclerLoggedUser.setVisibility(View.GONE);
        }
        isStart = false;
    }

    private SelectorAdapter.SelectorClickHandler changeClickListener = new SelectorAdapter.SelectorClickHandler() {
        @Override
        public void onItemClick(final UserModel model, View v) {
            if (model.getId() == AppData.userConfiguration.getUser().getId()) return;
            if (((System.currentTimeMillis() - AppData.lastSwitchTime) / 1000) > (TIME_LIMIT)) {
                mActivity.prepareToRecreate(true);
                mActivity.configPresenter.updateUser(model.getId());
                mActivity.startActivity(new Intent(mActivity.getApplicationContext(), SplashActivity.class));
                mActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                mActivity.finish();
            } else {
                updateConstraints(true);
            }
        }
    };

    public SelectorAdapter getmAdapter() {
        return mAdapter;
    }

    public void setmAdapter(SelectorAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }
}
