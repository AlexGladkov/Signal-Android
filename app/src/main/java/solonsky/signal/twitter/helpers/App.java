package solonsky.signal.twitter.helpers;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.libs.AppVisibilityDetector;
import solonsky.signal.twitter.libs.ApplicationLifecycleHandler;
import solonsky.signal.twitter.models.ConfigurationModel;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by neura on 10.06.17.
 */

public class App extends Application {
    public static final String TAG = "App";
    private static volatile App instance;
    private boolean isNightEnabled = false;

    private final String CONSUMER_KEY = "5OPDgxPnwuMkhFtkZfJmNDJj7";
    private final String CONSUMER_SECRET = "q2vCmYnI2h3ah2pjzAQYwipiQVskEZjkFAH83wJ3OaDglhDcaF";
    private static boolean mIsBackground = false;
    private static ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

    public static App getInstance() {
        App localInstance = instance;
        if (localInstance == null) {
            synchronized (App.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new App();
                }
            }
        }

        return localInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AppVisibilityDetector.init(App.this, new AppVisibilityDetector.AppVisibilityCallback() {
            @Override
            public void onAppGotoForeground() {
                Log.e(TAG, "Go to foreground");
                App.this.setmIsBackground(false);
            }
            @Override
            public void onAppGotoBackground() {
                Log.e(TAG, "Go to background");
                App.this.setmIsBackground(true);
            }
        });

        //We load the Night State here
        SharedPreferences mSharedPreferences = getSharedPreferences(getString(R.string.app_name), 0);
        this.isNightEnabled = mSharedPreferences.getBoolean("NIGHT_MODE", true);
    }

//    public void registerCycleListener() {
//        Log.e(TAG, "LifeCycler init");
//        ApplicationLifecycleHandler handler = new ApplicationLifecycleHandler();
//        registerActivityLifecycleCallbacks(handler);
//        registerComponentCallbacks(handler);
//    }

    public boolean isNightEnabled() {
        return isNightEnabled;
    }
    public void setNightEnabled(boolean nightEnabled) {
        isNightEnabled = nightEnabled;
    }

    public boolean ismIsBackground() {
        return mIsBackground;
    }

    public void setmIsBackground(boolean mIsBackground) {
        this.mIsBackground = mIsBackground;
    }

    public ConfigurationBuilder getConfigurationBuilder() {
        return configurationBuilder;
    }
}
