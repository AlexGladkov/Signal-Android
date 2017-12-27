package solonsky.signal.twitter.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.io.IOException;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.databinding.ActivityLoginBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.helpers.FileNames;
import solonsky.signal.twitter.helpers.FileWork;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.models.ConfigurationUserModel;
import solonsky.signal.twitter.models.User;
import solonsky.signal.twitter.viewmodels.LoginViewModel;
import twitter4j.AsyncTwitter;
import twitter4j.ResponseList;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 22.05.17.
 */

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "LOGINACTIVITY";
    private LoginActivity mActivity;
    private TwitterAuthClient twitterAuthClient;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig.newBuilder(this)
                    .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                    .setResizeAndRotateEnabledForNetwork(true)
                    .setDownsampleEnabled(true)
                    .build();
            Fresco.initialize(this, imagePipelineConfig);
            Log.e(TAG, "Fresco successfully init");
        } catch (Exception e) {
            Log.e(TAG, "Fresco wasnt init " + e.getLocalizedMessage());
        }

        try {
            Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
            Reservoir.init(getApplicationContext(), 50 * 1024 * 1024, gson);
            Log.e(TAG, "Cache successfully init");
        } catch (IOException e) {
            Log.e(TAG, "Failure init cache - " + e.getLocalizedMessage());
        }

        final FileWork fileWork = new FileWork(getApplicationContext());

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.parseColor("#33000000"));

        if (!fileWork.readFromFile(FileNames.CLIENT_SECRET).equals("") &&
                !fileWork.readFromFile(FileNames.CLIENT_TOKEN).equals("")) {
            AppData.CLIENT_TOKEN = (fileWork.readFromFile(FileNames.CLIENT_TOKEN));
            AppData.CLIENT_SECRET = (fileWork.readFromFile(FileNames.CLIENT_SECRET));
            startActivity(new Intent(getApplicationContext(), LoggedActivity.class));
            finish();
        }

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(AppData.CONSUMER_KEY, AppData.CONSUMER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mActivity = this;

        binding.btnLoginTwitter.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                final Handler handler = new Handler();
                TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                final TwitterAuthToken authToken = session.getAuthToken();
                AppData.CLIENT_TOKEN = (authToken.token);
                AppData.CLIENT_SECRET = (authToken.secret);

                final AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
                asyncTwitter.addListener(new TwitterAdapter() {
                    @Override
                    public void lookedupUsers(ResponseList<twitter4j.User> users) {
                        super.lookedupUsers(users);

                        /* Save user for next performance */
                        User user = User.getFromUserInstance(users.get(0));
                        AppData.ME = (user);
                        fileWork.writeToFile(String.valueOf(AppData.ME.getId()), FileNames.USERS_LAST_ID);
                        fileWork.writeToFile(authToken.token, FileNames.CLIENT_TOKEN);
                        fileWork.writeToFile(authToken.secret, FileNames.CLIENT_SECRET);

                        /* Create configuration user for notifications and bottom tabs */
                        AppData.userConfiguration = (ConfigurationUserModel.getDefaultInstance(user,
                                AppData.CONSUMER_KEY, AppData.CONSUMER_SECRET, authToken.token, authToken.secret));
                        AppData.configurationUserModels.add(AppData.userConfiguration);
                        ConfigurationUserModel.saveData();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Reservoir.put(Cache.Me + AppData.ME.getId(), AppData.ME);
                                } catch (IOException e) {
                                    Log.e(TAG, "Error saving me - " + e.getLocalizedMessage());
                                }

                                startActivity(new Intent(getApplicationContext(), LoggedActivity.class));
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onException(twitter4j.TwitterException te, TwitterMethod method) {
                        super.onException(te, method);
                    }
                });

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            asyncTwitter.lookupUsers(asyncTwitter.getId());
                        } catch (twitter4j.TwitterException e) {
                            Log.e(TAG, "Error loading user - " + e.getLocalizedMessage());
                        }
                    }
                }).start();
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getApplicationContext(), "Authorization failed", Toast.LENGTH_SHORT).show();
            }
        });

        binding.setModel(new LoginViewModel());
        binding.setClick(new LoginViewModel.LoginClickHandler() {
            @Override
            public void onLoginClick(View v) {
                binding.btnLoginTwitter.performClick();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        binding.btnLoginTwitter.onActivityResult(requestCode, resultCode, data);
    }
}
