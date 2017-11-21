package solonsky.signal.twitter.dialogs;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Collections;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.ComposeActivity;
import solonsky.signal.twitter.data.ShareData;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.libs.ShareContent;
import solonsky.signal.twitter.libs.TargetChosenReceiver;

/**
 * Created by neura on 18.09.17.
 */

public class UrlDialog {
    private final String TAG = UrlDialog.class.getSimpleName();
    private String urlText;
    private Activity mActivity;
    private MaterialDialog mDialog;

    public UrlDialog(String urlText, Activity mActivity) {
        this.urlText = urlText;
        this.mActivity = mActivity;
        createDialog();
    }

    private void createDialog() {
        mDialog = new MaterialDialog.Builder(mActivity)
                .customView(App.getInstance().isNightEnabled() ?
                        R.layout.dialog_dark_link : R.layout.dialog_light_link, false)
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

        final ShareContent shareContent = new ShareContent(mActivity);

        View view = mDialog.getView();

        TextView title = (TextView) view.findViewById(R.id.dialog_title);
        title.setText(urlText);

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.openLink(urlText, mActivity);
            }
        });

        view.findViewById(R.id.dialog_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)
                        mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(mActivity.getString(R.string.app_name), urlText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mActivity.getApplicationContext(),
                        mActivity.getString(R.string.success_copy), Toast.LENGTH_SHORT).show();
                if (mDialog != null) mDialog.dismiss();
            }
        });

        view.findViewById(R.id.dialog_tweet_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Flags.CURRENT_COMPOSE = Flags.COMPOSE_LINK;
                AppData.COMPOSE_LINK = urlText;
                mActivity.startActivity(new Intent(mActivity.getApplicationContext(), ComposeActivity.class));
                if (mDialog != null) mDialog.dismiss();
            }
        });

        if (!ShareData.getInstance().isCacheLoaded()) {
            ShareData.getInstance().loadCache();
        }

        ImageView fastShare1 = (ImageView) view.findViewById(R.id.dialog_img_share_1);
        ImageView fastShare2 = (ImageView) view.findViewById(R.id.dialog_img_share_2);
        ImageView fastShare3 = (ImageView) view.findViewById(R.id.dialog_img_share_3);


        switch (ShareData.getInstance().getShares().size()) {
            case 0:
                fastShare1.setVisibility(View.GONE);
                fastShare2.setVisibility(View.GONE);
                fastShare3.setVisibility(View.GONE);
                break;

            case 1:
                try {
                    Drawable icon1 = mActivity.getPackageManager().getApplicationIcon(
                            ShareData.getInstance().getShares().get(0).split("/")[0].replace("ComponentInfo{", ""));
                    fastShare1.setImageDrawable(icon1);
                    fastShare1.setVisibility(View.VISIBLE);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "Error creating icon - " + e.getLocalizedMessage());
                }

                fastShare2.setVisibility(View.GONE);
                fastShare3.setVisibility(View.GONE);
                break;

            case 2:
                try {
                    Drawable icon1 = mActivity.getPackageManager().getApplicationIcon(
                            ShareData.getInstance().getShares().get(0).split("/")[0].replace("ComponentInfo{", ""));
                    fastShare1.setImageDrawable(icon1);
                    fastShare1.setVisibility(View.VISIBLE);

                    Drawable icon2 = mActivity.getPackageManager().getApplicationIcon(
                            ShareData.getInstance().getShares().get(1).split("/")[0].replace("ComponentInfo{", ""));
                    fastShare2.setImageDrawable(icon2);
                    fastShare2.setVisibility(View.VISIBLE);
                } catch (PackageManager.NameNotFoundException e) {
                    // Do nothing
                }

                fastShare3.setVisibility(View.GONE);
                break;

            case 3:
                try {
                    Drawable icon1 = mActivity.getPackageManager().getApplicationIcon(
                            ShareData.getInstance().getShares().get(0).split("/")[0].replace("ComponentInfo{", ""));
                    fastShare1.setImageDrawable(icon1);
                    fastShare1.setVisibility(View.VISIBLE);

                    Drawable icon2 = mActivity.getPackageManager().getApplicationIcon(
                            ShareData.getInstance().getShares().get(1).split("/")[0].replace("ComponentInfo{", ""));
                    fastShare2.setImageDrawable(icon2);
                    fastShare2.setVisibility(View.VISIBLE);

                    Drawable icon3 = mActivity.getPackageManager().getApplicationIcon(
                            ShareData.getInstance().getShares().get(2).split("/")[0].replace("ComponentInfo{", ""));
                    fastShare3.setImageDrawable(icon3);
                    fastShare3.setVisibility(View.VISIBLE);
                } catch (PackageManager.NameNotFoundException e) {
                    // Do nothing
                }
                break;
        }

        view.findViewById(R.id.ll_dialog_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContent.shareText(urlText, "", new TargetChosenReceiver.IntentCallback() {
                    @Override
                    public void getComponentName(String componentName) {
                        ShareData.getInstance().addShare(componentName);
                        ShareData.getInstance().saveCache();
                    }
                });
                if (mDialog != null) mDialog.dismiss();
            }
        });

        view.findViewById(R.id.dialog_img_share_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName = ShareData.getInstance().getShares().get(0).split("/")[0].replace("ComponentInfo{", "");
                String packageActivity = ShareData.getInstance().getShares().get(0).split("/")[1].replace("}", "");
                shareContent.shareTextWithApp(urlText, packageName, packageActivity);
                if (mDialog != null) mDialog.dismiss();
            }
        });

        view.findViewById(R.id.dialog_img_share_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName = ShareData.getInstance().getShares().get(1).split("/")[0].replace("ComponentInfo{", "");
                String packageActivity = ShareData.getInstance().getShares().get(1).split("/")[1].replace("}", "");
                shareContent.shareTextWithApp(urlText, packageName, packageActivity);
                Collections.swap(ShareData.getInstance().getShares(), 1, 0);
                if (mDialog != null) mDialog.dismiss();
            }
        });

        view.findViewById(R.id.dialog_img_share_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName = ShareData.getInstance().getShares().get(2).split("/")[0].replace("ComponentInfo{", "");
                String packageActivity = ShareData.getInstance().getShares().get(2).split("/")[1].replace("}", "");
                shareContent.shareTextWithApp(urlText, packageName, packageActivity);
                Collections.swap(ShareData.getInstance().getShares(), 2, 1);
                Collections.swap(ShareData.getInstance().getShares(), 1, 0);
                if (mDialog != null) mDialog.dismiss();
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
