package solonsky.signal.twitter.libs;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import solonsky.signal.twitter.R;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * Created by kmoaz on 01.09.2017.
 */

public class DownloadFiles {
    private static final String TAG = DownloadFiles.class.getSimpleName();
    private Activity mActivity;

    private String url;
    private String path;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_DATA = 1;

    private long enqueue;
    private DownloadManager dm;

    public DownloadFiles (Activity mActivity) {
        this.mActivity = mActivity;
    }

    public void saveFile (String url, String path) {
        this.url = url;
        this.path = path;

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);

                    MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.save);
                    mediaPlayer.start();
                    Toast.makeText(mActivity.getApplicationContext(),
                            mActivity.getString(R.string.success_download), Toast.LENGTH_SHORT).show();

                    try {
                        Cursor c = dm.query(query);
                        if (c.moveToFirst()) {
                            int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                            if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                                String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                                Toast.makeText(mActivity.getApplicationContext(),
                                        mActivity.getString(R.string.success_download), Toast.LENGTH_SHORT).show();
                                //imageView.setImageURI(Uri.parse(uriString));
                            }
                        }
                    } catch (NullPointerException e) {
                        Log.e(TAG, "Error saving image");
                    }
                }
            }
        };

        mActivity.getApplicationContext().registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_DATA);
            }
        } else {
            file_download(url, path);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_DATA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    file_download(url, path);

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void file_download(String uRl, String dir) {
        File direct = new File(Environment.getExternalStorageDirectory() + dir);

        if (!direct.exists()) {
            direct.mkdirs();
        }

        String[] filename = uRl.split("/");

        dm = (DownloadManager) mActivity.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uRl));

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(filename[filename.length - 1])
                .setDescription("Something useful. No, really.")
                .setDestinationInExternalPublicDir(dir, filename[filename.length - 1]);

        dm.enqueue(request);
    }
}
