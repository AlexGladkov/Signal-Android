package solonsky.signal.twitter.libs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by kmoaz on 06.09.2017.
 */

public class ShareContent{
    private Activity mActivity;
    private TargetChosenReceiver targetChosenReceiver;

    public ShareContent (Activity mActivity) {
        this.mActivity = mActivity;
    }

    public void shareTextWithApp(String text, String packageName, String packageActivity) {
        try {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setClassName(packageName, packageActivity);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
            mActivity.startActivity(sharingIntent);
        } catch (Exception e) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(packageName));
            mActivity.startActivity(i);

        }
    }

    public void shareText (String text, String url, TargetChosenReceiver.IntentCallback callback) {
        final Intent textIntent = new Intent()
                .setAction(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, !text.isEmpty() & !url.isEmpty() ? text + "\n" + url : text.isEmpty() ? url : text);


        targetChosenReceiver = new TargetChosenReceiver();
        targetChosenReceiver.setCallback(callback);
        targetChosenReceiver.sendChooserIntent(mActivity, textIntent);
    }

    public void shareImageGif (Bitmap bitmap, TargetChosenReceiver.IntentCallback callback) {
        Intent imageIntent = new Intent()
                .setAction(Intent.ACTION_SEND)
                .setType("image/*")
                .putExtra(Intent.EXTRA_STREAM, Uri.parse(MediaStore.Images.Media.insertImage(mActivity.getBaseContext().getContentResolver(), bitmap, "title", null)));


        targetChosenReceiver = new TargetChosenReceiver();
        targetChosenReceiver.setCallback(callback);
        targetChosenReceiver.sendChooserIntent(mActivity, imageIntent);
    }

    public void shareImageGifWithText (Bitmap bitmap, String text, TargetChosenReceiver.IntentCallback callback) {
        Intent imageTextIntent = new Intent()
            .setAction(Intent.ACTION_SEND)
            .setType("image/*")
            .putExtra(Intent.EXTRA_TEXT, text)
            .putExtra(Intent.EXTRA_STREAM, Uri.parse(MediaStore.Images.Media.insertImage(mActivity.getBaseContext().getContentResolver(), bitmap, "title", null)))
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


        targetChosenReceiver = new TargetChosenReceiver();
        targetChosenReceiver.setCallback(callback);
        targetChosenReceiver.sendChooserIntent(mActivity, imageTextIntent);
    }

    /*public void shareVideo (String path) {
        Intent videoIntent = new Intent()
                .setAction(Intent.ACTION_SEND)
                .setType("video/*")
                .putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
        mActivity.startActivity(Intent.createChooser(videoIntent, "Share..."));
    }

    public void shareVideoWithText (String path, String text) {
        Intent videoTextIntent = new Intent()
                .setAction(Intent.ACTION_SEND)
                .setType("video/*")
                .putExtra(Intent.EXTRA_TEXT, text)
                .putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mActivity.startActivity(Intent.createChooser(videoTextIntent, "Share..."));
    }

    public Uri chooseImage () {
        Intent chooseIntent = new Intent();
        chooseIntent.setAction(Intent.ACTION_PICK);
        chooseIntent.setType("image/*, video/*");
        mActivity.startActivity(Intent.createChooser(chooseIntent, "Choose your media file..."));

        return chooseIntent.getData();
    }*/
}
