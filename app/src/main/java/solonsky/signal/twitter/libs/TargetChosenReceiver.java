package solonsky.signal.twitter.libs;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created by kmoaz on 21.09.2017.
 */

public class TargetChosenReceiver extends BroadcastReceiver {
    public interface IntentCallback {
        void getComponentName (String componentName);
    }

    private static IntentCallback intentCallback;

    public void setCallback (IntentCallback intentCallback) {
        this.intentCallback = intentCallback;
    }

    public IntentCallback getIntentCallback () {
        return intentCallback;
    }

    private static final String EXTRA_RECEIVER_TOKEN = "receiver_token";
    private static final Object LOCK = new Object();

    private static String sTargetChosenReceiveAction;
    private static TargetChosenReceiver sLastRegisteredReceiver;

    public void sendChooserIntent(Activity activity, Intent sharingIntent) {
        synchronized (LOCK) {
            if (sTargetChosenReceiveAction == null) {
                sTargetChosenReceiveAction = activity.getPackageName() + "/" + TargetChosenReceiver.class.getName() + "_ACTION";
            }
            Context context = activity.getApplicationContext();
            if (sLastRegisteredReceiver != null) {
                context.unregisterReceiver(sLastRegisteredReceiver);
            }
            sLastRegisteredReceiver = new TargetChosenReceiver();
            context.registerReceiver(
                    sLastRegisteredReceiver, new IntentFilter(sTargetChosenReceiveAction));
        }

        Intent intent = new Intent(sTargetChosenReceiveAction);
        intent.setPackage(activity.getPackageName());
        intent.putExtra(EXTRA_RECEIVER_TOKEN, sLastRegisteredReceiver.hashCode());
        final PendingIntent callback = PendingIntent.getBroadcast(activity, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        Intent chooserIntent = Intent.createChooser(sharingIntent, "Share", callback.getIntentSender());
        activity.startActivity(chooserIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String TAG = TargetChosenReceiver.class.getSimpleName();
        synchronized (LOCK) {
            if (sLastRegisteredReceiver != this) return;
            context.getApplicationContext().unregisterReceiver(sLastRegisteredReceiver);
            sLastRegisteredReceiver = null;
        }

        if (!intent.hasExtra(EXTRA_RECEIVER_TOKEN) || intent.getIntExtra(EXTRA_RECEIVER_TOKEN, 0) != this.hashCode()) {
            return;
        }

        if (intentCallback != null) intentCallback.getComponentName(intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT).toString());
    }
}
