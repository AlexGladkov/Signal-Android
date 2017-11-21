package solonsky.signal.twitter.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;

import solonsky.signal.twitter.adapters.StatusAdapter;
import solonsky.signal.twitter.api.DirectApi;
import solonsky.signal.twitter.data.DirectData;
import solonsky.signal.twitter.data.FeedData;
import solonsky.signal.twitter.data.LikesData;
import solonsky.signal.twitter.data.LoggedData;
import solonsky.signal.twitter.data.MentionsData;
import solonsky.signal.twitter.data.MuteData;
import solonsky.signal.twitter.data.NotificationsAllData;
import solonsky.signal.twitter.data.NotificationsFollowData;
import solonsky.signal.twitter.data.NotificationsLikeData;
import solonsky.signal.twitter.data.NotificationsReplyData;
import solonsky.signal.twitter.data.NotificationsRetweetData;
import solonsky.signal.twitter.data.SearchData;
import solonsky.signal.twitter.data.ShareData;
import solonsky.signal.twitter.data.StreamData;
import solonsky.signal.twitter.data.UsersData;
import solonsky.signal.twitter.fragments.MVPMentionsFragment;
import solonsky.signal.twitter.fragments.ProfileFragment;
import solonsky.signal.twitter.fragments.SearchContainerFragment;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.helpers.Flags;

import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;
import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.SelectorAdapter;
import solonsky.signal.twitter.databinding.ActivityLoggedBinding;
import solonsky.signal.twitter.fragments.DirectFragment;
import solonsky.signal.twitter.fragments.FeedFragment;
import solonsky.signal.twitter.fragments.LikesFragment;
import solonsky.signal.twitter.fragments.MentionsFragment;
import solonsky.signal.twitter.fragments.MuteFragment;
import solonsky.signal.twitter.fragments.NotificationsFragment;
import solonsky.signal.twitter.fragments.SearchFragment;
import solonsky.signal.twitter.fragments.SearchedFragment;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.FileNames;
import solonsky.signal.twitter.helpers.FileWork;
import solonsky.signal.twitter.helpers.NotificationHelper;
import solonsky.signal.twitter.interfaces.ActivityListener;
import solonsky.signal.twitter.interfaces.FragmentCounterListener;
import solonsky.signal.twitter.interfaces.NotificationListener;
import solonsky.signal.twitter.interfaces.UpdateHandler;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.libs.ShareContent;
import solonsky.signal.twitter.libs.bottomBar.AGBottomBar;
import solonsky.signal.twitter.libs.bottomBar.AGBottomBarItem;
import solonsky.signal.twitter.libs.bottomBar.AGBottomBarSingleItem;
import solonsky.signal.twitter.libs.bottomBar.AGBottomBarMultipleItem;
import solonsky.signal.twitter.models.ConfigurationModel;
import solonsky.signal.twitter.models.ConfigurationUserModel;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.models.UserModel;
import solonsky.signal.twitter.overlays.AccountSwitcherOverlay;
import solonsky.signal.twitter.services.MyLocationService;
import solonsky.signal.twitter.services.StreamService;
import solonsky.signal.twitter.viewmodels.LoggedViewModel;
import twitter4j.AsyncTwitter;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.User;

public class LoggedActivity extends AppCompatActivity implements ActivityListener {

    public final int REQUEST_LOCATION_CODE = 1;
    private final String TAG = "LOGGEDACTIVITY";
    public ActivityLoggedBinding binding;
    public LoggedViewModel viewModel;
    private SelectorAdapter mAdapter;
    private Gson gson = new Gson();

    private int BOTTOM_SHADOW_HEIGHT = 1;
    private int BOTTOM_SHADOW_DEFAULT_MARGIN = 47;

    private FeedFragment feedFragment;
    private MVPMentionsFragment mentionsFragment;
    private NotificationsFragment notificationsFragment;
    private LikesFragment likesFragment;
    private DirectFragment directFragment;
    private SearchFragment searchFragment;
    private ProfileFragment profileFragment;
    private MuteFragment muteFragment;

    private AGBottomBar bottomBar;
    private LoggedActivity mActivity;
    private Fragment lastFragment = null;
    private int feedCount = 0;
    private int previousTBState = AppData.TOOLBAR_LOGGED_MAIN;

    private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            App.getInstance().setNightEnabled(!App.getInstance().isNightEnabled());
            startActivity(new Intent(getApplicationContext(), LoggedActivity.class));
            LoggedActivity.this.finish();
            return true;
        }
    });

    public ShareContent shareContent;

    public void prepareToRecreate(boolean isChange) {
        if (isChange) {
            AppData.lastSwitchTime = System.currentTimeMillis();

            DirectData.setInstance(null);
            FeedData.setInstance(null);
            LikesData.setInstance(null);
            MentionsData.Companion.getInstance().clear();
            MuteData.setInstance(null);
            NotificationsAllData.setInstance(null);
            NotificationsFollowData.setInstance(null);
            NotificationsLikeData.setInstance(null);
            NotificationsRetweetData.setInstance(null);
            NotificationsReplyData.setInstance(null);
            SearchData.setInstance(null);
            UsersData.setInstance(null);
            StreamData.getInstance().endStream();
            StreamData.setInstance(null);
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (feedFragment != null && feedFragment.isAdded())
            fragmentTransaction.remove(feedFragment);
        if (mentionsFragment != null && mentionsFragment.isAdded())
            fragmentTransaction.remove(mentionsFragment);
        if (notificationsFragment != null && notificationsFragment.isAdded())
            fragmentTransaction.remove(notificationsFragment);
        if (likesFragment != null && likesFragment.isAdded())
            fragmentTransaction.remove(likesFragment);
        if (searchFragment != null && searchFragment.isAdded())
            fragmentTransaction.remove(searchFragment);
        if (directFragment != null && directFragment.isAdded())
            fragmentTransaction.remove(directFragment);
        if (muteFragment != null && muteFragment.isAdded())
            fragmentTransaction.remove(muteFragment);
        if (profileFragment != null && profileFragment.isAdded())
            fragmentTransaction.remove(profileFragment);
        fragmentTransaction.commit();
        System.gc();
    }

    @Override
    public void updateCounter(int count) {
        viewModel.setStatusType(count > 0 ?
                Flags.STATUS_TYPE.COUNTER : Flags.STATUS_TYPE.TITLE);
        viewModel.setFeedCount(count);
    }

    @Override
    public void updateStatusType(Flags.STATUS_TYPE statusType) {
        viewModel.setStatusType(statusType);
    }

    @Override
    public void updateToolbarState(int state, @ColorRes int statusBarColor) {
        viewModel.setToolbarState(state);
        setStatusBarColor(statusBarColor);
    }

    @Override
    public void updateBars(int dy) {
        hidePopup();
        if (!AppData.appConfiguration.isStaticBottomBar() && !viewModel.isStaticBottomBar()) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.agLoggedBottom.getLayoutParams();
            int bottomDiff = params.bottomMargin - dy;
            params.bottomMargin = bottomDiff >= 0 ? 0 : bottomDiff < -binding.agLoggedBottom.getHeight() ?
                    -binding.agLoggedBottom.getHeight() : bottomDiff;
            binding.agLoggedBottom.setLayoutParams(params);

            int startPosition = (int) Utilities.convertDpToPixel(47, getApplicationContext());
            params = (RelativeLayout.LayoutParams) binding.viewLoggedBottomShadow.getLayoutParams();
            bottomDiff = params.bottomMargin - dy;
            params.bottomMargin = bottomDiff >= startPosition ? startPosition :
                    bottomDiff < -binding.viewLoggedBottomShadow.getHeight() ? -binding.viewLoggedBottomShadow.getHeight() : bottomDiff;
            binding.viewLoggedBottomShadow.setLayoutParams(params);
        }

        if (!AppData.appConfiguration.isStaticTopBars() && !viewModel.isStaticToolbar()) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.tbLogged.getLayoutParams();
            RelativeLayout.LayoutParams searchParams = (RelativeLayout.LayoutParams) binding.tbSearch.getLayoutParams();
            int toolbarHeight = (int) Utilities.convertDpToPixel(80, getApplicationContext());
            int dropShadowHeight = (int) Utilities.convertDpToPixel(4, getApplicationContext());
            int topDiff = params.topMargin - dy;

            params.topMargin = topDiff > 0 ? 0 : topDiff <= -(toolbarHeight + dropShadowHeight) ? -(toolbarHeight + dropShadowHeight) : topDiff;
            searchParams.topMargin = topDiff > 0 ? 0 : topDiff <= -(toolbarHeight + dropShadowHeight) ? -(toolbarHeight + dropShadowHeight) : topDiff;
            binding.tbLogged.setLayoutParams(params);
            binding.tbSearch.setLayoutParams(searchParams);

            params = (RelativeLayout.LayoutParams) binding.viewLoggedDropShadow.getLayoutParams();
            topDiff = params.topMargin - dy;
            params.topMargin = topDiff > toolbarHeight ? toolbarHeight : topDiff <= -dropShadowHeight ? -dropShadowHeight : topDiff;
            binding.viewLoggedDropShadow.setLayoutParams(params);
        }

        isDown = !isDown;
    }

    @Override
    public void updateSettings(int title, boolean isStaticTop, boolean isStaticBottom) {
        endSearching(false);
        resetBars();
        viewModel.setStaticBottomBar(isStaticBottom);
        viewModel.setStaticToolbar(isStaticTop);
        viewModel.setTitle(getString(title));
    }

    @Override
    public void updateTitle(int title) {
        viewModel.setTitle(getString(title));
    }

    @Override
    public void onStartSearch(String searchQuery) {
        Utilities.hideKeyboard(mActivity);
        Cache.saveRecentSearch(searchQuery);
        mActivity.binding.txtLoggedSearch.setText(searchQuery);
        mActivity.binding.rlLoggedContainer.requestFocus();
        mActivity.viewModel.setSearch(true);

        if (mActivity.binding.flLoggedSearch.getVisibility() == View.VISIBLE) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fl_logged_search, new SearchedFragment()).commit();
        } else {
            previousTBState = mActivity.viewModel.getToolbarState();
            mActivity.viewModel.setToolbarState(AppData.TOOLBAR_LOGGED_SEARCHED);
            getSupportFragmentManager().beginTransaction().replace(R.id.fl_logged_search, new SearchedFragment()).commit();

            binding.flLoggedSearch.setVisibility(View.VISIBLE);
            binding.flLoggedSearch.animate().translationX(Utilities.getScreenWidth(mActivity)).setDuration(0).start();
            binding.flLoggedSearch.animate().translationX(0).setDuration(200).start();

            binding.flLoggedMain.animate().setDuration(200).alpha(0.7f).translationX(-(Utilities.getScreenWidth(mActivity) * 0.1f))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            binding.flLoggedMain.setVisibility(View.GONE);
                        }
                    });
        }
    }

    @Override
    public void onEndSearch() {

    }

    @Override
    public Flags.STATUS_TYPE checkState() {
        return viewModel.getStatusType();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
            Reservoir.init(getApplicationContext(), 50 * 1024 * 1024, gson);
            Log.e(TAG, "Cache successfully init");
        } catch (IOException e) {
            Log.e(TAG, "Failure init cache - " + e.getLocalizedMessage());
        }

        if (savedInstanceState == null) {
            loadSettings(); // load app settings
            loadProfile(); // load current user configuration
            loadConfigurations(); // load users configuration

            AppData.lastSwitchTime = System.currentTimeMillis();
            ShareData.getInstance().loadCache(); // Load shares
        }

        shareContent = new ShareContent(this);

        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDark);
        }

//        Toast.makeText(this, getMessageText(getIntent()), Toast.LENGTH_SHORT).show();

//        nReceiver = new NotificationReceiver();
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("com.kpbird.nlsexample.NOTIFICATION_LISTENER_EXAMPLE");
//        registerReceiver(nReceiver, filter);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_logged);
        UsersData.getInstance().init();
        LoggedData.getInstance().loadCache(); // Load bullets

        binding.tbLogged.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        binding.tbLoggedMute.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        binding.txtLoggedFeedCount.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        binding.txtLoggedFeedCount.startAnimation(
                                AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down));
                        break;

                    case MotionEvent.ACTION_UP:
                        binding.txtLoggedFeedCount.clearAnimation();
                        break;
                }
                return false;
            }
        });

        binding.imgLoggedFeedArrow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        binding.imgLoggedFeedArrow.startAnimation(
                                AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down));
                        break;

                    case MotionEvent.ACTION_UP:
                        binding.imgLoggedFeedArrow.clearAnimation();
                        break;
                }
                return false;
            }
        });

        binding.txtLoggedFeedTitle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        binding.txtLoggedFeedTitle.startAnimation(
                                AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_down));
                        break;

                    case MotionEvent.ACTION_UP:
                        binding.txtLoggedFeedTitle.clearAnimation();
                        break;
                }
                return false;
            }
        });

//        Log.e(TAG, "current name - " + AppData.userConfiguration.getUser().getName());
//        Log.e(TAG, "current client token - " + AppData.userConfiguration.getClientToken());
//        Log.e(TAG, "current client secret - " + AppData.userConfiguration.getClientSecret());
//        Log.e(TAG, "current consumer key - " + AppData.userConfiguration.getConsumerKey());
//        Log.e(TAG, "current consumer secret - " + AppData.userConfiguration.getConsumerSecret());

        AppData.CLIENT_TOKEN = AppData.userConfiguration.getClientToken();
        AppData.CLIENT_SECRET = AppData.userConfiguration.getClientSecret();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setStatusBarColor(App.getInstance().isNightEnabled() ?
                R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color);

        final AccountSwitcherOverlay switcherOverlay = new AccountSwitcherOverlay(this);
        switcherOverlay.createSwitcher();

        mActivity = this;
        viewModel = new LoggedViewModel(switcherOverlay.getmAdapter(), getApplicationContext(), feedCount);

        if (AppData.ME != null)
            viewModel.setAvatar(AppData.ME.getOriginalProfileImageURL());

        if (getIntent() != null && getIntent().getExtras() != null) {
            parseNotification(
                    Integer.valueOf(String.valueOf(getIntent().getExtras().getInt(Flags.NOTIFICATION_ID))),
                    getIntent().getExtras().get(Flags.NOTIFICATION_TYPE),
                    (String) getIntent().getExtras().get(Flags.NOTIFICATION_USERNAME),
                    Long.valueOf(String.valueOf(getIntent().getExtras().get(Flags.NOTIFICATION_STATUS_ID))));
        }

        binding.setLogged(viewModel);
        binding.setClick(new LoggedViewModel.LoggedClickHandler() {
            @Override
            public void onComposeClick(View v) {
                Flags.CURRENT_COMPOSE = Flags.COMPOSE_NONE;
                makeCompose();
                hidePopup();
//                NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
//                notificationHelper.createNotification();
            }

            @Override
            public void onUserClick(View v) {
                switcherOverlay.showOverlay();
                hidePopup();
            }

            @Override
            public void onUpdateClick(View v) {
                makeUpdate();
                hidePopup();
            }

            @Override
            public void onCancelClick(View v) {
                switcherOverlay.hideOverlay();
                hidePopup();
            }

            @Override
            public void onAddClick(View v) {
                if (!viewModel.isAdding()) {
                    binding.btnLoggedTwitter.performClick();
                }
                hidePopup();
            }

            @Override
            public void onSettingsClick(View v) {
                if (!viewModel.isAdding()) {
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
                hidePopup();
            }

            @Override
            public void onMuteAddClick(View v) {
                startActivity(new Intent(getApplicationContext(), MuteAddActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                hidePopup();
            }

            @Override
            public void onDMClick(View v) {
                startActivity(new Intent(getApplicationContext(), ChatSelectActivity.class));
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_no_animation);
                hidePopup();
            }

            @Override
            public void onBackSearchClick(View v) {
                endSearching(true);
            }
        });

        initBottomBar();
        initFragments();

        binding.btnLoggedTwitter.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                final Handler handler = new Handler();
                viewModel.setAdding(true);
                TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                final TwitterAuthToken authToken = session.getAuthToken();
                AppData.CLIENT_TOKEN = authToken.token;
                AppData.CLIENT_SECRET = authToken.secret;

                final AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
                asyncTwitter.addListener(new TwitterAdapter() {
                    @Override
                    public void onException(twitter4j.TwitterException te, TwitterMethod method) {
                        super.onException(te, method);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Error adding user", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void lookedupUsers(ResponseList<User> users) {
                        super.lookedupUsers(users);
                        FileWork fileWork = new FileWork(getApplicationContext());

                        /* Save user for next performance */
                        solonsky.signal.twitter.models.User user =
                                solonsky.signal.twitter.models.User.getFromUserInstance(users.get(0));
                        AppData.ME = user;
                        fileWork.writeToFile(String.valueOf(AppData.ME.getId()), FileNames.USERS_LAST_ID);
                        fileWork.writeToFile(authToken.token, FileNames.CLIENT_TOKEN);
                        fileWork.writeToFile(authToken.secret, FileNames.CLIENT_SECRET);

                        /* Create configuration user for notifications and bottom tabs */
                        boolean hasUser = false;

                        for (ConfigurationUserModel userModel : AppData.configurationUserModels) {
                            if (userModel.getUser().getId() == user.getId()) {
                                hasUser = true;
                                break;
                            }
                        }

                        if (!hasUser) {
                            AppData.userConfiguration = ConfigurationUserModel.getDefaultInstance(user,
                                    AppData.CONSUMER_KEY, AppData.CONSUMER_SECRET, authToken.token, authToken.secret);
                            AppData.configurationUserModels.add(AppData.userConfiguration);
                            ConfigurationUserModel.saveData();
                        }

                        prepareToRecreate(true);
                        new FileWork(getApplicationContext()).writeToFile(String.valueOf(AppData.ME.getId()),
                                FileNames.USERS_LAST_ID);

                        saveProfile();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                LoggedActivity.this.recreate();
                            }
                        });
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
            public void failure(com.twitter.sdk.android.core.TwitterException exception) {
                Log.e(TAG, "Error adding user - " + exception.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), getString(R.string.error_add_account), Toast.LENGTH_SHORT).show();
            }
        });

        LoggedData.getInstance().setUpdateHandler(new UpdateHandler() {
            @Override
            public void onUpdate() {
                bottomBar.setCaptionVisibility(LoggedData.getInstance().isNewFeed() ?
                        View.VISIBLE : View.GONE, 0);
                bottomBar.setCaptionVisibility(LoggedData.getInstance().isNewMention() ?
                        View.VISIBLE : View.GONE, 1);
                bottomBar.setCaptionVisibility(LoggedData.getInstance().isNewActivity() ?
                        View.VISIBLE : View.GONE, 2);

                for (AGBottomBarItem agBottomBarItem : bottomBar.getBarItems()) {
                    if (agBottomBarItem.getId() == 3) {
                        bottomBar.setCaptionVisibility(LoggedData.getInstance().isNewMessage() ?
                                View.VISIBLE : View.GONE, bottomBar.getBarItems().indexOf(agBottomBarItem));
                    }
                }
            }
        });

        final NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
        StreamData.getInstance().startStream();
        StreamData.getInstance().setNotificationListener(new NotificationListener() {
            @Override
            public void onCreateLikeNotification(String text, String sender, String senderScreenName,
                                                 String receiver, String avatar) {
                notificationHelper.createLikeNotification(text, sender, senderScreenName, receiver, avatar);
            }

            @Override
            public void onCreateRetweetNotification(String text, String sender, String senderScreenName,
                                                    String receiver, String avatar, long statusId) {
                notificationHelper.createRetweetNotification(text, sender, senderScreenName, receiver, avatar, statusId);
            }

            @Override
            public void onCreateQuoteNotification(String text, String sender, String senderScreenName,
                                                  String receiver, String avatar, long statusId) {
                notificationHelper.createQuoteNotification(text, sender, senderScreenName, receiver, avatar, statusId);
            }

            @Override
            public void onCreateMentionNotification(String text, String sender, String senderScreenName,
                                                    String receiver, String avatar) {
                notificationHelper.createMentionNotification(text, sender, senderScreenName, receiver, avatar);
            }

            @Override
            public void onCreateDirectNotitifcation(String text, String sender, String senderScreenName,
                                                    String receiver, String avatar) {
                notificationHelper.createDirectNotification(text, sender, senderScreenName, receiver, avatar);
            }

            @Override
            public void onCreateReplyNotification(String text, String sender, String senderScreenName,
                                                  String receiver, String avatar) {
                notificationHelper.createReplyNotification(text, sender, senderScreenName, receiver, avatar, null);
            }

            @Override
            public void onCreateFollowNotification(String sender, String senderScreenName, String receiver, String avatar) {
                notificationHelper.createFollowNotification(sender, senderScreenName, receiver, avatar);
            }

            @Override
            public void onCreateListedNotification(String listName, String sender, String senderScreenName,
                                                   String receiver, String avatar) {
                notificationHelper.createListNotification(listName, sender, senderScreenName, receiver, avatar);
            }
        });

        startService(new Intent(getApplicationContext(), MyLocationService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    /**
     * Reset bars state to default state
     */
    public void resetBars() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.agLoggedBottom.getLayoutParams();
        params.bottomMargin = 0;
        binding.agLoggedBottom.setLayoutParams(params);

        params = (RelativeLayout.LayoutParams) binding.viewLoggedBottomShadow.getLayoutParams();
        params.bottomMargin = (int) (Utilities.convertDpToPixel(BOTTOM_SHADOW_DEFAULT_MARGIN, getApplicationContext()) +
                Utilities.convertDpToPixel(BOTTOM_SHADOW_HEIGHT, getApplicationContext()));
        binding.viewLoggedBottomShadow.setLayoutParams(params);

        params = (RelativeLayout.LayoutParams) binding.tbLogged.getLayoutParams();
        params.topMargin = 0;
        binding.tbLogged.setLayoutParams(params);

        isDown = false;
    }

    /**
     * Hides top bar or bottom bar depends on configuration
     *
     * @param isHide - @true for hide
     * @link .models.ConfigurationModel
     */
    private boolean isDown = false;

    /**
     * Loading App settings into ConfigurationModel
     */
    private void loadSettings() {
        if (AppData.appConfiguration == null) {
            FileWork fileWork = new FileWork(getApplicationContext());
            String loadedConfig = fileWork.readFromFile(FileNames.APP_CONFIGURATION);

            if (loadedConfig.equals("")) {
                AppData.appConfiguration = ConfigurationModel.defaultSettings();
                fileWork.writeToFile(AppData.appConfiguration.exportConfiguration().toString(), FileNames.APP_CONFIGURATION);
            } else {
                JsonParser jsonParser = new JsonParser();
                JsonObject jsonObject = (JsonObject) jsonParser.parse(loadedConfig);
                AppData.appConfiguration = ConfigurationModel.createFromJson(jsonObject);
            }

            App.getInstance().setNightEnabled(AppData.appConfiguration.getDarkMode()
                    == ConfigurationModel.DARK_ALWAYS);
        }
    }

    /**
     * Load user's configurations
     */
    private void loadConfigurations() {
        Type resultType = new TypeToken<List<ConfigurationUserModel>>() {
        }.getType();
        try {
            List<ConfigurationUserModel> configurationUserModels = Reservoir.get(Cache.UsersConfigurations, resultType);
            List<Long> ids = new ArrayList<>();
            AppData.configurationUserModels.clear();
            Log.e(TAG, "Me id - " + AppData.ME.getId() + " Me name - " + AppData.ME.getName());
            for (ConfigurationUserModel configurationModel : configurationUserModels) {
                if (!ids.contains(configurationModel.getUser().getId())) {
                    AppData.configurationUserModels.add(configurationModel);
                    ids.add(configurationModel.getUser().getId());

                    if (AppData.ME != null && configurationModel.getUser().getId() == AppData.ME.getId()) {
                        AppData.userConfiguration = configurationModel;
                    }
                }
            }
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, "Error load configurations cache " + e.getLocalizedMessage());
            AppData.userConfiguration = ConfigurationUserModel.getDefaultInstance(AppData.ME,
                    AppData.CONSUMER_KEY, AppData.CONSUMER_SECRET, AppData.CLIENT_TOKEN, AppData.CLIENT_SECRET);
            AppData.configurationUserModels = new ArrayList<>();
            AppData.configurationUserModels.add(AppData.userConfiguration);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        binding.btnLoggedTwitter.onActivityResult(requestCode, resultCode, data);
    }

    public void saveProfile() {
        final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        final FileWork fileWork = new FileWork(getApplicationContext());
        fileWork.writeToFile(gson.toJson(AppData.ME), FileNames.USER);
    }

    /**
     * Load cached profile
     */
    private void loadProfile() {
        final JsonParser jsonParser = new JsonParser();
        final Gson gson = new Gson();
        final FileWork fileWork = new FileWork(getApplicationContext());
        final String userString = fileWork.readFromFile(FileNames.USER);
        if (userString.equals("")) {
            loadMe();
        } else {
            AppData.ME = gson.fromJson(jsonParser.parse(userString), solonsky.signal.twitter.models.User.class);
            Log.e(TAG, "My id - " + AppData.ME.getId());
            Log.e(TAG, "My name - " + AppData.ME.getName());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadMe();
                }
            }, 1000);
        }
    }

    private void loadMe() {
        final AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void lookedupUsers(ResponseList<User> users) {
                super.lookedupUsers(users);
                AppData.ME = gson.fromJson(gson.toJsonTree(users.get(0)), solonsky.signal.twitter.models.User.class);
                AppData.ME.setBiggerProfileImageURL(users.get(0).getBiggerProfileImageURL());
                AppData.ME.setOriginalProfileImageURL(users.get(0).getOriginalProfileImageURL());

                saveProfile();
                loadConfigurations();
                viewModel.setAvatar(AppData.ME.getOriginalProfileImageURL());
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long myId = asyncTwitter.getId();
                    Log.e(TAG, "loaded id - " + myId);
                    asyncTwitter.lookupUsers(myId);
                } catch (TwitterException e) {
                    Log.e(TAG, "error - " + e.getLocalizedMessage());
                }
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (viewModel.isSearch()) {
            endSearching(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LoggedData.getInstance().saveCache();
    }

    public void setStatusBarColor(int color) {
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(getResources().getColor(color));
    }

    /**
     * Scroll feed to top
     */
    private void makeUpdate() {
        if (lastFragment != null) {
            if (viewModel.getStatusType().equals(Flags.STATUS_TYPE.COUNTER)) {
                if (lastFragment instanceof FeedFragment)
                    viewModel.setStatusType(Flags.STATUS_TYPE.ARROW);
                if (lastFragment instanceof FragmentCounterListener) {
                    ((FragmentCounterListener) lastFragment).onUpdate();
                    ((FragmentCounterListener) lastFragment).onScrollToTop();
                }
            } else if (viewModel.getStatusType().equals(Flags.STATUS_TYPE.ARROW)) {
                viewModel.setStatusType(Flags.STATUS_TYPE.COUNTER);
                if (lastFragment instanceof FragmentCounterListener) {
                    ((FragmentCounterListener) lastFragment).onBackToPosition();
                }
            } else {
                if (lastFragment instanceof FragmentCounterListener) {
                    ((FragmentCounterListener) lastFragment).onScrollToTopWithAnimation(250, 500, 250);
                }
            }
        }
    }

    /**
     * Performs end of searching
     *
     * @param isAnimated - @true for animated
     */
    public void endSearching(boolean isAnimated) {
        Utilities.hideKeyboard(mActivity);

        if (searchFragment != null) {
            if (Flags.isSearchSaved) {
                searchFragment.updateData();
            } else {
                searchFragment.initPopup();
            }
        }
        mActivity.viewModel.setToolbarState(previousTBState);
        mActivity.viewModel.setSearch(false);
        mActivity.binding.txtLoggedSearch.setText("");
        mActivity.binding.txtLoggedSearch.requestFocus();

        if (isAnimated) {
            binding.flLoggedMain.setVisibility(View.VISIBLE);
            binding.flLoggedSearch.animate().setDuration(300).translationX(Utilities.getScreenWidth(mActivity)).start();
            binding.flLoggedMain.animate().setDuration(300).alpha(1).translationX(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    binding.flLoggedSearch.setVisibility(View.GONE);
                }
            }).start();
        } else {
            binding.flLoggedMain.animate().setDuration(0).alpha(1).translationX(0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    binding.flLoggedMain.setVisibility(View.VISIBLE);
                    binding.flLoggedSearch.setVisibility(View.GONE);
                }
            }).start();
        }
    }

    /**
     * Starts activity to compose new tweet
     */
    private void makeCompose() {
        startActivity(new Intent(getApplicationContext(), ComposeActivity.class));
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_no_animation);
    }

    /**
     * Hide multiple popup menu
     */
    public void hidePopup() {
        if (bottomBar != null && bottomBar.getPopup() != null) bottomBar.getPopup().dismiss();
    }

    /**
     * Init initial fragments to show in
     */
    private void initFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (AppData.userConfiguration != null) {
            switch (AppData.userConfiguration.getTabPosition()) {
                case 0:
                    if (feedFragment == null)
                        feedFragment = new FeedFragment();
                    if (!feedFragment.isAdded())
                        fragmentTransaction.add(R.id.fl_logged_main,
                                feedFragment, feedFragment.getTag());
                    lastFragment = feedFragment;
                    break;

                case 1:
                    if (mentionsFragment == null)
                        mentionsFragment = MVPMentionsFragment.Companion.newInstance();
                    if (!mentionsFragment.isAdded())
                        fragmentTransaction.add(R.id.fl_logged_main,
                                mentionsFragment, mentionsFragment.getTag());
                    lastFragment = mentionsFragment;
                    break;

                case 2:
                    if (notificationsFragment == null)
                        notificationsFragment = new NotificationsFragment();
                    if (!notificationsFragment.isAdded())
                        fragmentTransaction.add(R.id.fl_logged_main,
                                notificationsFragment, notificationsFragment.getTag());
                    lastFragment = notificationsFragment;
                    break;

                case 3:
                    if (directFragment == null)
                        directFragment = new DirectFragment();
                    if (!directFragment.isAdded())
                        fragmentTransaction.add(R.id.fl_logged_main,
                                directFragment, directFragment.getTag());
                    lastFragment = directFragment;
                    break;

                case 4:
                    if (searchFragment == null)
                        searchFragment = new SearchFragment();
                    if (!searchFragment.isAdded())
                        fragmentTransaction.add(R.id.fl_logged_main,
                                searchFragment, searchFragment.getTag());
                    lastFragment = searchFragment;
                    break;

                case 5:
                    if (muteFragment == null)
                        muteFragment = new MuteFragment();
                    if (!muteFragment.isAdded())
                        fragmentTransaction.add(R.id.fl_logged_main,
                                muteFragment, muteFragment.getTag());
                    lastFragment = muteFragment;
                    break;

                case 6:
                    if (profileFragment == null)
                        profileFragment = new ProfileFragment();
                    if (!profileFragment.isAdded())
                        fragmentTransaction.add(R.id.fl_logged_main,
                                profileFragment, profileFragment.getTag());
                    lastFragment = profileFragment;
                    break;

                case 7:
                    if (likesFragment == null)
                        likesFragment = new LikesFragment();
                    if (!likesFragment.isAdded())
                        fragmentTransaction.add(R.id.fl_logged_main,
                                likesFragment, likesFragment.getTag());
                    lastFragment = likesFragment;
                    break;

            }
        } else {
            if (feedFragment == null)
                feedFragment = new FeedFragment();
            if (!feedFragment.isAdded())
                fragmentTransaction.add(R.id.fl_logged_main,
                        feedFragment, feedFragment.getTag());
            lastFragment = feedFragment;
        }

        fragmentTransaction.commit();
        viewModel.setToolbarState(AppData.TOOLBAR_LOGGED_MAIN);
    }

    /**
     * Init bottom bar with properly count of items and options
     */
    private void initBottomBar() {
        bottomBar = (AGBottomBar) findViewById(R.id.ag_logged_bottom);
        bottomBar.setActivity(this);

        if (AppData.userConfiguration != null) {
            bottomBar.setCURRENT_TAB_ID(AppData.userConfiguration.getTabPosition());
        }

        final AGBottomBarSingleItem bottomBarItem = new AGBottomBarSingleItem(0, false, true, R.color.light_bar_inactive_color,
                R.drawable.ic_tabbar_icons_home);
        AGBottomBarSingleItem bottomBarItem1 = new AGBottomBarSingleItem(1, false, true, R.color.light_bar_inactive_color,
                R.drawable.ic_tabbar_icons_mentions);
        AGBottomBarSingleItem bottomBarItem2 = new AGBottomBarSingleItem(2, false, true, R.color.light_bar_inactive_color,
                R.drawable.ic_tabbar_icons_activity);

        AGBottomBarMultipleItem bottomBarItem3 = new AGBottomBarMultipleItem(3, false,
                AppData.userConfiguration.getBottomIds().contains(3),
                AppData.userConfiguration.getBottomIds().contains(3) ?
                        R.drawable.ic_tabbar_icons_messages_active : R.drawable.ic_tabbar_icons_messages,
                R.drawable.ic_tabbar_icons_more);
        AGBottomBarMultipleItem bottomBarItem4 = new AGBottomBarMultipleItem(4, false,
                AppData.userConfiguration.getBottomIds().contains(4),
                R.drawable.ic_tabbar_icons_search, R.drawable.ic_tabbar_icons_more);
        AGBottomBarMultipleItem bottomBarItem5 = new AGBottomBarMultipleItem(5, false,
                AppData.userConfiguration.getBottomIds().contains(5),
                R.drawable.ic_tabbar_icons_mute, R.drawable.ic_tabbar_icons_more);
        AGBottomBarMultipleItem bottomBarItem6 = new AGBottomBarMultipleItem(6, false,
                AppData.userConfiguration.getBottomIds().contains(6),
                R.drawable.ic_tabbar_icons_profile, R.drawable.ic_tabbar_icons_more);
        AGBottomBarMultipleItem bottomBarItem7 = new AGBottomBarMultipleItem(7, false,
                AppData.userConfiguration.getBottomIds().contains(7),
                R.drawable.ic_tabbar_icons_likes, R.drawable.ic_tabbar_icons_more);

        List<AGBottomBarItem> items = new ArrayList<>();
        items.add(bottomBarItem);
        items.add(bottomBarItem1);
        items.add(bottomBarItem2);
        items.add(bottomBarItem3);
        items.add(bottomBarItem4);
        items.add(bottomBarItem5);
        items.add(bottomBarItem6);
        items.add(bottomBarItem7);

        int firstPosition = 0;
        int secondPosition = 1;

        for (AGBottomBarItem agBottomBarItem : items) {
            if (agBottomBarItem.getId() == AppData.userConfiguration.getBottomIds().get(3))
                firstPosition = items.indexOf(agBottomBarItem);
            if (agBottomBarItem.getId() == AppData.userConfiguration.getBottomIds().get(4))
                secondPosition = items.indexOf(agBottomBarItem);
        }

        Collections.swap(items, firstPosition, 3);
        Collections.swap(items, secondPosition, 4);

        for (AGBottomBarItem agBottomBarItem : items) {
            if (agBottomBarItem instanceof AGBottomBarMultipleItem)
                bottomBar.addItem((AGBottomBarMultipleItem) agBottomBarItem);
            if (agBottomBarItem instanceof AGBottomBarSingleItem)
                bottomBar.addItem((AGBottomBarSingleItem) agBottomBarItem);
        }

        bottomBar.setAgPopup(new AGBottomBar.AGPopup() {
            @Override
            public void changeItem(int position, int id) {
                AppData.userConfiguration.getBottomIds().set(position, id);
                ConfigurationUserModel.saveCache();
                bottomBar.setCaptionVisibility(View.GONE, 3);
                bottomBar.setCaptionVisibility(View.GONE, 4);

                if (id == 3) {
                    LoggedData.getInstance().getUpdateHandler().onUpdate();
                }
            }
        });

        bottomBar.setAgHandler(new AGBottomBar.AGHandler() {
            @Override
            public void itemClick(View view, int id) {
                if (id != 6) AppData.userConfiguration.setTabPosition(id);
                switch (id) {
                    case 0:
                        LoggedData.getInstance().setNewFeed(false);
                        bottomBar.updateIcon(3, R.drawable.ic_tabbar_icons_messages);
                        binding.agLoggedBottom.setCaptionVisibility(View.GONE, 0);
                        if (feedFragment == null) feedFragment = new FeedFragment();
                        showFragment(feedFragment);
                        break;

                    case 1:
                        LoggedData.getInstance().setNewMention(false);
                        bottomBar.updateIcon(3, R.drawable.ic_tabbar_icons_messages);
                        binding.agLoggedBottom.setCaptionVisibility(View.GONE, 1);
                        if (mentionsFragment == null) mentionsFragment = MVPMentionsFragment.Companion.newInstance();
                        showFragment(mentionsFragment);
                        break;

                    case 2:
                        LoggedData.getInstance().setNewActivity(false);
                        bottomBar.updateIcon(3, R.drawable.ic_tabbar_icons_messages);
                        binding.agLoggedBottom.setCaptionVisibility(View.GONE, 2);
                        if (notificationsFragment == null)
                            notificationsFragment = new NotificationsFragment();
                        showFragment(notificationsFragment);
                        break;

                    case 3:
                        bottomBar.updateIcon(3, R.drawable.ic_tabbar_icons_messages_active);
                        if (directFragment == null) directFragment = new DirectFragment();
                        showFragment(directFragment);
                        break;

                    case 4:
                        bottomBar.updateIcon(3, R.drawable.ic_tabbar_icons_messages);
                        if (searchFragment == null) searchFragment = new SearchFragment();
                        showFragment(searchFragment);
                        break;

                    case 5:
                        bottomBar.updateIcon(3, R.drawable.ic_tabbar_icons_messages);
                        if (muteFragment == null) muteFragment = new MuteFragment();
                        showFragment(muteFragment);
                        break;

                    case 6:
                        bottomBar.updateIcon(3, R.drawable.ic_tabbar_icons_messages);
                        if (profileFragment == null) profileFragment = new ProfileFragment();
                        showFragment(profileFragment);
                        break;

                    case 7:
                        bottomBar.updateIcon(3, R.drawable.ic_tabbar_icons_messages);
                        if (likesFragment == null) likesFragment = new LikesFragment();
                        showFragment(likesFragment);
                        break;
                }

                ConfigurationUserModel.saveCache();
                hidePopup();
            }
        });

        bottomBar.build();
    }

    public void showFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (lastFragment != null) fragmentTransaction.hide(lastFragment);
        if (!fragment.isAdded()) {
            fragmentTransaction.add(R.id.fl_logged_main, fragment);
        } else {
            fragmentTransaction.show(fragment);
        }

        fragmentTransaction.commit();
        lastFragment = fragment;
    }

    private void parseNotification(int notificationId, final Object input, String sender, long statusId) {
        if (notificationId > 0) {
            NotificationManager mNotificationManager = (NotificationManager)
                    getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(getApplicationContext().getString(R.string.app_name), notificationId);
        }

        if (input.equals(Flags.NotificationTypes.FAV) || input.equals(Flags.NotificationTypes.UNDEFINED)
                || input.equals(Flags.NotificationTypes.FOLLOW) || input.equals(Flags.NotificationTypes.MENTION)) {
            Flags.userSource = Flags.UserSource.screenName;
            AppData.CURRENT_SCREEN_NAME = sender;
            mActivity.startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (input.equals(Flags.NotificationTypes.DIRECT)) {
            DirectApi.getInstance().clear();
            DirectApi.getInstance().setScreenName(sender);
            mActivity.startActivity(new Intent(getApplicationContext(), ChatActivity.class));
            mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (input.equals(Flags.NotificationTypes.RT) || input.equals(Flags.NotificationTypes.QUOTED)) {
            final Handler handler = new Handler();
            AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
            asyncTwitter.addListener(new TwitterAdapter() {
                @Override
                public void gotShowStatus(Status status) {
                    super.gotShowStatus(status);
                    final StatusModel statusModel = new Gson().fromJson(new Gson().toJsonTree(status), StatusModel.class);
                    statusModel.tuneModel(status);
                    statusModel.linkClarify();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (input.equals(Flags.NotificationTypes.RT)) {
                                AppData.CURRENT_STATUS_MODEL = statusModel.getRetweetedStatus();
                            } else {
                                AppData.CURRENT_STATUS_MODEL = statusModel;
                            }
                            mActivity.startActivity(new Intent(getApplicationContext(), DetailActivity.class));
                            mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }
                    });
                }
            });

            asyncTwitter.showStatus(statusId);
        } else if (input.equals(Flags.NotificationTypes.REPLY)) {
            Flags.CURRENT_COMPOSE = Flags.COMPOSE_MENTION;
            AppData.COMPOSE_MENTION = "@" + sender;
            mActivity.startActivity(new Intent(getApplicationContext(), ComposeActivity.class));
            mActivity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_no_animation);
        }
    }

    public void setFeedCount(int feedCount) {
        this.feedCount = feedCount;
    }

    public int getREQUEST_LOCATION_CODE() {
        return REQUEST_LOCATION_CODE;
    }
}
