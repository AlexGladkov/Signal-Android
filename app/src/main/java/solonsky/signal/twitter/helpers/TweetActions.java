package solonsky.signal.twitter.helpers;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.ComposeActivity;
import solonsky.signal.twitter.activities.DetailActivity;
import solonsky.signal.twitter.api.ActionsApiFactory;
import solonsky.signal.twitter.data.ShareData;
import solonsky.signal.twitter.libs.ShareContent;
import solonsky.signal.twitter.libs.TargetChosenReceiver;
import solonsky.signal.twitter.models.StatusModel;
import twitter4j.AsyncTwitter;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;


/**
 * Created by neura on 08.08.17.
 * Class describing tweet actions
 * Made for reusable actions through all client
 */

public class TweetActions {
    private static final String TAG = TweetActions.class.getSimpleName();

    public interface ActionCallback {
        void onException(String error);
    }

    public interface MoreCallback {
        void onDelete(StatusModel statusModel);
    }

    /**
     * Performs more popup
     * @param mActivity - calling activity
     * @param v - popup view source
     * @param statusModel - current status
     */
    public static void morePopup(final Activity mActivity, View v,
                                 final StatusModel statusModel, final MoreCallback moreCallback) {
        PopupMenu popupMenu = new PopupMenu(mActivity, v, 0, 0, R.style.popup_menu);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.menu_tweet_more, popupMenu.getMenu());

        popupMenu.getMenu().getItem(4).setVisible(statusModel.getUser().getId() == AppData.ME.getId());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.more_copy:
                        ClipboardManager clipboard = (ClipboardManager)
                                mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText(mActivity.getString(R.string.app_name), statusModel.getText());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(mActivity.getApplicationContext(), mActivity.getString(R.string.success_copy), Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.more_delete:
                        final Handler handler = new Handler();
                        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
                        asyncTwitter.addListener(new TwitterAdapter() {
                            @Override
                            public void destroyedStatus(Status destroyedStatus) {
                                super.destroyedStatus(destroyedStatus);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(mActivity.getApplicationContext(), R.string.success_deleted,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                        asyncTwitter.destroyStatus(statusModel.getId());
                        moreCallback.onDelete(statusModel);
                        break;

                    case R.id.more_details:
                        AppData.CURRENT_STATUS_MODEL = statusModel;
                        mActivity.startActivity(new Intent(mActivity.getApplicationContext(), DetailActivity.class));
                        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        break;

                    case R.id.more_link:
                        ShareContent shareContent = new ShareContent(mActivity);
                        String template = "https://twitter.com/[screen_name]/status/[status_id]";
                        String shareText = template.replace("[screen_name]", statusModel.getUser().getScreenName())
                                .replace("[status_id]", String.valueOf(statusModel.getId()));

                        shareContent.shareText("", shareText, new TargetChosenReceiver.IntentCallback() {
                            @Override
                            public void getComponentName(String componentName) {
                                if (!ShareData.getInstance().isCacheLoaded())
                                    ShareData.getInstance().loadCache();

                                ShareData.getInstance().addShare(componentName);
                            }
                        });
                        break;

                    case R.id.more_translate:
                        ActionsApiFactory.translate(statusModel.getText(), mActivity);
                        break;
                }
                return false;
            }
        });

        popupMenu.show();
    }

    /**
     * Performs retweet popup and full dialog implementation
     * @param mActivity - calling activity
     * @param v - popup view source
     * @param statusModel - current status
     * @param actionCallback - error callback
     */
    public static void retweetPopup(final Activity mActivity, View v, final StatusModel statusModel,
                                    final ActionCallback actionCallback) {
        final PopupMenu popupMenu = new PopupMenu(mActivity, v, 0, 0, R.style.popup_menu);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.menu_retweet, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tweet_retweet:
                        popupMenu.dismiss();
                        retweet(statusModel, mActivity.getApplicationContext(), actionCallback);
                        break;

                    case R.id.tweet_quote:
                        popupMenu.dismiss();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                quote(mActivity, statusModel);
                            }
                        }, 200);
                        break;
                }
                return false;
            }
        });

        popupMenu.show();
    }

    /**
     * Change tweet favorite status
     * @param isFavorite - needs favorite state
     * @param id - status id
     * @param actionCallback - callback if error
     */
    public static void favorite(boolean isFavorite, long id, final ActionCallback actionCallback) {
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                actionCallback.onException(te.getLocalizedMessage());
            }
        });

        if (isFavorite) {
            asyncTwitter.createFavorite(id);
        } else {
            asyncTwitter.destroyFavorite(id);
        }
    }

    /**
     * Performs status share
     * @param statusModel - current status
     * @param mActivity - calling activity
     */
    public static void share(StatusModel statusModel, AppCompatActivity mActivity) {
        ShareContent shareContent = new ShareContent(mActivity);
        shareContent.shareText(statusModel.getText(), "", new TargetChosenReceiver.IntentCallback() {
            @Override
            public void getComponentName(String componentName) {
                ShareData.getInstance().addShare(componentName);
                ShareData.getInstance().saveCache();
            }
        });
    }

    /**
     * Performs reply action
     * @param statusModel - current status
     * @param mActivity - calling activity
     */
    public static void reply(StatusModel statusModel, AppCompatActivity mActivity) {
        AppData.CURRENT_STATUS_MODEL = statusModel;
        AppData.CURRENT_USER = statusModel.getUser();
        Flags.CURRENT_COMPOSE = Flags.COMPOSE_REPLY;

        mActivity.startActivity(new Intent(mActivity.getApplicationContext(), ComposeActivity.class));
        mActivity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_no_animation);
    }

    /**
     * Sorting and make dividers guide to markup
     * @param mTweetsList - incoming array
     */
    public static void verifyDividers(ArrayList<StatusModel> mTweetsList) {
        boolean isNight = App.getInstance().isNightEnabled();

        if (mTweetsList.size() == 1) mTweetsList.get(0).setDivideState(Flags.DIVIDER_LONG);
        for (int i = 0; i < mTweetsList.size() - 1; i++) {
            StatusModel nextModel = mTweetsList.get(i + 1);
            StatusModel statusModel = mTweetsList.get(i);

            if (i == mTweetsList.size() - 2) {
                nextModel.setDivideState(Flags.DIVIDER_LONG);
            } else {
                statusModel.setDivideState(statusModel.isHighlighted() ?
                        nextModel.isHighlighted() ? Flags.DIVIDER_SHORT : isNight ? Flags.DIVIDER_NONE
                                : Flags.DIVIDER_LONG : nextModel.isHighlighted() ? isNight ? Flags.DIVIDER_NONE
                        : Flags.DIVIDER_LONG : Flags.DIVIDER_SHORT);
            }
        }
    }

    /**
     * Performs retweet action
     * @param statusModel - retweeted status
     * @param actionCallback - callback error
     */
    public static void retweet(final StatusModel statusModel, final Context context, final ActionCallback actionCallback) {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void retweetedStatus(final Status retweetedStatus) {
                super.retweetedStatus(retweetedStatus);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (AppData.appConfiguration.isSounds()) {
                            final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.rt);
                            mediaPlayer.start();
                        }
                        Toast.makeText(context, context.getString(R.string.success_retweeted), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onException(TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                actionCallback.onException(te.getLocalizedMessage());
            }
        });

        asyncTwitter.retweetStatus(statusModel.getId());
    }

    /**
     * Perform quote action
     * @param mActivity - calling activity
     * @param statusModel - current tweet
     */
    public static void quote(Activity mActivity, StatusModel statusModel) {
        Flags.CURRENT_COMPOSE = Flags.COMPOSE_QUOTE;
        AppData.CURRENT_STATUS_MODEL = statusModel;

        mActivity.startActivity(new Intent(mActivity.getApplicationContext(), ComposeActivity.class));
        mActivity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_no_animation);
    }
}
