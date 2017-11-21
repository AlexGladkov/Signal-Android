package solonsky.signal.twitter.helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by neura on 30.07.17.
 */

public class Permission {
    public static final int LOCATION_REQUEST = 100;
    public static final int PHOTO_REQUEST = 101;
    public static final int WRITE_EXTERNAL_REQUEST = 102;

    public static boolean checkSelfPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
