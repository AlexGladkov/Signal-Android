package solonsky.signal.twitter.fragments;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.google.gson.Gson;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.util.ArrayList;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Transitions;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.interfaces.ActivityListener;
import solonsky.signal.twitter.interfaces.SearchListener;
import solonsky.signal.twitter.models.StatusModel;
import solonsky.signal.twitter.models.User;
import twitter4j.AsyncTwitter;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.URLEntity;

/**
 * Created by neura on 27.07.17.
 */

public class SearchedFragment extends Fragment implements SmartTabLayout.TabProvider {
    private final String TAG = SearchedFragment.class.getSimpleName();

    private SearchPeopleFragment searchPeopleFragment = new SearchPeopleFragment();
    private SearchAllFragment searchAllFragment = new SearchAllFragment();
    private SearchMediaFragment searchMediaFragment = new SearchMediaFragment();
    private SearchHomeFragment searchHomeFragment = new SearchHomeFragment();

    private ArrayList<StatusModel> searchedModels = new ArrayList<>();
    private ArrayList<StatusModel> searchedMedia = new ArrayList<>();
    private ArrayList<StatusModel> homeModels = new ArrayList<>();
    private ArrayList<User> userModels = new ArrayList<>();

    private Fragment lastFragment = null;
//    private LoggedActivity mActivity;

    private int CURRENT_POSITION = Flags.isSearchUser ? 1 : 0;
    private SmartTabLayout smartTabLayout;
    private ViewPager viewPager;
    private SearchListener mCallback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_searched, container, false);

//        mActivity = (LoggedActivity) getActivity();
//        mActivity.viewModel.setStaticBottomBar(false);
//        mActivity.viewModel.setStaticToolbar(false);

        smartTabLayout = (SmartTabLayout) view.findViewById(R.id.stb_search);
        viewPager = (ViewPager) view.findViewById(R.id.vp_search);

        ScrollView scrollView = (ScrollView) view.findViewById(R.id.sl_searched);
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (mCallback != null)
                    mCallback.updateBar(scrollY - oldScrollY);
            }
        });

        smartTabLayout.setCustomTabView(this);
        smartTabLayout.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        selectFragment(searchAllFragment);
                        break;

                    case 1:
                        selectFragment(searchPeopleFragment);
                        break;

                    case 2:
                        selectFragment(searchMediaFragment);
                        break;

                    case 3:
                        selectFragment(searchHomeFragment);
                        break;
                }

                View oldTab = smartTabLayout.getTabAt(CURRENT_POSITION);
                View currentTab = smartTabLayout.getTabAt(position);

                RelativeLayout oldView = (RelativeLayout) oldTab.findViewById(R.id.tab_layout);
                RelativeLayout currentView = (RelativeLayout) currentTab.findViewById(R.id.tab_layout);

                oldView.setBackground(getResources().getDrawable(R.drawable.tab_shape_transparent));
                currentView.setBackground(getResources().getDrawable(App.getInstance().isNightEnabled() ?
                        R.drawable.tab_shape_dark : R.drawable.tab_shape_light));

                CURRENT_POSITION = position;
            }
        });

        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(getActivity())
                .add("All", DummyFragment.class)
                .add("3", DummyFragment.class)
                .add("7", DummyFragment.class)
                .add("5", DummyFragment.class)
                .create());

        viewPager.setAdapter(adapter);
        smartTabLayout.setViewPager(viewPager);
        initFragments();
        loadData();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (SearchListener) context;
        } catch (ClassCastException e) {
            //Do nothing
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public void filterSource(int position, int source, boolean value) {
        switch (position) {
            case 0:
                searchAllFragment.filterSource(source, value);
                break;

            case 1:
                searchPeopleFragment.filterSource(source, value);
                break;

            case 2:
                searchMediaFragment.filterSource(source, value);
                break;

            case 3:
                searchHomeFragment.filterSource(source, value);
                break;
        }
    }

    public void setupList(int position) {
        switch (position) {
            case 0:
                searchAllFragment.setupList();
                break;

            case 1:
                searchPeopleFragment.setupList();
                break;

            case 2:
                searchMediaFragment.setupList();
                break;

            case 3:
                searchHomeFragment.setupList();
                break;
        }
    }

    private void initFragments() {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        fragmentTransaction.replace(R.id.fl_search,
                Flags.isSearchUser ? searchPeopleFragment : searchAllFragment,
                Flags.isSearchUser ? searchPeopleFragment.getTag() : searchAllFragment.getTag());
        fragmentTransaction.add(R.id.fl_search, searchHomeFragment, searchHomeFragment.getTag());
        fragmentTransaction.add(R.id.fl_search, searchMediaFragment, searchMediaFragment.getTag());
        fragmentTransaction.add(R.id.fl_search,
                Flags.isSearchUser ? searchAllFragment : searchPeopleFragment,
                Flags.isSearchUser ? searchAllFragment.getTag() : searchPeopleFragment.getTag());

        fragmentTransaction.hide(searchHomeFragment);
        fragmentTransaction.hide(searchMediaFragment);
        fragmentTransaction.hide(Flags.isSearchUser ? searchAllFragment : searchPeopleFragment);
        fragmentTransaction.show(Flags.isSearchUser ? searchPeopleFragment : searchAllFragment);

        fragmentTransaction.commit();

        lastFragment = Flags.isSearchUser ? searchPeopleFragment : searchAllFragment;
    }

    private void loadData() {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            Gson gson = new Gson();

            @Override
            public void gotHomeTimeline(ResponseList<Status> statuses) {
                super.gotHomeTimeline(statuses);
                final String query = AppData.searchQuery;
                for (int i = 0; i < statuses.size(); i++) {
                    Status status = statuses.get(i);
                    final StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                    statusModel.tuneModel(status);
                    statusModel.linkClarify();

                    statusModel.setRetweet(status.isRetweet());
                    statusModel.setRetweetedByMe(status.isRetweetedByMe());

                    if (isHome(status, query)) {
                        homeModels.add(statusModel);
                    }

//                    if (statusModel.isRetweet() && (statusModel.getRetweetedStatus().getText().toLowerCase()
//                            .contains(query.toLowerCase()) || statusModel.getRetweetedStatus().getUser().getName()
//                            .toLowerCase().contains(query.toLowerCase()) || statusModel.getRetweetedStatus().getUser().getScreenName()
//                            .toLowerCase().contains(query.toLowerCase()) || statusModel.getUser().getName().toLowerCase()
//                            .contains(query.toLowerCase()) || statusModel.getUser().getScreenName().toLowerCase().contains(query.toLowerCase()))) {
//                        homeModels.add(statusModel);
//                    } else if (statusModel.getText().toLowerCase().contains(query.toLowerCase()) ||
//                            statusModel.getUser().getName().toLowerCase().contains(query.toLowerCase()) ||
//                            statusModel.getUser().getScreenName().toLowerCase().contains(query.toLowerCase())) {
//                        homeModels.add(statusModel);
//                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        searchHomeFragment.updateData(homeModels);
                    }
                });
            }

            @Override
            public void searchedUser(ResponseList<twitter4j.User> userList) {
                super.searchedUser(userList);
                for (int i = 0; i < userList.size(); i++) {
                    User user = gson.fromJson(gson.toJsonTree(userList.get(i)), User.class);
                    user.setBiggerProfileImageURL(userList.get(i).getBiggerProfileImageURL());
                    user.setOriginalProfileImageURL(userList.get(i).getOriginalProfileImageURL());
                    userModels.add(user);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        searchPeopleFragment.updateData(userModels);
                        if (Flags.isSearchUser) {
                            selectFragment(searchPeopleFragment);
                            viewPager.setCurrentItem(1);
                            Flags.isSearchUser = false;
                        }
                    }
                });
            }

            @Override
            public void searched(final QueryResult queryResult) {
                super.searched(queryResult);
                for (Status status : queryResult.getTweets()) {
                    StatusModel statusModel = gson.fromJson(gson.toJsonTree(status), StatusModel.class);
                    statusModel.parseYoutubeTest();
                    statusModel.tuneModel(status);
                    statusModel.linkClarify();

                    statusModel.setRetweet(status.isRetweet());
                    statusModel.setRetweetedByMe(status.isRetweetedByMe());

                    searchedModels.add(statusModel);

                    if (statusModel.getMediaEntities().size() > 0 || statusModel.getUrlEntities().size() > 0) {
                        searchedMedia.add(statusModel);
                    }
                }

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        searchAllFragment.updateData(searchedModels);
                        searchMediaFragment.updateData(searchedMedia);
                    }
                }, 100);
            }
        });

        asyncTwitter.search(new Query(AppData.searchQuery));
        asyncTwitter.searchUsers(AppData.searchQuery, 0);
        asyncTwitter.getHomeTimeline(new Paging(1, 100));
    }

    private void selectFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (fragment.isAdded()) {
            fragmentTransaction.hide(lastFragment).show(fragment).commit();
        }

        lastFragment = fragment;
    }

    @Override
    public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        Resources res = container.getContext().getResources();
        View tab = inflater.inflate(R.layout.tab_search_item, container, false);
        ImageView imageView = (ImageView) tab.findViewById(R.id.tab_iv);
        RelativeLayout layoutView = (RelativeLayout) tab.findViewById(R.id.tab_layout);

        layoutView.setBackground(CURRENT_POSITION == position ?
                res.getDrawable(App.getInstance().isNightEnabled() ?
                        R.drawable.tab_shape_dark : R.drawable.tab_shape_light)
                : res.getDrawable(R.drawable.tab_shape_transparent));

        boolean isNightEnabled = App.getInstance().isNightEnabled();
        switch (position) {
            case 0:
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_search_all));
                imageView.setColorFilter(res.getColor(isNightEnabled ?
                        R.color.dark_profile_tint_color : R.color.light_profile_tint_color));
                break;

            case 1:
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_search_user));
                imageView.setColorFilter(res.getColor(isNightEnabled ?
                        R.color.dark_profile_tint_color : R.color.light_profile_tint_color));
                break;

            case 2:
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_search_media));
                imageView.setColorFilter(res.getColor(isNightEnabled ?
                        R.color.dark_profile_tint_color : R.color.light_profile_tint_color));
                break;

            case 3:
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_search_feed));
                imageView.setColorFilter(res.getColor(App.getInstance().isNightEnabled() ?
                        R.color.dark_profile_tint_color : R.color.light_profile_tint_color));
                break;
        }

        return tab;
    }

    private boolean isHome(Status statusModel, String query) {
        if (statusModel.getText().toLowerCase().contains(query.toLowerCase())) {
            return true;
        }

        if (statusModel.getUser().getName().toLowerCase().contains(query.toLowerCase())) {
            return true;
        }

        if (statusModel.getUser().getScreenName().toLowerCase().contains(query.toLowerCase())) {
            return true;
        }

        if (statusModel.getRetweetedStatus() != null && statusModel.
                getRetweetedStatus().getUser().getName().toLowerCase().contains(query.toLowerCase())) {
            return true;
        }

        if (statusModel.getRetweetedStatus() != null && statusModel.
                getRetweetedStatus().getUser().getScreenName().toLowerCase().contains(query.toLowerCase())) {
            return true;
        }

        boolean hasUrl = false;
        for (URLEntity urlEntity : statusModel.getURLEntities()) {
            if (urlEntity.getDisplayURL().toLowerCase().contains(query.toLowerCase())) {
                hasUrl = true;
            }
        }

        return hasUrl;
    }
}
