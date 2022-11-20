package solonsky.signal.twitter.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Collections;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.data.ShareData;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.libs.DownloadFiles;
import solonsky.signal.twitter.libs.ShareContent;
import solonsky.signal.twitter.libs.TargetChosenReceiver;

/**
 * Created by neura on 20.09.17.
 */

public class MediaDialog {
    private final String TAG = MediaDialog.class.getSimpleName();
    private String urlString;
    private Activity mActivity;
    private MaterialDialog mDialog;
    private boolean isVideo;

    public MediaDialog(String urlString, Activity mActivity, boolean isVideo) {
        this.urlString = urlString;
        this.mActivity = mActivity;
        this.isVideo = isVideo;
        createDialog();
    }

    private void createDialog() {
        mDialog = new MaterialDialog.Builder(mActivity)
                .customView(App.getInstance().isNightEnabled() ?
                        R.layout.dialog_dark_media : R.layout.dialog_light_media, false)
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
        TextView title = (TextView) view.findViewById(R.id.dialog_title);

        title.setText(urlString);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.openLink(urlString, mActivity);
            }
        });

        TextView mBtnSaveImage = (TextView) view.findViewById(R.id.dialog_save_image);
        TextView mBtnSaveVideo = (TextView) view.findViewById(R.id.dialog_save_video);
        LinearLayout mBtnShare = (LinearLayout) view.findViewById(R.id.ll_dialog_share);

        mBtnSaveImage.setVisibility(isVideo ? View.GONE : View.VISIBLE);
        mBtnSaveVideo.setVisibility(isVideo ? View.VISIBLE : View.GONE);

        final ShareContent shareContent = new ShareContent(mActivity);

        mBtnSaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadFiles downloadFiles = new DownloadFiles(mActivity);
                downloadFiles.saveFile(urlString, mActivity.getString(R.string.download_url));
                if (mDialog != null) mDialog.dismiss();
            }
        });

        mBtnSaveVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadFiles downloadFiles = new DownloadFiles(mActivity);
                downloadFiles.saveFile(urlString, mActivity.getString(R.string.download_url));
                if (mDialog != null) mDialog.dismiss();
            }
        });

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

        view.findViewById(R.id.dialog_img_share_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName = ShareData.getInstance().getShares().get(0).split("/")[0].replace("ComponentInfo{", "");
                String packageActivity = ShareData.getInstance().getShares().get(0).split("/")[1].replace("}", "");
                shareContent.shareTextWithApp(urlString, packageName, packageActivity);
                if (mDialog != null) mDialog.dismiss();
            }
        });

        view.findViewById(R.id.dialog_img_share_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName = ShareData.getInstance().getShares().get(1).split("/")[0].replace("ComponentInfo{", "");
                String packageActivity = ShareData.getInstance().getShares().get(1).split("/")[1].replace("}", "");
                shareContent.shareTextWithApp(urlString, packageName, packageActivity);
                Collections.swap(ShareData.getInstance().getShares(), 1, 0);
                if (mDialog != null) mDialog.dismiss();
            }
        });

        view.findViewById(R.id.dialog_img_share_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName = ShareData.getInstance().getShares().get(2).split("/")[0].replace("ComponentInfo{", "");
                String packageActivity = ShareData.getInstance().getShares().get(2).split("/")[1].replace("}", "");
                shareContent.shareTextWithApp(urlString, packageName, packageActivity);
                Collections.swap(ShareData.getInstance().getShares(), 2, 1);
                Collections.swap(ShareData.getInstance().getShares(), 1, 0);
                if (mDialog != null) mDialog.dismiss();
            }
        });

        mBtnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareContent.shareText(urlString, "", new TargetChosenReceiver.IntentCallback() {
                    @Override
                    public void getComponentName(String componentName) {
                        ShareData.getInstance().addShare(componentName);
                        ShareData.getInstance().saveCache();
                    }
                });
                if (mDialog != null) mDialog.dismiss();
            }
        });
    }

    public void show() {
        if (mDialog != null) {
            mDialog.show();
        }
    }
}
