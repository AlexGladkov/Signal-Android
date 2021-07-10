package solonsky.signal.twitter.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.api.NotificationsApi;
import solonsky.signal.twitter.data.LoggedData;
import solonsky.signal.twitter.data.NotificationsAllData;
import solonsky.signal.twitter.databinding.FragmentNotificationsBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.interfaces.ActivityListener;
import solonsky.signal.twitter.interfaces.FragmentCounterListener;
import solonsky.signal.twitter.interfaces.UpdateHandler;
import solonsky.signal.twitter.libs.NonSwipeableViewPager;
import solonsky.signal.twitter.viewmodels.NotificationsViewModel;

/**
 * Created by neura on 19.05.17.
 */

public class NotificationsFragment extends Fragment implements SmartTabLayout.TabProvider, FragmentCounterListener {
    private final String TAG = NotificationsFragment.class.getSimpleName();
    private int CURRENT_POSITION = 0;

    private UpdateHandler updateHandler;

    private NotificationsAllFragment notificationsAllFragment = new NotificationsAllFragment();
    private NotificationsReplyFragment notificationsReplyFragment = new NotificationsReplyFragment();
    private NotificationsLikeFragment notificationsLikeFragment = new NotificationsLikeFragment();
    private NotificationsRetweetFragment notificationsRetweetFragment = new NotificationsRetweetFragment();
    private NotificationsFollowFragment notificationsFollowFragment = new NotificationsFollowFragment();
    private Fragment lastFragment = null;
    private ActivityListener mCallback;
    private FragmentNotificationsBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notifications, container, false);
        binding.stbNotificationsFragment.setCustomTabView(this);

        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(getActivity())
                .add("", DummyFragment.class)
                .add("0", DummyFragment.class)
                .add("7", DummyFragment.class)
                .add("5", DummyFragment.class)
                .add("8", DummyFragment.class)
                .create());

        binding.vpNotificationsFragment.setAdapter(adapter);
        binding.vpNotificationsFragment.setPageTransformer(false, new NonSwipeableViewPager.FadePageTransformer());
        binding.stbNotificationsFragment.setViewPager(binding.vpNotificationsFragment);
        binding.stbNotificationsFragment.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    default:
                        if (mCallback != null) {
                            mCallback.updateTitle(R.string.title_notification);
                            mCallback.updateCounter(NotificationsAllData.getInstance().getEntryCount());
                        }
                        selectFragment(notificationsAllFragment);
                        break;

                    case 1:
                        if (mCallback != null) {
                            mCallback.updateStatusType(Flags.STATUS_TYPE.TITLE);
                            mCallback.updateTitle(R.string.notifications_title_reply);
                        }
                        selectFragment(notificationsReplyFragment);
                        break;

                    case 2:
                        if (mCallback != null) {
                            mCallback.updateStatusType(Flags.STATUS_TYPE.TITLE);
                            mCallback.updateTitle(R.string.notifications_title_like);
                        }
                        selectFragment(notificationsLikeFragment);
                        break;

                    case 3:
                        if (mCallback != null) {
                            mCallback.updateStatusType(Flags.STATUS_TYPE.TITLE);
                            mCallback.updateTitle(R.string.notifications_title_retweet);
                        }
                        selectFragment(notificationsRetweetFragment);
                        break;

                    case 4:
                        if (mCallback != null) {
                            mCallback.updateStatusType(Flags.STATUS_TYPE.TITLE);
                            mCallback.updateTitle(R.string.notifications_title_follow);
                        }
                        selectFragment(notificationsFollowFragment);
                }

                View oldTab = binding.stbNotificationsFragment.getTabAt(CURRENT_POSITION);
                View currentTab = binding.stbNotificationsFragment.getTabAt(position);

                RelativeLayout oldView = (RelativeLayout) oldTab.findViewById(R.id.tab_layout);
                RelativeLayout currentView = (RelativeLayout) currentTab.findViewById(R.id.tab_layout);

                oldView.setBackground(getResources().getDrawable(R.drawable.tab_shape_transparent));
                currentView.setBackground(getResources().getDrawable(App.getInstance().isNightEnabled() ?
                        R.drawable.tab_shape_dark : R.drawable.tab_shape_light));

                CURRENT_POSITION = position;
            }
        });

        NotificationsViewModel viewModel = new NotificationsViewModel();
        binding.setNotifications(viewModel);

        initFragments();
        selectFragment(notificationsAllFragment);
        NotificationsApi.initData();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mCallback != null) {
            mCallback.updateCounter(0);
            mCallback.updateSettings(R.string.title_notification, true, true);
            mCallback.updateToolbarState(AppData.TOOLBAR_LOGGED_MAIN, App.getInstance().isNightEnabled() ?
                    R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color);
        }

        this.updateHandler = new UpdateHandler() {
            @Override
            public void onUpdate() {
                if (mCallback != null) {
                    mCallback.updateCounter(NotificationsAllData.getInstance().getEntryCount());
                }
//                updateView(likeView, NotificationsLikeData.getInstance().getLikesList().size());
//                updateView(replyView, NotificationsReplyData.getInstance().getReplyList().size());
//                updateView(rtView, NotificationsRetweetData.getInstance().getRetweetList().size());
//                updateView(peopleView, NotificationsFollowData.getInstance().getFollowList().size());
            }
        };
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            binding.vpNotificationsFragment.setCurrentItem(0);
            selectFragment(notificationsAllFragment);
            notificationsAllFragment.onScrollToTop();
            if (mCallback != null) {
                mCallback.updateCounter(NotificationsAllData.getInstance().getEntryCount());
                mCallback.updateSettings(R.string.title_notification, true, true);
                mCallback.updateToolbarState(AppData.TOOLBAR_LOGGED_MAIN, App.getInstance().isNightEnabled() ?
                        R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (ActivityListener) context;
        } catch (ClassCastException e) {
            // Do nothing
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    private void initFragments() {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fl_notifications_fragments, notificationsAllFragment);
        fragmentTransaction.add(R.id.fl_notifications_fragments, notificationsReplyFragment);
        fragmentTransaction.add(R.id.fl_notifications_fragments, notificationsLikeFragment);
        fragmentTransaction.add(R.id.fl_notifications_fragments, notificationsRetweetFragment);
        fragmentTransaction.add(R.id.fl_notifications_fragments, notificationsFollowFragment);
        fragmentTransaction.hide(notificationsReplyFragment);
        fragmentTransaction.hide(notificationsLikeFragment);
        fragmentTransaction.hide(notificationsRetweetFragment);
        fragmentTransaction.hide(notificationsFollowFragment);
        fragmentTransaction.commit();

        lastFragment = notificationsAllFragment;
    }

    @Override
    public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        Resources res = container.getContext().getResources();
        View tab = inflater.inflate(R.layout.tab_item, container, false);
        ImageView imageView = (ImageView) tab.findViewById(R.id.tab_iv);
        TextView textView = (TextView) tab.findViewById(R.id.tab_txt);
        RelativeLayout layoutView = (RelativeLayout) tab.findViewById(R.id.tab_layout);

        textView.setTextColor(res.getColor(App.getInstance().isNightEnabled() ?
                R.color.dark_primary_text_color : R.color.light_primary_text_color));
        layoutView.setBackground(CURRENT_POSITION == position ?
                res.getDrawable(App.getInstance().isNightEnabled() ?
                        R.drawable.tab_shape_dark : R.drawable.tab_shape_light)
                : res.getDrawable(R.drawable.tab_shape_transparent));

        int size = 0;

        switch (position) {
            case 0:
                textView.setText("");
                textView.setVisibility(View.GONE);
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_tiny_list));
                imageView.setColorFilter(res.getColor(App.getInstance().isNightEnabled() ?
                        R.color.dark_hint_text_color : R.color.light_hint_text_color));
                break;

            case 1:
//                size = NotificationsReplyData.getInstance().getReplyList().size();
//                textView.setText(String.valueOf(size));
                textView.setVisibility(View.GONE);
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_tiny_reply));
                imageView.setColorFilter(res.getColor(App.getInstance().isNightEnabled() ?
                        R.color.dark_reply_tint_color : R.color.light_reply_tint_color));
//                replyView = tab;
//                imageView.setAlpha(size == 0 ? 0.3f : 1.0f);
//                textView.setAlpha(size == 0 ? 0.3f : 1.0f);
                break;

            case 2:
//                size = NotificationsLikeData.getInstance().getLikesList().size();
//                textView.setText(String.valueOf(size));
//                likeView = tab;
                textView.setVisibility(View.GONE);
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_tiny_like));
                imageView.setColorFilter(res.getColor(App.getInstance().isNightEnabled() ?
                        R.color.dark_like_tint_color : R.color.light_like_tint_color));
//                imageView.setAlpha(size == 0 ? 0.3f : 1.0f);
//                textView.setAlpha(size == 0 ? 0.3f : 1.0f);
                break;

            case 3:
//                size = NotificationsRetweetData.getInstance().getRetweetList().size();
//                textView.setText(String.valueOf(size));
//                rtView = tab;
                textView.setVisibility(View.GONE);
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_tiny_rt));
                imageView.setColorFilter(res.getColor(App.getInstance().isNightEnabled() ?
                        R.color.dark_rt_tint_color : R.color.light_rt_tint_color));
//                imageView.setAlpha(size == 0 ? 0.3f : 1.0f);
//                textView.setAlpha(size == 0 ? 0.3f : 1.0f);
                break;

            case 4:
//                size = NotificationsFollowData.getInstance().getFollowList().size();
//                textView.setText(String.valueOf(size));
//                peopleView = tab;
                textView.setVisibility(View.GONE);
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_tiny_user));
                imageView.setColorFilter(res.getColor(App.getInstance().isNightEnabled() ?
                        R.color.dark_profile_tint_color : R.color.light_profile_tint_color));
//                imageView.setAlpha(size == 0 ? 0.3f : 1.0f);
//                textView.setAlpha(size == 0 ? 0.3f : 1.0f);
                break;
        }

        return tab;
    }

//    private void updateView(View view, int count) {
//        if (view != null && count > 0) {
//            ((TextView) view.findViewById(R.id.tab_txt)).setText(String.valueOf(count));
//            view.findViewById(R.id.tab_txt).setAlpha(1);
//            view.findViewById(R.id.tab_iv).setAlpha(1);
//        }
//    }


    private void selectFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        if (lastFragment != null) fragmentTransaction.hide(lastFragment);
        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.show(fragment).commit();
        lastFragment = fragment;
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    @Override
    public void onScrollToTop() {
        Log.e(TAG, "scroll to top");
    }

    @Override
    public void onScrollToTopWithAnimation(int startDuration, int pauseDuration, int endDuration) {
        switch (CURRENT_POSITION) {
            case 0:
                notificationsAllFragment.onScrollToTopWithAnimation(startDuration, pauseDuration, endDuration);
                break;

            case 1:
                notificationsReplyFragment.onScrollToTopWithAnimation(startDuration, pauseDuration, endDuration);
                break;

            case 2:
                notificationsLikeFragment.onScrollToTopWithAnimation(startDuration, pauseDuration, endDuration);
                break;

            case 3:
                notificationsRetweetFragment.onScrollToTopWithAnimation(startDuration, pauseDuration, endDuration);
                break;

            case 4:
                notificationsFollowFragment.onScrollToTopWithAnimation(startDuration, pauseDuration, endDuration);
                break;
        }
    }

    @Override
    public void onBackToPosition() {

    }

    @Override
    public void onUpdate() {
        if (CURRENT_POSITION == 0) {
            NotificationsAllData.getInstance().clearNew();
            LoggedData.getInstance().setNewActivity(false);
            LoggedData.getInstance().getUpdateHandler().onUpdate();

            if (mCallback != null)
                mCallback.updateCounter(NotificationsAllData.getInstance().getEntryCount());
        }
    }
}
