package solonsky.signal.twitter.dialogs;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.ComposeActivity;
import solonsky.signal.twitter.data.MuteData;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.models.RemoveModel;

/**
 * Created by neura on 18.09.17.
 */

public class HashtagDialog {
    private final String TAG = HashtagDialog.class.getSimpleName();
    private String hashtagText;
    private Activity mActivity;
    private MaterialDialog mDialog;

    public HashtagDialog(String hashtagText, Activity mActivity) {
        this.hashtagText = hashtagText;
        this.mActivity = mActivity;
        createDialog();
    }

    private void createDialog() {
        mDialog = new MaterialDialog.Builder(mActivity)
                .customView(App.getInstance().isNightEnabled() ?
                        R.layout.dialog_dark_hashtag : R.layout.dialog_light_hashtag, false)
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

        View view = mDialog.getView();
        TextView title = (TextView) view.findViewById(R.id.dialog_hashtag_title);
        title.setText(hashtagText);

        view.findViewById(R.id.dialog_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)
                        mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(mActivity.getString(R.string.app_name), hashtagText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mActivity, mActivity.getString(R.string.success_copy), Toast.LENGTH_SHORT).show();
                mDialog.dismiss();
            }
        });

        view.findViewById(R.id.dialog_tweet_tag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Flags.CURRENT_COMPOSE = Flags.COMPOSE_HASHTAG;
                AppData.COMPOSE_HASHTAG = hashtagText;
                mActivity.startActivity(new Intent(mActivity, ComposeActivity.class));
                mDialog.dismiss();
            }
        });

        view.findViewById(R.id.dialog_hash_mute).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Handler handler = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!MuteData.getInstance().isCacheLoaded()) {
                            MuteData.getInstance().loadCache();
                        }

                        RemoveModel removeModel = new RemoveModel(0, hashtagText);
                        if (!MuteData.getInstance().getmHashtagsList().contains(removeModel))
                            MuteData.getInstance().getmHashtagsList().add(0, removeModel);

                        MuteData.getInstance().saveCache();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mActivity, mActivity.getString(R.string.success_mute), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
                mDialog.dismiss();
            }
        });
    }

    public void show() {
        try {
            mDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Too hard architecture - " + e.getLocalizedMessage());
        }
    }
}
