package solonsky.signal.twitter.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.ChatActivity;
import solonsky.signal.twitter.activities.ComposeActivity;
import solonsky.signal.twitter.api.ActionsApiFactory;
import solonsky.signal.twitter.api.DirectApi;
import solonsky.signal.twitter.data.MuteData;
import solonsky.signal.twitter.data.UsersData;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.models.RemoveModel;
import solonsky.signal.twitter.models.User;

/**
 * Created by neura on 18.09.17.
 */

public class ProfileDialog {
    private final String TAG = ProfileDialog.class.getSimpleName();
    private User mUser;
    private String mScreenName;
    private Activity mActivity;
    private MaterialDialog mDialog;

    public ProfileDialog(String mScreenName, Activity activity) {
        this.mScreenName = mScreenName;
        this.mActivity = activity;
        createDialog();
    }

    public ProfileDialog(User user, Activity activity) {
        this.mUser = user;
        this.mActivity = activity;
        createDialog();
    }

    private void createDialog() {
        mDialog = new MaterialDialog.Builder(mActivity)
                .customView(App.getInstance().isNightEnabled() ?
                        R.layout.dialog_dark_user : R.layout.dialog_light_user, false)
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {

                    }
                })
                .build();

        mDialog.getWindow().getAttributes().windowAnimations = R.style.actionSheetAnimation;
        WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
        lp.alpha = 1.0f;
        mDialog.getWindow().setAttributes(lp);

        final View view = mDialog.getView();
        TextView title = (TextView) view.findViewById(R.id.dialog_user_title);

        boolean isFollow = false;
        boolean isFriend = false;
        boolean isMuted = false;
        boolean isDisabled = mScreenName != null || MuteData.getInstance().getmRetweetsIds().contains(mUser.getId());
//        Log.e(TAG, "isDisabled - " + isDisabled + " screenName " + mScreenName + " contains " +
//            MuteData.getInstance().getmRetweetsIds().contains(mUser.getId()));
        boolean isMe = mScreenName == null ?
                mUser.getId() == AppData.ME.getId() :
                TextUtils.equals(mScreenName.replace("@", "").toLowerCase(), AppData.ME.getScreenName().toLowerCase());

        if (mScreenName == null) {
            for (RemoveModel removeModel : MuteData.getInstance().getmUsersList()) {
                if (TextUtils.equals(removeModel.getTitle().toLowerCase(), "@" + mUser.getScreenName().toLowerCase())) {
                    isMuted = true;
                    break;
                }
            }
        } else {
            for (RemoveModel removeModel : MuteData.getInstance().getmUsersList()) {
                if (TextUtils.equals(removeModel.getTitle().toLowerCase(), mScreenName)) {
                    isMuted = true;
                    break;
                }
            }
        }

        if (mScreenName == null) {
            isFollow = UsersData.getInstance().getFollowersList().contains(mUser.getId());
            isFriend = UsersData.getInstance().getFollowingList().contains(mUser.getId());
        } else {
            for (solonsky.signal.twitter.models.User user : UsersData.getInstance().getUsersList()) {
                if (user.getScreenName().toLowerCase().contains(mScreenName.replace("@", ""))) {
                    if (UsersData.getInstance().getFollowersList().contains(user.getId())) {
                        isFollow = true;
                    }

                    if (UsersData.getInstance().getFollowingList().contains(user.getId())) {
                        isFriend = true;
                    }
                    break;
                }
            }
        }

        String titleText = mScreenName == null ? "@" + mUser.getScreenName() : mScreenName;
        titleText = titleText + (!isMe ? (" " + (isFollow ?
                mActivity.getResources().getString(R.string.user_follow) :
                mActivity.getResources().getString(R.string.user_not_follow))) : "");
        title.setText(titleText);

        final TextView mBtnFollow = (TextView) view.findViewById(R.id.dialog_follow);
        final TextView mBtnUnfollow = (TextView) view.findViewById(R.id.dialog_unfollow);
        final TextView mBtnMute = (TextView) view.findViewById(R.id.dialog_mute);
        final TextView mBtnUnmute = (TextView) view.findViewById(R.id.dialog_unmute);
        final TextView mBtnDisableRt = (TextView) view.findViewById(R.id.dialog_disable_retweets);
        final TextView mBtnEnableRt = (TextView) view.findViewById(R.id.dialog_enable_retweets);

        mBtnUnmute.setVisibility((!isMuted || isMe) ? View.GONE : View.VISIBLE);
        mBtnMute.setVisibility((isMuted || isMe) ? View.GONE : View.VISIBLE);
        mBtnFollow.setVisibility((isFriend || isMe) ? View.GONE : View.VISIBLE);
        mBtnUnfollow.setVisibility((!isFriend || isMe) ? View.GONE : View.VISIBLE);
        mBtnDisableRt.setVisibility(isDisabled ? View.GONE : View.VISIBLE);
        mBtnEnableRt.setVisibility(!isDisabled ? View.GONE : View.VISIBLE);

        view.findViewById(R.id.dialog_reply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Flags.CURRENT_COMPOSE = Flags.COMPOSE_MENTION;
//                AppData.CURRENT_STATUS_MODEL = statusModel;
                AppData.COMPOSE_MENTION = (mScreenName == null ?
                        "@" + mUser.getScreenName() : mScreenName);
                mActivity.startActivity(new Intent(mActivity.getApplicationContext(), ComposeActivity.class));
                mDialog.dismiss();
            }
        });

        view.findViewById(R.id.dialog_send_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppData.DM_SELECTED_USER = (mScreenName == null ?
                        mUser.getName() : mScreenName);

                String screenName = mScreenName == null ?
                        mUser.getScreenName() : mScreenName.replace("@", "");
                long userId = mScreenName == null ? mUser.getId() : -1;

                DirectApi.getInstance().clear();
                DirectApi.getInstance().setUserId(userId);
                DirectApi.getInstance().setScreenName(screenName);
                DirectApi.getInstance().setUserName(mUser != null ? mUser.getName() : "");
                mActivity.startActivity(new Intent(mActivity.getApplicationContext(), ChatActivity.class));
                mDialog.dismiss();
            }
        });

        mBtnDisableRt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScreenName == null) {
                    ActionsApiFactory.disableRetweet(mUser.getId(), mActivity.getApplicationContext());
                } else {
                    ActionsApiFactory.disableRetweet(mScreenName.replace("@", ""), mActivity.getApplicationContext());
                }
                mDialog.dismiss();
            }
        });

        mBtnEnableRt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScreenName == null) {
                    ActionsApiFactory.enableRetweet(mUser.getId(), mActivity.getApplicationContext());
                } else {
                    ActionsApiFactory.enableRetweet(mScreenName.replace("@", ""), mActivity.getApplicationContext());
                }
                mDialog.dismiss();
            }
        });

        mBtnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScreenName == null) {
                    ActionsApiFactory.mute(mUser.getId(), mActivity.getApplicationContext());
                } else {
                    ActionsApiFactory.mute(mScreenName.replace("@", ""), mActivity.getApplicationContext());
                }
                mDialog.dismiss();
            }
        });

        mBtnUnmute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScreenName == null) {
                    ActionsApiFactory.unmute(mUser.getId(), mActivity.getApplicationContext());
                } else {
                    ActionsApiFactory.unmute(mScreenName.replace("@", ""), mActivity.getApplicationContext());
                }
                mDialog.dismiss();
            }
        });

        mBtnFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScreenName == null) {
                    ActionsApiFactory.follow(mUser.getId(), mActivity.getApplicationContext());
                } else {
                    ActionsApiFactory.follow(mScreenName.replace("@", ""), mActivity.getApplicationContext());
                }
                mDialog.dismiss();
            }
        });

        mBtnUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScreenName == null) {
                    ActionsApiFactory.unfollow(mUser.getId(), mActivity.getApplicationContext());
                } else {
                    ActionsApiFactory.unfollow(mScreenName.replace("@", ""), mActivity.getApplicationContext());
                }
                mDialog.dismiss();
            }
        });
    }

    public void show() {
        try {
            mDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Too hard architecture");
        }
    }
}
