package solonsky.signal.twitter.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.databinding.ActivitySearchBinding;
import solonsky.signal.twitter.fragments.SearchedFragment;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.interfaces.SearchListener;
import solonsky.signal.twitter.viewmodels.SearchActivityViewModel;
import twitter4j.AsyncTwitter;
import twitter4j.SavedSearch;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 13.10.17.
 */

public class SearchActivity extends AppCompatActivity implements SearchListener {
    private ActivitySearchBinding binding;
    private final String TAG = SearchActivity.class.getSimpleName();
    private SearchActivity mActivity;
    private PopupMenu popupMenuAll;
    private PopupMenu popupMenuPeople;
    private PopupMenu popupMenuMedia;
    private PopupMenu popupMenuHome;
    private int TAB_POSITION = 0;
    private SearchedFragment searchedFragment;

    private final int popular = 0;
    private final int retweet = 1;
    private final int followers = 2;
    private final int verified = 3;
    private final int images = 4;
    private final int video = 5;
    private final int links = 6;
    private final int mentions = 7;

    private boolean isRetweet = true;
    private boolean isPopular = true;
    private boolean isFollowers = false;
    private boolean isVerified = false;
    private boolean isHomeImages = true;
    private boolean isHomeVideo = true;
    private boolean isHomeLinks = true;
    private boolean isHomeMentions = true;
    private boolean isHomeRetweet = true;
    private boolean isMediaImages = true;
    private boolean isMediaVideo = true;
    private boolean isMediaLinks = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.getInstance().isNightEnabled()) {
            setTheme(R.style.ActivityThemeDark);
        }

        mActivity = this;

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(getResources().getColor(App.getInstance().isNightEnabled() ?
                R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color));

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        SearchActivityViewModel viewModel = new SearchActivityViewModel();
        binding.setModel(viewModel);
        binding.setClick(new SearchActivityViewModel.SearchActivityClickHandler() {
            @Override
            public void onBackClick(View v) {
                onBackPressed();
            }
        });
        binding.txtSearchInput.setText(AppData.searchQuery);
        binding.flSearchMain.requestFocus();

        searchedFragment = new SearchedFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_search, searchedFragment).commit();

        initPopupAll();
        initPopupHome();
        initPopupPeople();
        initPopupMedia();

        binding.txtSearchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    startSearch(binding.txtSearchInput.getText().toString());
                }
                return false;
            }
        });

        binding.btnSearchMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (TAB_POSITION) {
                    case 0:
                        popupMenuAll.show();
                        break;

                    case 1:
                        popupMenuPeople.show();
                        break;

                    case 2:
                        popupMenuMedia.show();
                        break;

                    case 3:
                        popupMenuHome.show();
                        break;
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
    }

    private void initPopupAll() {
        popupMenuAll = new PopupMenu(mActivity, mActivity.binding.btnSearchMore, 0, 0, R.style.popup_menu);
        MenuInflater menuInflater = popupMenuAll.getMenuInflater();
        menuInflater.inflate(R.menu.menu_search_all, popupMenuAll.getMenu());

        final Handler handler = new Handler();
        popupMenuAll.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
                asyncTwitter.addListener(new TwitterAdapter() {
                    @Override
                    public void createdSavedSearch(SavedSearch savedSearch) {
                        super.createdSavedSearch(savedSearch);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), getString(R.string.created_saved_search), Toast.LENGTH_SHORT).show();
                                Flags.isSearchSaved = true;
                            }
                        });
                    }

                    @Override
                    public void onException(TwitterException te, TwitterMethod method) {
                        super.onException(te, method);
                        Log.e(TAG, "Error - " + te.getLocalizedMessage() + " method - " + method.name());
                        if (method.name().equals("CREATE_SAVED_SEARCH")) {
                            Toast.makeText(getApplicationContext(), R.string.error_saving_search, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                switch (item.getItemId()) {
                    case R.id.search_save:
                        asyncTwitter.createSavedSearch(AppData.searchQuery);
                        break;

                    case R.id.search_include_retweets:
                        item.setChecked(!item.isChecked());
                        isRetweet = !isRetweet;
                        searchedFragment.filterSource(TAB_POSITION, retweet, isRetweet);
                        searchedFragment.setupList(TAB_POSITION);
                        break;

                    case R.id.search_popular:
                        item.setChecked(!item.isChecked());
                        isPopular = !isPopular;
                        searchedFragment.filterSource(TAB_POSITION, popular, isPopular);
                        searchedFragment.setupList(TAB_POSITION);
                        break;
                }
                return true;
            }
        });
    }

    private void initPopupPeople() {
        popupMenuPeople = new PopupMenu(mActivity, binding.btnSearchMore, 0, 0, R.style.popup_menu);
        MenuInflater menuInflater = popupMenuPeople.getMenuInflater();
        menuInflater.inflate(R.menu.menu_search_people, popupMenuPeople.getMenu());

        final Handler handler = new Handler();
        popupMenuPeople.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.search_save:
                        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
                        asyncTwitter.addListener(new TwitterAdapter() {
                            @Override
                            public void createdSavedSearch(SavedSearch savedSearch) {
                                super.createdSavedSearch(savedSearch);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.created_saved_search), Toast.LENGTH_SHORT).show();
                                        Flags.isSearchSaved = true;
                                    }
                                });
                            }

                            @Override
                            public void onException(TwitterException te, TwitterMethod method) {
                                super.onException(te, method);
                                if (method.name().equals("CREATE_SAVED_SEARCH")) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.error_saving_search), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        asyncTwitter.createSavedSearch(AppData.searchQuery);
                        break;

                    case R.id.search_followers_only:
                        item.setChecked(!item.isChecked());
                        isFollowers = !isFollowers;
                        searchedFragment.filterSource(TAB_POSITION, followers, isFollowers);
                        searchedFragment.setupList(TAB_POSITION);
                        break;

                    case R.id.search_verified_only:
                        item.setChecked(!item.isChecked());
                        isVerified = !isVerified;
                        searchedFragment.filterSource(TAB_POSITION, verified, isVerified);
                        searchedFragment.setupList(TAB_POSITION);
                        break;
                }
                return false;
            }
        });
    }

    private void initPopupHome() {
        popupMenuHome = new PopupMenu(mActivity, mActivity.binding.btnSearchMore, 0, 0, R.style.popup_menu);
        MenuInflater menuInflater = popupMenuHome.getMenuInflater();
        menuInflater.inflate(R.menu.menu_search_home, popupMenuHome.getMenu());

        final Handler handler = new Handler();
        popupMenuHome.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.search_save:
                        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
                        asyncTwitter.addListener(new TwitterAdapter() {
                            @Override
                            public void createdSavedSearch(SavedSearch savedSearch) {
                                super.createdSavedSearch(savedSearch);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.created_saved_search), Toast.LENGTH_SHORT).show();
                                        Flags.isSearchSaved = true;
                                    }
                                });
                            }

                            @Override
                            public void onException(TwitterException te, TwitterMethod method) {
                                super.onException(te, method);
                                if (method.name().equals("CREATE_SAVED_SEARCH")) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.error_saving_search), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        asyncTwitter.createSavedSearch(AppData.searchQuery);
                        break;

                    case R.id.search_images:
                        item.setChecked(!item.isChecked());
                        isHomeImages = !isHomeImages;
                        searchedFragment.filterSource(TAB_POSITION, images, isHomeImages);
                        searchedFragment.setupList(TAB_POSITION);
                        break;

                    case R.id.search_video:
                        item.setChecked(!item.isChecked());
                        isHomeVideo = !isHomeVideo;
                        searchedFragment.filterSource(TAB_POSITION, video, isHomeVideo);
                        searchedFragment.setupList(TAB_POSITION);
                        break;

                    case R.id.search_links:
                        item.setChecked(!item.isChecked());
                        isHomeLinks = !isHomeLinks;
                        searchedFragment.filterSource(TAB_POSITION, links, isHomeLinks);
                        searchedFragment.setupList(TAB_POSITION);
                        break;

                    case R.id.search_include_mentions:
                        item.setChecked(!item.isChecked());
                        isHomeMentions = !isHomeMentions;
                        searchedFragment.filterSource(TAB_POSITION, mentions, isHomeMentions);
                        searchedFragment.setupList(TAB_POSITION);
                        break;

                    case R.id.search_include_retweets:
                        item.setChecked(!item.isChecked());
                        isHomeRetweet = !isHomeRetweet;
                        searchedFragment.filterSource(TAB_POSITION, retweet, isHomeRetweet);
                        searchedFragment.setupList(TAB_POSITION);
                        break;

                }
                return false;
            }
        });
    }

    private void initPopupMedia() {
        popupMenuMedia = new PopupMenu(mActivity, binding.btnSearchMore, 0, 0, R.style.popup_menu);
        MenuInflater menuInflater = popupMenuMedia.getMenuInflater();
        menuInflater.inflate(R.menu.menu_search_media, popupMenuMedia.getMenu());

        final Handler handler = new Handler();
        popupMenuMedia.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.search_save:
                        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
                        asyncTwitter.addListener(new TwitterAdapter() {
                            @Override
                            public void createdSavedSearch(SavedSearch savedSearch) {
                                super.createdSavedSearch(savedSearch);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), getString(R.string.created_saved_search), Toast.LENGTH_SHORT).show();
                                        Flags.isSearchSaved = true;
                                    }
                                });
                            }

                            @Override
                            public void onException(TwitterException te, TwitterMethod method) {
                                super.onException(te, method);
                                if (method.name().equals("CREATE_SAVED_SEARCH")) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.error_saving_search), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        asyncTwitter.createSavedSearch(AppData.searchQuery);
                        break;

                    case R.id.search_images:
                        item.setChecked(!item.isChecked());
                        isMediaImages = !isMediaImages;
                        searchedFragment.filterSource(TAB_POSITION, images, isMediaImages);
                        searchedFragment.setupList(TAB_POSITION);
                        break;

                    case R.id.search_video:
                        item.setChecked(!item.isChecked());
                        isMediaVideo = !isMediaVideo;
                        searchedFragment.filterSource(TAB_POSITION, video, isMediaVideo);
                        searchedFragment.setupList(TAB_POSITION);
                        break;

                    case R.id.search_links:
                        item.setChecked(!item.isChecked());
                        isMediaLinks = !isMediaLinks;
                        searchedFragment.filterSource(TAB_POSITION, links, isMediaLinks);
                        searchedFragment.setupList(TAB_POSITION);
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void updatePosition(int position) {
        TAB_POSITION = position;
    }

    @Override
    public void updateBar(int scrollY) {
        if (!AppData.appConfiguration.isStaticTopBars()) {
            FrameLayout.LayoutParams searchParams = (FrameLayout.LayoutParams) binding.tbSearch.getLayoutParams();
            int toolbarHeight = (int) Utilities.convertDpToPixel(80, getApplicationContext());
            int dropShadowHeight = (int) Utilities.convertDpToPixel(4, getApplicationContext());
            int topDiff = searchParams.topMargin - scrollY;

            searchParams.topMargin = topDiff > 0 ? 0 : topDiff <= -(toolbarHeight + dropShadowHeight) ? -(toolbarHeight + dropShadowHeight) : topDiff;
            binding.tbSearch.setLayoutParams(searchParams);

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) binding.viewLoggedDropShadow.getLayoutParams();
            topDiff = params.topMargin - scrollY;
            params.topMargin = topDiff > toolbarHeight ? toolbarHeight : topDiff <= -dropShadowHeight ? -dropShadowHeight : topDiff;
            binding.viewLoggedDropShadow.setLayoutParams(params);
        }
    }

    @Override
    public void startSearch(String searchText) {
        FrameLayout.LayoutParams searchParams = (FrameLayout.LayoutParams) binding.tbSearch.getLayoutParams();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) binding.viewLoggedDropShadow.getLayoutParams();

        searchParams.topMargin = 0;
        params.topMargin = 0;

        binding.tbSearch.setLayoutParams(searchParams);
        binding.viewLoggedDropShadow.setLayoutParams(params);

        AppData.searchQuery = searchText;
        binding.txtSearchInput.setText(searchText);
        binding.flSearchMain.requestFocus();
        Utilities.hideKeyboard(mActivity);
        searchedFragment = new SearchedFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_search, searchedFragment).commit();
    }
}
