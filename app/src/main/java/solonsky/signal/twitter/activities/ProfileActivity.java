package solonsky.signal.twitter.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import it.sephiroth.android.library.easing.Linear;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.SimplePagerAdapter;
import solonsky.signal.twitter.api.ActionsApiFactory;
import solonsky.signal.twitter.api.DirectApi;
import solonsky.signal.twitter.api.ProfileDataApi;
import solonsky.signal.twitter.data.MuteData;
import solonsky.signal.twitter.data.UsersData;
import solonsky.signal.twitter.databinding.ActivityProfileBinding;
import solonsky.signal.twitter.fragments.DummyFragment;
import solonsky.signal.twitter.fragments.HeaderInfoFragment;
import solonsky.signal.twitter.fragments.HeaderStatsFragment;
import solonsky.signal.twitter.fragments.ProfileLikesFragment;
import solonsky.signal.twitter.fragments.ProfileMediaFragment;
import solonsky.signal.twitter.fragments.ProfileTweetsFragment;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.interfaces.ProfileListener;
import solonsky.signal.twitter.libs.DownloadFiles;
import solonsky.signal.twitter.models.RemoveModel;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.overlays.ImageOverlay;
import solonsky.signal.twitter.viewmodels.ProfileViewModel;
import twitter4j.AsyncTwitter;
import twitter4j.ResponseList;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.User;

import static android.support.v4.view.ViewPager.*;

/**
 * Created by neura on 08.07.17.
 */

public class ProfileActivity extends AppCompatActivity implements SmartTabLayout.TabProvider, ProfileListener {

    /**
     * Avatar size in DP
     *
     * @link Utilities.convertDptoPixel
     */
    private final int AVATAR_SIZE = 76;

    /**
     * Verified icon size in DP
     *
     * @link Utilities.convertDptoPixel
     */
    private final int VERIFIED_SIZE = 20;
    private final int TAIL_Y_OFFSET = 208;
    private final int TABS_Y_OFFSET = 176;
    private final int FADER_Y_OFFSET = 138;
    private final int ESTIMATED_PAGER_SIZE = 80;

    private int CURRENT_POSITION = 0;

    public ActivityProfileBinding binding;
    private ProfileViewModel viewModel;
    private ProfileActivity mActivity;

    private ArrayList<StatusModel> mFeedList = new ArrayList<>();
    private ArrayList<StatusModel> mLikeList = new ArrayList<>();

    private ProfileTweetsFragment profileTweetsFragment = new ProfileTweetsFragment();
    private ProfileLikesFragment profileLikesFragment = new ProfileLikesFragment();
    private ProfileMediaFragment profileMediaFragment = new ProfileMediaFragment();
    private final String TAG = ProfileActivity.class.getSimpleName();
    private int difference = 0;

    private HeaderStatsFragment headerStatsFragment;
    private HeaderInfoFragment headerInfoFragment;
    private solonsky.signal.twitter.models.User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.TranslucentDark);
        }

        mActivity = this;

        if (AppData.CURRENT_USER != null)
            this.currentUser = new solonsky.signal.twitter.models.User(AppData.CURRENT_USER);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        binding.scrollProfile.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                updateAvatar(scrollY);
                setupTab(scrollY);
                setupTitle(scrollY);

                View view = binding.scrollProfile.getChildAt(binding.scrollProfile.getChildCount() - 1);
                int diff = (view.getBottom() - (binding.scrollProfile.getHeight() + binding.scrollProfile.getScrollY()));

                // if diff is zero, then the bottom has been reached
                if (diff < 300 && !viewModel.isLoading() && mFeedList.size() < 75) {
                    viewModel.setLoading(true);
                }
            }
        });

        if (Flags.userSource == Flags.UserSource.screenName ||
                Flags.userSource == Flags.UserSource.id) {
            final Handler handler = new Handler();
            AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
            asyncTwitter.addListener(new TwitterAdapter() {
                @Override
                public void lookedupUsers(ResponseList<User> users) {
                    super.lookedupUsers(users);
                    final User user = users.get(0);
                    AppData.CURRENT_USER = solonsky.signal.twitter.models.User.getFromUserInstance(user);
                    ProfileActivity.this.currentUser = AppData.CURRENT_USER;

                    final String screenName = Flags.homeUser ? "@" + AppData.CURRENT_USER.getScreenName() :
                            UsersData.getInstance().getFollowersList().contains(AppData.CURRENT_USER.getId())
                                    ? "follows you" : "does not follow you";

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            viewModel = new ProfileViewModel(user.getProfileBannerIPadRetinaURL(),
                                    user.getOriginalProfileImageURL(), user.getName(), screenName,
                                    "Followers " + Utilities.parseFollowers(user.getFollowersCount(), ""), user.isVerified());
                            binding.txtProfileScreenName.setTextColor(getResources().getColor(
                                    Flags.homeUser ? R.color.dark_hint_text_color : App.getInstance().isNightEnabled() ?
                                            R.color.dark_highlight_color : R.color.light_highlight_color));
                            binding.txtProfileScreenName.setTypeface(binding.txtProfileScreenName.getTypeface(), Typeface.ITALIC);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    viewModel.setTwitterName(AppData.CURRENT_USER.getScreenName());
                                    binding.txtProfileScreenName.setTextColor(getResources().getColor(R.color.dark_hint_text_color));
                                    binding.txtProfileScreenName.setTypeface(binding.txtProfileScreenName.getTypeface(), Typeface.NORMAL);
                                }
                            }, 2000);

                            setUpFadeAnimation(1900, binding.txtProfileScreenName);

                            Picasso.with(getApplicationContext())
                                    .load(user.getOriginalProfileImageURL())
                                    .into(binding.imgProfileAvatarImage);

                            Picasso.with(getApplicationContext())
                                    .load(user.getProfileBannerIPadRetinaURL())
                                    .resize(Utilities.getScreenWidth(mActivity),
                                            (int) Utilities.convertDpToPixel(186f, getApplicationContext()))
                                    .centerCrop()
                                    .into(binding.imgProfileTestHeader);

                            binding.setModel(viewModel);

//                            headerInfoFragment.setProfileListener(mActivity);
//                            headerStatsFragment.setProfileListener(mActivity);

                            updateInfo();
                            updateStats();

                            ProfileDataApi.getInstance().clear();
                            ProfileDataApi.getInstance().setScreenName(AppData.CURRENT_USER.getScreenName());
                            ProfileDataApi.getInstance().loadData();

                            binding.vpProfileFragment.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    selectFragment(profileTweetsFragment);
                                }
                            }, 1000);
                        }
                    });
                }

                @Override
                public void onException(TwitterException te, TwitterMethod method) {
                    super.onException(te, method);
                    Log.e(TAG, "Error loading user - " + te.getLocalizedMessage());
                }
            });

            if (Flags.userSource == Flags.UserSource.screenName) {
                asyncTwitter.lookupUsers(AppData.CURRENT_SCREEN_NAME.replace("@", ""));
            } else {
                asyncTwitter.lookupUsers(AppData.CURRENT_USER_ID);
            }
        } else {
            String screenName = Flags.homeUser ? "@" + AppData.CURRENT_USER.getScreenName() :
                    UsersData.getInstance().getFollowersList().contains(AppData.CURRENT_USER.getId())
                            ? "follows you" : "does not follow you";
            binding.txtProfileScreenName.setTextColor(getResources().getColor(
                    Flags.homeUser ? R.color.dark_hint_text_color : App.getInstance().isNightEnabled() ?
                            R.color.dark_highlight_color : R.color.light_highlight_color));
            if (!Flags.homeUser)
                binding.txtProfileScreenName.setTypeface(binding.txtProfileScreenName.getTypeface(), Typeface.ITALIC);

            viewModel = new ProfileViewModel(AppData.CURRENT_USER.getProfileBannerImageUrl(),
                    AppData.CURRENT_USER.getOriginalProfileImageURL(), AppData.CURRENT_USER.getName(),
                    screenName, "Followers " + Utilities.parseFollowers(AppData.CURRENT_USER.getFollowersCount(), ""),
                    AppData.CURRENT_USER.isVerified());
            viewModel.setHomeUser(Flags.homeUser);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    viewModel.setTwitterName("@" + AppData.CURRENT_USER.getScreenName());
                    binding.txtProfileScreenName.setTextColor(getResources().getColor(R.color.dark_hint_text_color));
                    binding.txtProfileScreenName.setTypeface(binding.txtProfileScreenName.getTypeface(), Typeface.NORMAL);
                }
            }, 2000);

            setUpFadeAnimation(1900, binding.txtProfileScreenName);

            Picasso.with(getApplicationContext())
                    .load(AppData.CURRENT_USER.getOriginalProfileImageURL())
                    .into(binding.imgProfileAvatarImage);

            Picasso.with(getApplicationContext())
                    .load(AppData.CURRENT_USER.getProfileBannerImageUrl())
                    .resize(Utilities.getScreenWidth(mActivity),
                            (int) Utilities.convertDpToPixel(186f, getApplicationContext()))
                    .centerCrop()
                    .into(binding.imgProfileTestHeader);

            ProfileDataApi.getInstance().clear();
            ProfileDataApi.getInstance().setScreenName(AppData.CURRENT_USER.getScreenName());
            ProfileDataApi.getInstance().loadData();

            binding.vpProfileFragment.postDelayed(new Runnable() {
                @Override
                public void run() {
                    selectFragment(profileTweetsFragment);
                }
            }, 1000);
        }

        binding.setModel(viewModel);
        binding.setClick(new ProfileViewModel.ProfileClickHandler() {
            @Override
            public void onBackClick(View v) {
                onBackPressed();
            }

            @Override
            public void onSettingsClick(View v) {
                mActivity.startActivity(new Intent(mActivity, ProfileSettingsActivity.class));
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onMoreClick(View v) {
                switch (CURRENT_POSITION) {
                    case 0:
                        AppData.CURRENT_USER = currentUser;
                        mActivity.startActivity(new Intent(getApplicationContext(), StatsTweetsActivity.class));
                        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        break;

                    case 1:
                        AppData.CURRENT_USER = currentUser;
                        mActivity.startActivity(new Intent(getApplicationContext(), StatsLikesActivity.class));
                        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        break;

                    case 2:
                        AppData.CURRENT_USER = currentUser;
                        mActivity.startActivity(new Intent(getApplicationContext(), StatsImagesActivity.class));
                        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        break;
                }
            }

            @Override
            public void onLinkClick(View v) {

            }

            @Override
            public void onMenuClick(View v) {
                setupPopupMenu();
            }

            @Override
            public void onAvatarClick(View v) {
                ArrayList<String> urls = new ArrayList<>();
                urls.add(viewModel.getAvatar());

                final solonsky.signal.twitter.overlays.ImageOverlay imageOverlay = new ImageOverlay(urls, mActivity, 0);
                imageOverlay.setImageOverlayClickHandler(new ImageOverlay.ImageOverlayClickHandler() {
                    @Override
                    public void onBackClick(View v) {
                        imageOverlay.getImageViewer().onDismiss();
                    }

                    @Override
                    public void onSaveClick(View v, String url) {
                        DownloadFiles downloadFiles = new DownloadFiles(ProfileActivity.this);
                        downloadFiles.saveFile(url, getString(R.string.download_url));
                    }
                });
            }

            @Override
            public void onBackDropClick(View v) {
                ArrayList<String> urls = new ArrayList<>();
                urls.add(viewModel.getBackdrop());
                final solonsky.signal.twitter.overlays.ImageOverlay imageOverlay = new ImageOverlay(urls, mActivity, 0);
                imageOverlay.setImageOverlayClickHandler(new ImageOverlay.ImageOverlayClickHandler() {
                    @Override
                    public void onBackClick(View v) {
                        imageOverlay.getImageViewer().onDismiss();
                    }

                    @Override
                    public void onSaveClick(View v, String url) {
                        DownloadFiles downloadFiles = new DownloadFiles(ProfileActivity.this);
                        downloadFiles.saveFile(url, getString(R.string.download_url));
                    }
                });
            }
        });

        setupHeaderPager();
        setupContentPager();
    }

    @Override
    public void onBackPressed() {
        ProfileDataApi.getInstance().clear();
        finish();
        if (Flags.userDirection == Flags.Directions.FADE) {
            overridePendingTransition(R.anim.slide_out_no_animation, R.anim.fade_out);
        } else {
            overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
        }
    }

    private void setupPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(mActivity, binding.btnProfileMore, Gravity.BOTTOM, 0, R.style.popup_menu);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.menu_profile, popupMenu.getMenu());

        if (!MuteData.getInstance().isCacheLoaded())
            MuteData.getInstance().loadCache();

        boolean isFollow = UsersData.getInstance().getFollowingList().contains(AppData.CURRENT_USER.getId());
        boolean isRetweetDisabled = MuteData.getInstance().getmRetweetsIds().contains(AppData.CURRENT_USER.getId());
        boolean isMuted = false;
        boolean isBlocked = UsersData.getInstance().getBlockList().contains(AppData.CURRENT_USER.getId());

        for (RemoveModel removeModel : MuteData.getInstance().getmUsersList()) {
            if (TextUtils.equals(removeModel.getTitle().toLowerCase(), "@" + AppData.CURRENT_USER.getScreenName().toLowerCase())) {
                isMuted = true;
                break;
            }
        }

        popupMenu.getMenu().getItem(2).setVisible(!isFollow);
        popupMenu.getMenu().getItem(3).setVisible(isFollow);

        popupMenu.getMenu().getItem(4).setVisible(!isRetweetDisabled);
        popupMenu.getMenu().getItem(5).setVisible(isRetweetDisabled);

        popupMenu.getMenu().getItem(6).setVisible(!isMuted);
        popupMenu.getMenu().getItem(7).setVisible(isMuted);

        popupMenu.getMenu().getItem(8).setVisible(!isBlocked);
        popupMenu.getMenu().getItem(9).setVisible(isBlocked);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profile_reply:
                        Flags.CURRENT_COMPOSE = Flags.COMPOSE_MENTION;
                        AppData.COMPOSE_MENTION = "@" + AppData.CURRENT_USER.getScreenName();
                        mActivity.startActivity(new Intent(getApplicationContext(), ComposeActivity.class));
                        break;

                    case R.id.profile_direct:
                        String screenName = "@" + AppData.CURRENT_USER.getScreenName();
                        final long userId = AppData.CURRENT_USER.getId();

                        DirectApi.getInstance().clear();
                        DirectApi.getInstance().setUserId(userId);
                        DirectApi.getInstance().setScreenName(screenName);
                        mActivity.startActivity(new Intent(getApplicationContext(), ChatActivity.class));
                        break;

                    case R.id.profile_disable_retweets:
                        ActionsApiFactory.disableRetweet(AppData.CURRENT_USER.getId(), getApplicationContext());
                        break;

                    case R.id.profile_enable_retweets:
                        ActionsApiFactory.enableRetweet(AppData.CURRENT_USER.getId(), getApplicationContext());
                        break;

                    case R.id.profile_follow:
                        ActionsApiFactory.follow(AppData.CURRENT_USER.getId(), getApplicationContext());
                        break;

                    case R.id.profile_unfollow:
                        ActionsApiFactory.unfollow(AppData.CURRENT_USER.getId(), getApplicationContext());
                        break;

                    case R.id.profile_mute:
                        ActionsApiFactory.mute(AppData.CURRENT_USER.getId(), getApplicationContext());
                        break;

                    case R.id.profile_unmute:
                        ActionsApiFactory.unmute(AppData.CURRENT_USER.getId(), getApplicationContext());
                        break;

                    case R.id.profile_block:
                        ActionsApiFactory.block(AppData.CURRENT_USER.getId(), getApplicationContext());
                        break;

                    case R.id.profile_unblock:
                        ActionsApiFactory.unblock(AppData.CURRENT_USER.getId(), getApplicationContext());
                        break;

                    case R.id.profile_report:
                        ActionsApiFactory.report(AppData.CURRENT_USER.getId(), getApplicationContext());
                        break;
                }
                return false;
            }
        });

        popupMenu.show();
    }

    /**
     * Setup title floating position
     *
     * @param scrollY - scroll y position
     */
    private void setupTitle(int scrollY) {
        int diff = (int) (Utilities.convertDpToPixel(51.25f, getApplicationContext()) - scrollY);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) binding.llToolbarTitle.getLayoutParams();

        if (diff > 0) {
            params.topMargin = (int) Utilities.convertDpToPixel(80, getApplicationContext());
        } else {
            params.topMargin = -diff >= Utilities.convertDpToPixel(44.75f, getApplicationContext()) ?
                    (int) Utilities.convertDpToPixel(35.25f, getApplicationContext()) : (int) Utilities.convertDpToPixel(80, getApplicationContext()) + diff;
        }

        binding.llToolbarTitle.setLayoutParams(params);
    }

    /**
     * Setup tab fixed position when scroll
     *
     * @param scrollY - scroll y position
     */
    private void setupTab(int scrollY) {
        int diff = (int) (Utilities.convertDpToPixel(TAIL_Y_OFFSET + 16, getApplicationContext())
                + difference - scrollY);
        if (diff < 0) {
            binding.stbProfileFragment.animate().translationY(-diff).setDuration(0).start();
        } else {
            binding.stbProfileFragment.animate().translationY(0).setDuration(0).start();
        }
    }

    /**
     * Setup content view pager
     */
    private void setupContentPager() {
        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(mActivity)
                .add(getString(R.string.profile_tweets).toUpperCase(), DummyFragment.class)
                .add(getString(R.string.profile_likes).toUpperCase(), DummyFragment.class)
                .add(getString(R.string.profile_media).toUpperCase(), DummyFragment.class)
                .create());

        binding.vpProfileFragment.setAdapter(adapter);
        binding.stbProfileFragment.setCustomTabView(this);
        binding.stbProfileFragment.setViewPager(binding.vpProfileFragment);
        binding.stbProfileFragment.setOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                switch (position) {
                    case 0:
                        selectFragment(profileTweetsFragment);
                        ProfileDataApi.getInstance().setLikesHandler(null);
                        ProfileDataApi.getInstance().setMediaHandler(null);
                        viewModel.setSelectorName("Tweets");
                        break;

                    case 1:
                        selectFragment(profileLikesFragment);
                        ProfileDataApi.getInstance().setTweetHandler(null);
                        ProfileDataApi.getInstance().setMediaHandler(null);
                        viewModel.setSelectorName("Likes");
                        break;

                    case 2:
                        selectFragment(profileMediaFragment);
                        ProfileDataApi.getInstance().setLikesHandler(null);
                        ProfileDataApi.getInstance().setTweetHandler(null);
                        viewModel.setSelectorName("Media");
                        break;
                }

                View oldTab = binding.stbProfileFragment.getTabAt(CURRENT_POSITION);
                View currentTab = binding.stbProfileFragment.getTabAt(position);

                RelativeLayout oldView = (RelativeLayout) oldTab.findViewById(R.id.tab_layout);
                RelativeLayout currentView = (RelativeLayout) currentTab.findViewById(R.id.tab_layout);

                ((TextView) currentView.findViewById(R.id.tab_txt)).setTextColor(getResources()
                        .getColor(App.getInstance().isNightEnabled() ?
                                R.color.dark_primary_text_color : R.color.light_primary_text_color));
                ((TextView) currentView.findViewById(R.id.tab_txt)).setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));

                ((TextView) oldView.findViewById(R.id.tab_txt)).setTextColor(getResources()
                        .getColor(App.getInstance().isNightEnabled() ?
                                R.color.dark_hint_text_color : R.color.light_hint_text_color));
                ((TextView) oldView.findViewById(R.id.tab_txt)).setTypeface(Typeface.DEFAULT);

                oldView.setBackground(getResources().getDrawable(R.drawable.tab_shape_transparent));
                currentView.setBackground(getResources().getDrawable(App.getInstance().isNightEnabled() ?
                        R.drawable.tab_shape_dark : R.drawable.tab_shape_light));

                CURRENT_POSITION = position;
            }
        });
    }

    /**
     * Setup view pager placed in header
     */
    private void setupHeaderPager() {
        ArrayList<Fragment> fragments = new ArrayList<>();

        headerInfoFragment = new HeaderInfoFragment();
        headerStatsFragment = new HeaderStatsFragment();

//        headerInfoFragment.setProfileListener(this);
//        headerStatsFragment.setProfileListener(this);

        fragments.add(headerInfoFragment);
        fragments.add(headerStatsFragment);

        SimplePagerAdapter adapter = new SimplePagerAdapter(fragments, getSupportFragmentManager());

        binding.vpProfileHeader.setAdapter(adapter);
        binding.stbProfileHeader.setViewPager(binding.vpProfileHeader);
        binding.stbProfileHeader.setOnPageChangeListener(new SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        headerInfoFragment.changeProfileHeight(true);
                        break;

                    case 1:
                        headerStatsFragment.changeProfileHeight(true);
                        break;
                }
            }
        });
    }

    private void selectFragment(Fragment fragment) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fl_profile, fragment).commitNow();
        } catch (NullPointerException e) {
            // Do nothing
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        Resources res = container.getContext().getResources();
        View tab = inflater.inflate(R.layout.tab_item_only_text, container, false);
        ImageView imageView = (ImageView) tab.findViewById(R.id.tab_iv);
        TextView textView = (TextView) tab.findViewById(R.id.tab_txt);
        RelativeLayout layoutView = (RelativeLayout) tab.findViewById(R.id.tab_layout);

        textView.setTypeface(CURRENT_POSITION == position ?
                Typeface.create("sans-serif-medium", Typeface.NORMAL) :
                Typeface.create("sans-serif", Typeface.NORMAL));
        textView.setTextColor(res.getColor(App.getInstance().isNightEnabled() ?
                CURRENT_POSITION == position ? R.color.dark_primary_text_color : R.color.dark_hint_text_color
                : CURRENT_POSITION == position ? R.color.light_primary_text_color : R.color.light_hint_text_color));

        imageView.setVisibility(View.GONE);
        layoutView.setBackground(CURRENT_POSITION == position ?
                res.getDrawable(App.getInstance().isNightEnabled() ?
                        R.drawable.tab_shape_dark : R.drawable.tab_shape_light)
                : res.getDrawable(R.drawable.tab_shape_transparent));

        switch (position) {
            case 0:
                textView.setText(getString(R.string.profile_tweets).toUpperCase());
                break;

            case 1:
                textView.setText(getString(R.string.profile_likes).toUpperCase());
                break;

            case 2:
                textView.setText(getString(R.string.profile_media).toUpperCase());
                break;
        }

        return tab;
    }

    private void setUpFadeAnimation(int delayed, final TextView textView) {
        // Start from 0.1f if you desire 90% fade animation
        final Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(150);

        // End to 0.1f if you desire 90% fade animation
        final Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(150);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.startAnimation(fadeOut);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        textView.startAnimation(fadeIn);
                    }
                }, 150);
            }
        }, delayed);
    }

    @Override
    public void updateHeader(final int diff, boolean isAnimated) {
        difference = diff == 0 ? difference : diff;

        final RelativeLayout.LayoutParams paramsFader = (RelativeLayout.LayoutParams) binding.rlHeaderFader.getLayoutParams();
        final RelativeLayout.LayoutParams paramsStbContent = (RelativeLayout.LayoutParams) binding.stbProfileFragment.getLayoutParams();
        final RelativeLayout.LayoutParams paramsFlContent = (RelativeLayout.LayoutParams) binding.flProfile.getLayoutParams();
        final LinearLayout.LayoutParams paramsVpHeader = (LinearLayout.LayoutParams) binding.vpProfileHeader.getLayoutParams();

        if (isAnimated) {
            int duration = 300;

            Animation animVpHeader = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (diff == 0) {
//                        paramsVpHeader.height = ((int) (Utilities.convertDpToPixel(ESTIMATED_PAGER_SIZE, getApplicationContext()) + difference - (difference * interpolatedTime)));
                    } else if (diff > 0) {
//                        paramsVpHeader.height = ((int) (Utilities.convertDpToPixel(ESTIMATED_PAGER_SIZE, getApplicationContext()) + (diff * interpolatedTime)));
                    }
                    binding.vpProfileHeader.setLayoutParams(paramsVpHeader);
                }
            };

            Animation animHeader = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (diff == 0) {
                        paramsFader.topMargin = ((int) (Utilities.convertDpToPixel(FADER_Y_OFFSET, getApplicationContext()) + difference - (difference * interpolatedTime)));
                    } else {
                        paramsFader.topMargin = ((int) (Utilities.convertDpToPixel(FADER_Y_OFFSET, getApplicationContext()) + (diff * interpolatedTime)));
                    }
                    binding.rlHeaderFader.setLayoutParams(paramsFader);
                }
            };

            Animation animStb = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (diff == 0) {
                        paramsStbContent.topMargin = ((int) (Utilities.convertDpToPixel(TABS_Y_OFFSET, getApplicationContext()) + difference - (difference * interpolatedTime)));
                    } else {
                        paramsStbContent.topMargin = ((int) (Utilities.convertDpToPixel(TABS_Y_OFFSET, getApplicationContext()) + (diff * interpolatedTime)));
                    }
                    binding.stbProfileFragment.setLayoutParams(paramsStbContent);
                }
            };

            Animation animFragment = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (diff == 0) {
                        paramsFlContent.topMargin = ((int) (Utilities.convertDpToPixel(TAIL_Y_OFFSET, getApplicationContext()) + difference - (difference * interpolatedTime)));
                    } else {
                        paramsFlContent.topMargin = ((int) (Utilities.convertDpToPixel(TAIL_Y_OFFSET, getApplicationContext()) + (diff * interpolatedTime)));
                    }
                    binding.flProfile.setLayoutParams(paramsFlContent);
                }
            };

            animHeader.setDuration(duration);
            animStb.setDuration(duration);
            animFragment.setDuration(duration);
            animVpHeader.setDuration(duration);

            animHeader.setInterpolator(new DecelerateInterpolator());
            animStb.setInterpolator(new DecelerateInterpolator());
            animFragment.setInterpolator(new DecelerateInterpolator());
            animVpHeader.setInterpolator(new DecelerateInterpolator());

            binding.rlHeaderFader.startAnimation(animHeader);
            binding.stbProfileFragment.startAnimation(animStb);
            binding.flProfile.startAnimation(animFragment);
            binding.vpProfileHeader.startAnimation(animVpHeader);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    difference = diff;
                }
            }, duration);
        } else {
            if (diff > 0) {
                paramsVpHeader.height = ((int) (Utilities.convertDpToPixel(ESTIMATED_PAGER_SIZE, getApplicationContext()) + diff));
                binding.vpProfileHeader.setLayoutParams(paramsVpHeader);
            }

            paramsFader.topMargin = ((int) (Utilities.convertDpToPixel(FADER_Y_OFFSET, getApplicationContext()) + diff));
            binding.rlHeaderFader.setLayoutParams(paramsFader);

            paramsStbContent.topMargin = ((int) (Utilities.convertDpToPixel(TABS_Y_OFFSET, getApplicationContext()) + diff));
            binding.stbProfileFragment.setLayoutParams(paramsStbContent);

            paramsFlContent.topMargin = ((int) (Utilities.convertDpToPixel(TAIL_Y_OFFSET, getApplicationContext()) + diff));
            binding.flProfile.setLayoutParams(paramsFlContent);

            difference = diff;
        }
    }

    @Override
    public void updateAvatar(int scrollY) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                binding.imgProfileAvatar.getLayoutParams();
        RelativeLayout.LayoutParams verifiedParams = (RelativeLayout.LayoutParams)
                binding.imgProfileVerified.getLayoutParams();

        verifiedParams.width = (int) (Utilities.convertDpToPixel(VERIFIED_SIZE, getApplicationContext()) - scrollY) < 0 ?
                0 : (int) (Utilities.convertDpToPixel(VERIFIED_SIZE, getApplicationContext()) - scrollY);
        verifiedParams.height = (int) (Utilities.convertDpToPixel(VERIFIED_SIZE, getApplicationContext()) - scrollY) < 0 ?
                0 : (int) (Utilities.convertDpToPixel(VERIFIED_SIZE, getApplicationContext()) - scrollY);

        params.width = (int) (Utilities.convertDpToPixel(AVATAR_SIZE, getApplicationContext()) - scrollY) < 0 ?
                0 : (int) (Utilities.convertDpToPixel(AVATAR_SIZE, getApplicationContext()) - scrollY);
        params.height = (int) (Utilities.convertDpToPixel(AVATAR_SIZE, getApplicationContext()) - scrollY) < 0 ?
                0 : (int) (Utilities.convertDpToPixel(AVATAR_SIZE, getApplicationContext()) - scrollY);
        params.topMargin = -params.height / 2;

        verifiedParams.bottomMargin = (int) ((Utilities.convertDpToPixel(VERIFIED_SIZE, getApplicationContext()) - verifiedParams.height) / 3);
        verifiedParams.rightMargin = (int) ((Utilities.convertDpToPixel(VERIFIED_SIZE, getApplicationContext()) - verifiedParams.height) / 3);

        float progress = (Utilities.convertDpToPixel(AVATAR_SIZE, getApplicationContext()) - scrollY) / Utilities.convertDpToPixel(AVATAR_SIZE, getApplicationContext());
        binding.imgProfileAvatar.setAlpha(progress);

        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        binding.imgProfileAvatar.setLayoutParams(params);
        binding.imgProfileVerified.setLayoutParams(verifiedParams);
    }

    @Override
    public void updateInfo() {
        if (currentUser != null) {
            String realDescription = "";

            if (currentUser.getDescription() != null) {
                realDescription = currentUser.getDescription();
                if (currentUser.getDescriptionUrlEntities() != null) {
                    for (JsonElement jsonElement : currentUser.getDescriptionUrlEntities()) {
                        realDescription = realDescription.replace(
                                jsonElement.getAsJsonObject().get("url").getAsString(),
                                jsonElement.getAsJsonObject().get("expandedURL").getAsString());
                    }
                }
            }

            headerInfoFragment.setViewModel(
                    realDescription,
                    currentUser.getUrlEntity() == null ? "" : currentUser.getUrlEntity().get("displayURL").getAsString(),
                    currentUser.getLocation());
        }
    }

    @Override
    public void updateStats() {
        if (currentUser != null)
            headerStatsFragment.setViewModel(
                    currentUser.getFollowersCount(), currentUser.getFriendsCount(),
                    currentUser.getStatusesCount(), currentUser.getListedCount(), 0, 0, 0, 0);
    }

    @Override
    public void openActivity(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void openLink(String string) {
        Utilities.openLink(string, mActivity);
    }
}
