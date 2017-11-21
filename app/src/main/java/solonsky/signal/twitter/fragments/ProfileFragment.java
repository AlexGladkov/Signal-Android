package solonsky.signal.twitter.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.activities.ProfileSettingsActivity;
import solonsky.signal.twitter.activities.StatsLikesActivity;
import solonsky.signal.twitter.activities.StatsTweetsActivity;
import solonsky.signal.twitter.adapters.SimplePagerAdapter;
import solonsky.signal.twitter.api.ProfileDataApi;
import solonsky.signal.twitter.data.ProfileRefreshData;
import solonsky.signal.twitter.databinding.ActivityProfileBinding;
import solonsky.signal.twitter.databinding.FragmentProfileBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.FileWork;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.interfaces.ProfileListener;
import solonsky.signal.twitter.interfaces.ProfileRefreshHandler;
import solonsky.signal.twitter.viewmodels.ProfileViewModel;

import static android.view.View.inflate;

/**
 * Created by neura on 24.06.17.
 */

public class ProfileFragment extends Fragment implements SmartTabLayout.TabProvider, ProfileListener {
    private final String TAG = ProfileFragment.class.getSimpleName();

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
    private LoggedActivity mActivity;
    public ActivityProfileBinding binding;
    private ProfileViewModel viewModel;
    private int difference = 0;

    private ProfileTweetsFragment profileTweetsFragment = new ProfileTweetsFragment();
    private ProfileLikesFragment profileLikesFragment = new ProfileLikesFragment();
    private ProfileMediaFragment profileMediaFragment = new ProfileMediaFragment();

    private final HeaderInfoFragment headerInfoFragment = new HeaderInfoFragment();
    private final HeaderStatsFragment headerStatsFragment = new HeaderStatsFragment();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_profile, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = (LoggedActivity) getActivity();
        mActivity.viewModel.setToolbarState(AppData.TOOLBAR_LOGGED_PROFILE);
        mActivity.binding.rlLoggedContainer.setFitsSystemWindows(false);
        mActivity.setStatusBarColor(android.R.color.transparent);

        binding.scrollProfile.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                updateAvatar(scrollY);
                setupTab(scrollY);
                setupTitle(scrollY);
            }
        });

        viewModel = new ProfileViewModel(AppData.ME.getProfileBannerImageUrl(),
                AppData.ME.getOriginalProfileImageURL(), AppData.ME.getName(), "@" + AppData.ME.getScreenName(),
                "Followers " + AppData.ME.getFollowersCount(), false);
        viewModel.setHomeUser(true);

        binding.setModel(viewModel);
        binding.setClick(new ProfileViewModel.ProfileClickHandler() {
            @Override
            public void onBackClick(View v) {
                mActivity.onBackPressed();
            }

            @Override
            public void onSettingsClick(View v) {
                mActivity.startActivity(new Intent(getContext(), ProfileSettingsActivity.class));
                mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }

            @Override
            public void onMoreClick(View v) {
                switch (CURRENT_POSITION) {
                    case 0:
                        AppData.CURRENT_USER = AppData.ME;
                        mActivity.startActivity(new Intent(getContext(), StatsTweetsActivity.class));
                        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        break;

                    case 1:
                        AppData.CURRENT_USER = AppData.ME;
                        mActivity.startActivity(new Intent(getContext(), StatsLikesActivity.class));
                        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        break;

                    case 2:

                        break;
                }
            }

            @Override
            public void onLinkClick(View v) {

            }

            @Override
            public void onMenuClick(View v) {
                // Do nothing
            }

            @Override
            public void onAvatarClick(View v) {
                ArrayList<String> urls = new ArrayList<>();
                urls.add(viewModel.getAvatar());

                View overlay = inflate(getContext(), R.layout.overlay_profile, null);

                final ImageViewer imageViewer = new ImageViewer.Builder<>(mActivity, urls)
                        .setStartPosition(0)
                        .setOverlayView(overlay)
                        .setStyleRes(R.style.statusMediaStyle).build();

                imageViewer.show();
                overlay.findViewById(R.id.img_profile_content_back).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageViewer.onDismiss();
                    }
                });
            }

            @Override
            public void onBackDropClick(View v) {
                ArrayList<String> urls = new ArrayList<>();
                urls.add(viewModel.getBackdrop());

                View overlay = inflate(getContext(), R.layout.overlay_profile, null);

                final ImageViewer imageViewer = new ImageViewer.Builder<>(mActivity, urls)
                        .setStartPosition(0)
                        .setOverlayView(overlay)
                        .setStyleRes(R.style.statusMediaStyle).build();

                imageViewer.show();
                overlay.findViewById(R.id.img_profile_content_back).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imageViewer.onDismiss();
                    }
                });
            }
        });

        Log.e(TAG, "old banner " + viewModel.getBackdrop());
        Picasso.with(getContext()).load(viewModel.getBackdrop())
                .resize(Utilities.getScreenWidth(mActivity), (int) Utilities.convertDpToPixel(186, getContext()))
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .centerCrop()
                .into(binding.imgProfileTestHeader);

        Picasso.with(getContext()).load(viewModel.getAvatar())
                .resize((int) Utilities.convertDpToPixel(72, getContext()),
                        (int) Utilities.convertDpToPixel(72, getContext()))
                .centerCrop()
                .into(binding.imgProfileAvatarImage);

        ProfileDataApi.getInstance().setScreenName(AppData.ME.getScreenName());
        ProfileDataApi.getInstance().loadData();
        ProfileRefreshData.getInstance().setUpdateHandler(new ProfileRefreshHandler() {
            @Override
            public void onAvatarUpdate() {
                viewModel.setAvatar(AppData.ME.getOriginalProfileImageURL());
                Picasso.with(getContext())
                        .load(AppData.ME.getOriginalProfileImageURL())
                        .into(binding.imgProfileAvatarImage);
            }

            @Override
            public void onBannerUpdate() {
                viewModel.setBackdrop(AppData.ME.getProfileBannerImageUrl());
                binding.imgProfileTestHeader.setImageResource(android.R.color.transparent);
                Picasso.with(getContext()).load(AppData.ME.getProfileBannerImageUrl())
                        .resize(Utilities.getScreenWidth(mActivity), (int) Utilities.convertDpToPixel(186, getContext()))
                        .centerCrop()
                        .into(binding.imgProfileTestHeader, new Callback() {
                            @Override
                            public void onSuccess() {
                                binding.imgProfileTestHeader.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        binding.imgProfileTestHeader.invalidate();
                                        binding.imgProfileTestHeader.requestLayout();
                                    }
                                }, 50);
                            }

                            @Override
                            public void onError() {
                                Log.e(TAG, "Error loading new image");
                            }
                        });
            }

            @Override
            public void onInfoUpdate() {
                viewModel.setUsername(AppData.ME.getName());
                viewModel.setTwitterName(AppData.ME.getScreenName());

                String realDescription = "";

                if (AppData.ME.getDescription() != null) {
                    realDescription = AppData.ME.getDescription();
                    if (AppData.ME.getDescriptionUrlEntities() != null) {
                        for (JsonElement jsonElement : AppData.ME.getDescriptionUrlEntities()) {
                            realDescription = realDescription.replace(
                                    jsonElement.getAsJsonObject().get("url").getAsString(),
                                    jsonElement.getAsJsonObject().get("expandedURL").getAsString());
                        }
                    }
                }

                headerInfoFragment.setViewModel(
                        realDescription, AppData.ME.getUrlEntity() == null ? "" :
                                AppData.ME.getUrlEntity().get("displayURL").getAsString(), AppData.ME.getLocation());
            }
        });

        setupHeaderPager();
        setupContentPager();

        binding.imgProfileAvatar.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateHeader(0, false);
            }
        }, 100);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            mActivity.endSearching(false);
            mActivity.viewModel.setToolbarState(AppData.TOOLBAR_LOGGED_PROFILE);
            mActivity.binding.rlLoggedContainer.setFitsSystemWindows(false);
            mActivity.setStatusBarColor(android.R.color.transparent);
        }
    }

    private void selectFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fl_profile, fragment).commit();
    }

    /**
     * Setup title floating position
     *
     * @param scrollY - scroll y position
     */
    private void setupTitle(int scrollY) {
        int diff = (int) (Utilities.convertDpToPixel(51.25f, getContext()) - scrollY);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) binding.llToolbarTitle.getLayoutParams();

        if (diff > 0) {
            params.topMargin = (int) Utilities.convertDpToPixel(80, getContext());
        } else {
            params.topMargin = -diff >= Utilities.convertDpToPixel(44.75f, getContext()) ?
                    (int) Utilities.convertDpToPixel(35.25f, getContext()) : (int) Utilities.convertDpToPixel(80, getContext()) + diff;
        }

        binding.llToolbarTitle.setLayoutParams(params);
    }

    /**
     * Setup tab fixed position when scroll
     *
     * @param scrollY - scroll y position
     */
    private void setupTab(int scrollY) {
        int diff = (int) (Utilities.convertDpToPixel(TAIL_Y_OFFSET + 16, getContext())
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
                getChildFragmentManager(), FragmentPagerItems.with(mActivity)
                .add(getString(R.string.profile_tweets).toUpperCase(), DummyFragment.class)
                .add(getString(R.string.profile_likes).toUpperCase(), DummyFragment.class)
                .add(getString(R.string.profile_media).toUpperCase(), DummyFragment.class)
                .create());

        binding.vpProfileFragment.setAdapter(adapter);
        binding.stbProfileFragment.setCustomTabView(this);
        binding.stbProfileFragment.setViewPager(binding.vpProfileFragment);
        binding.stbProfileFragment.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        selectFragment(profileTweetsFragment);
                        viewModel.setSelectorName(getString(R.string.profile_tweets));
                        break;

                    case 1:
                        selectFragment(profileLikesFragment);
                        viewModel.setSelectorName(getString(R.string.profile_likes));
                        break;

                    case 2:
                        selectFragment(profileMediaFragment);
                        viewModel.setSelectorName(getString(R.string.profile_media));
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

        binding.vpProfileFragment.postDelayed(new Runnable() {
            @Override
            public void run() {
                selectFragment(profileTweetsFragment);
            }
        }, 1000);
    }

    /**
     * Setup view pager placed in header
     */
    private void setupHeaderPager() {
        ArrayList<Fragment> fragments = new ArrayList<>();

        headerStatsFragment.setProfileListener(this);
        headerInfoFragment.setProfileListener(this);

        fragments.add(headerStatsFragment);
        fragments.add(headerInfoFragment);

        SimplePagerAdapter adapter = new SimplePagerAdapter(fragments, getChildFragmentManager());
        binding.vpProfileHeader.setAdapter(adapter);
        binding.stbProfileHeader.setViewPager(binding.vpProfileHeader);
        binding.stbProfileHeader.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 1:
                        headerInfoFragment.changeProfileHeight(true);
                        break;

                    case 0:
                        headerStatsFragment.changeProfileHeight(true);
                        break;
                }
            }
        });
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
                        paramsFader.topMargin = ((int) (Utilities.convertDpToPixel(FADER_Y_OFFSET, getContext()) + difference - (difference * interpolatedTime)));
                    } else {
                        paramsFader.topMargin = ((int) (Utilities.convertDpToPixel(FADER_Y_OFFSET, getContext()) + (diff * interpolatedTime)));
                    }
                    binding.rlHeaderFader.setLayoutParams(paramsFader);
                }
            };

            Animation animStb = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (diff == 0) {
                        paramsStbContent.topMargin = ((int) (Utilities.convertDpToPixel(TABS_Y_OFFSET, getContext()) + difference - (difference * interpolatedTime)));
                    } else {
                        paramsStbContent.topMargin = ((int) (Utilities.convertDpToPixel(TABS_Y_OFFSET, getContext()) + (diff * interpolatedTime)));
                    }
                    binding.stbProfileFragment.setLayoutParams(paramsStbContent);
                }
            };

            Animation animFragment = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (diff == 0) {
                        paramsFlContent.topMargin = ((int) (Utilities.convertDpToPixel(TAIL_Y_OFFSET, getContext()) + difference - (difference * interpolatedTime)));
                    } else {
                        paramsFlContent.topMargin = ((int) (Utilities.convertDpToPixel(TAIL_Y_OFFSET, getContext()) + (diff * interpolatedTime)));
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
            Log.e(TAG, "no animation - " + diff);
            if (diff >= 0) {
                paramsVpHeader.height = ((int) (Utilities.convertDpToPixel(ESTIMATED_PAGER_SIZE, getContext()) + diff));
                binding.vpProfileHeader.setLayoutParams(paramsVpHeader);
            }

            paramsFader.topMargin = ((int) (Utilities.convertDpToPixel(FADER_Y_OFFSET, getContext()) + diff));
            binding.rlHeaderFader.setLayoutParams(paramsFader);

            paramsStbContent.topMargin = ((int) (Utilities.convertDpToPixel(TABS_Y_OFFSET, getContext()) + diff));
            binding.stbProfileFragment.setLayoutParams(paramsStbContent);

            paramsFlContent.topMargin = ((int) (Utilities.convertDpToPixel(TAIL_Y_OFFSET, getContext()) + diff));
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

        verifiedParams.width = (int) (Utilities.convertDpToPixel(VERIFIED_SIZE, getContext()) - scrollY) < 0 ?
                0 : (int) (Utilities.convertDpToPixel(VERIFIED_SIZE, getContext()) - scrollY);
        verifiedParams.height = (int) (Utilities.convertDpToPixel(VERIFIED_SIZE, getContext()) - scrollY) < 0 ?
                0 : (int) (Utilities.convertDpToPixel(VERIFIED_SIZE, getContext()) - scrollY);

        params.width = (int) (Utilities.convertDpToPixel(AVATAR_SIZE, getContext()) - scrollY) < 0 ?
                0 : (int) (Utilities.convertDpToPixel(AVATAR_SIZE, getContext()) - scrollY);
        params.height = (int) (Utilities.convertDpToPixel(AVATAR_SIZE, getContext()) - scrollY) < 0 ?
                0 : (int) (Utilities.convertDpToPixel(AVATAR_SIZE, getContext()) - scrollY);
        params.topMargin = -params.height / 2;

        verifiedParams.bottomMargin = (int) ((Utilities.convertDpToPixel(VERIFIED_SIZE, getContext()) - verifiedParams.height) / 3);
        verifiedParams.rightMargin = (int) ((Utilities.convertDpToPixel(VERIFIED_SIZE, getContext()) - verifiedParams.height) / 3);

        float progress = (Utilities.convertDpToPixel(AVATAR_SIZE, getContext()) - scrollY) / Utilities.convertDpToPixel(AVATAR_SIZE, getContext());
        binding.imgProfileAvatar.setAlpha(progress);

        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        binding.imgProfileAvatar.setLayoutParams(params);
        binding.imgProfileVerified.setLayoutParams(verifiedParams);
    }

    @Override
    public void updateInfo() {
        headerInfoFragment.setViewModel(
                AppData.ME.getDescription(),
                AppData.ME.getUrlEntity() == null ? "" : AppData.ME.getUrlEntity().get("displayURL").getAsString(),
                AppData.ME.getLocation());
    }

    @Override
    public void updateStats() {
        final FileWork fileWork = new FileWork(getContext());
        String oldFollowers = fileWork.readFromFile(AppData.ME.getId() + "followers");
        String oldFollowing = fileWork.readFromFile(AppData.ME.getId() + "following");
        String oldTweets = fileWork.readFromFile(AppData.ME.getId() + "tweets");
        String oldListed = fileWork.readFromFile(AppData.ME.getId() + "listed");

        long followers = TextUtils.isEmpty(oldFollowers) ? 0 : (AppData.ME.getFollowersCount() - Integer.valueOf(oldFollowers));
        long following = TextUtils.isEmpty(oldFollowing) ? 0 : (AppData.ME.getFriendsCount() - Integer.valueOf(oldFollowing));
        long tweets = TextUtils.isEmpty(oldTweets) ? 0 : (AppData.ME.getStatusesCount() - Integer.valueOf(oldTweets));
        long listed = TextUtils.isEmpty(oldListed) ? 0 : (AppData.ME.getListedCount() - Integer.valueOf(oldListed));

        headerStatsFragment.setViewModel(
                AppData.ME.getFollowersCount(), AppData.ME.getFriendsCount(), AppData.ME.getStatusesCount(),
                AppData.ME.getListedCount(), (int) followers, (int) following, (int) tweets, (int) listed);

        new Thread(new Runnable() {
            @Override
            public void run() {
                fileWork.writeToFile(String.valueOf(AppData.ME.getFollowersCount()), AppData.ME.getId() + "followers");
                fileWork.writeToFile(String.valueOf(AppData.ME.getFriendsCount()), AppData.ME.getId() + "following");
                fileWork.writeToFile(String.valueOf(AppData.ME.getStatusesCount()), AppData.ME.getId() + "tweets");
                fileWork.writeToFile(String.valueOf(AppData.ME.getListedCount()), AppData.ME.getId() + "listed");

            }
        }).start();
    }

    @Override
    public void openActivity(Intent intent) {
        AppData.CURRENT_USER = AppData.ME;
        mActivity.startActivity(intent);
        mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void openLink(String string) {
        Utilities.openLink(string, mActivity);
    }
}
