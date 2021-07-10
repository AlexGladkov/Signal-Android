package solonsky.signal.twitter.libs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * Created by neura on 29.05.17.
 */

public class CustomDialog extends Dialog implements android.view.View.OnClickListener {

    public Activity activity;
    public Dialog dialog;

    public CustomDialog(@NonNull Context context) {
        super(context);
    }


    @Override
    public void onClick(View v) {

    }
}
