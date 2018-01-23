package solonsky.signal.twitter.helpers;

import android.app.Application;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.di.AppComponent;
import solonsky.signal.twitter.di.DaggerAppComponent;
import solonsky.signal.twitter.libs.AppVisibilityDetector;
import solonsky.signal.twitter.libs.ApplicationLifecycleHandler;
import solonsky.signal.twitter.models.ConfigurationModel;
import solonsky.signal.twitter.room.AppDatabase;
import solonsky.signal.twitter.room.RoomContract;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by neura on 10.06.17.
 */

public class App extends Application {
    public static final String TAG = "App";
    private static volatile App instance;
    private boolean isNightEnabled = false;
    private AppComponent appComponent;

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

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL( "CREATE TABLE " + RoomContract.USER_ID_TABLE + " " +
                    "(id INTEGER NOT NULL, id_keys TEXT, PRIMARY KEY(id))");
        }
    };

    public static AppDatabase db;
    @Override
    public void onCreate() {
        super.onCreate();
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class,
                RoomContract.DB_NAME)
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build();

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

    public AppComponent getAppComponent() {
        if (appComponent == null) {
            appComponent = DaggerAppComponent.builder().build();
        }
        return appComponent;
    }

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
