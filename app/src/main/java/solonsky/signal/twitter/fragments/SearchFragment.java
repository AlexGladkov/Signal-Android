package solonsky.signal.twitter.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirGetCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ConcurrentModificationException;
import java.util.List;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.ComposeActivity;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.activities.SearchActivity;
import solonsky.signal.twitter.adapters.SearchAdapter;
import solonsky.signal.twitter.adapters.SimpleAdapter;
import solonsky.signal.twitter.data.SearchData;
import solonsky.signal.twitter.databinding.FragmentSearchBinding;
import solonsky.signal.twitter.helpers.App;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.interfaces.ActivityListener;
import solonsky.signal.twitter.models.SearchModel;
import solonsky.signal.twitter.models.SimpleModel;
import solonsky.signal.twitter.viewmodels.ComposeViewModel;
import solonsky.signal.twitter.viewmodels.SearchViewModel;
import twitter4j.AsyncTwitter;
import twitter4j.GeoLocation;
import twitter4j.GeoQuery;
import twitter4j.Location;
import twitter4j.Place;
import twitter4j.ResponseList;
import twitter4j.SavedSearch;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;

/**
 * Created by neura on 22.05.17.
 */

public class SearchFragment extends Fragment {
    private final String TAG = SearchFragment.class.getSimpleName();

    private SearchAdapter mSavedAdapter;
    private SearchAdapter mRecentAdapter;
    private SimpleAdapter mTrendsAdapter;
    private SearchViewModel viewModel;
    private FragmentSearchBinding binding;
    private LoggedActivity mActivity;
    private PopupMenu popupMenu;
    private ActivityListener mCallback;

    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (!viewModel.isLoading()) {
                viewModel.setLoading(true);
                loadData();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);

        mSavedAdapter = new SearchAdapter(SearchData.getInstance().getSavedList(), getContext(), savedClickListener);
        mRecentAdapter = new SearchAdapter(SearchData.getInstance().getRecentList(), getContext(), recentClickListener);
        mTrendsAdapter = new SimpleAdapter(SearchData.getInstance().getTrendsList(), getContext(), trendsClickListener);

        mSavedAdapter.setHasStableIds(true);
        mRecentAdapter.setHasStableIds(true);
        mTrendsAdapter.setHasStableIds(true);

        viewModel = new SearchViewModel("", mSavedAdapter, mRecentAdapter, mTrendsAdapter, getContext());

        viewModel.setRecent(SearchData.getInstance().getRecentList().size() > 0);
        binding.setModel(viewModel);

        mActivity = (LoggedActivity) getActivity();
        setupActivity();

        mActivity.binding.txtLoggedSearch.requestFocus();
        mActivity.binding.txtLoggedSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (!mActivity.binding.txtLoggedSearch.getText().toString().equals("")) {
                    mActivity.binding.rlLoggedContainer.requestFocus();
                    AppData.searchQuery = mActivity.binding.txtLoggedSearch.getText().toString();
                    getActivity().startActivity(new Intent(getContext(), SearchActivity.class));
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    Utilities.hideKeyboard(mActivity);
                }
                return false;
            }
        });

        mActivity.binding.txtLoggedSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!mActivity.binding.txtLoggedSearch.getText().toString().equals("")) {
                        mActivity.binding.txtLoggedSearch.setSelection(0, mActivity.binding.txtLoggedSearch.getText().toString().length());
                    }
                }
            }
        });

        binding.svSearch.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (mCallback != null)
                    mCallback.updateBars(scrollY - oldScrollY);
            }
        });

        binding.srlSearch.setOnRefreshListener(refreshListener);
        binding.srlSearch.setProgressViewOffset(false,
                (int) Utilities.convertDpToPixel(80, getContext()),
                (int) Utilities.convertDpToPixel(96, getContext()));
        mActivity.binding.txtLoggedSearch.setText("");

        if (SearchData.getInstance().getRecentList().size() == 0 && SearchData.getInstance().getSavedList().size() == 0
                && SearchData.getInstance().getTrendsList().size() == 0) {
            loadCacheSearch();
            loadData();
        } else {
            viewModel.setSaved(SearchData.getInstance().getSavedList().size() > 0);
            viewModel.setRecent(SearchData.getInstance().getRecentList().size() > 0);
            viewModel.setTrends(SearchData.getInstance().getTrendsList().size() > 0);
        }

        initPopup();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (ActivityListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            mActivity.endSearching(false);
            mActivity.resetBars();
            setupActivity();
            mActivity.binding.txtLoggedSearch.setText("");
            mActivity.binding.txtLoggedSearch.requestFocus();
            mActivity.setStatusBarColor(App.getInstance().isNightEnabled() ?
                    R.color.dark_status_bar_timeline_color : R.color.light_status_bar_timeline_color);
        } else {
            if (binding.srlSearch != null) {
                binding.srlSearch.setRefreshing(false);
                binding.srlSearch.destroyDrawingCache();
                binding.srlSearch.clearAnimation();
            }
        }
    }

    private void setupActivity() {
        mActivity.viewModel.setToolbarState(AppData.TOOLBAR_LOGGED_SEARCH);
        mActivity.viewModel.setStaticBottomBar(true);
        mActivity.viewModel.setStaticToolbar(true);
    }

    public void updateData() {
        initPopup();
        loadSavedSearch();
        Flags.isSearchSaved = false;
    }

    public boolean onBackPressed() {
        mActivity.binding.rlLoggedContainer.requestFocus();
        if (!viewModel.isSearch()) {
            viewModel.setSearch(true);
            mActivity.binding.txtLoggedSearch.setText("");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Load global trends from server
     */
    private void loadGlobalTrends() {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(final TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.srlSearch.setRefreshing(false);
                        viewModel.setLoading(false);
                        Toast.makeText(getContext(), "Error loading trends because " + te.getErrorMessage().toLowerCase(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void gotPlaceTrends(Trends trends) {
                super.gotPlaceTrends(trends);
                SearchData.getInstance().getTrendsList().clear();
                for (int i = 0; i < trends.getTrends().length; i++) {
                    SearchData.getInstance().getTrendsList().add(new SimpleModel(i,
                            trends.getTrends()[i].getName()));
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTrendsAdapter.notifyDataSetChanged();
                        viewModel.setTrends(SearchData.getInstance().getTrendsList().size() != 0);
                        binding.srlSearch.setRefreshing(false);
                        viewModel.setLoading(false);
                    }
                });
            }
        });

        viewModel.setCountry("Global".toUpperCase());
        asyncTwitter.getPlaceTrends(1);
    }

    /**
     * Load local trends from server
     */
    private void loadLocalTrends() {
        final Handler handler = new Handler();
        final AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void gotClosestTrends(ResponseList<Location> locations) {
                super.gotClosestTrends(locations);
                if (locations.size() > 0) {
                    asyncTwitter.getPlaceTrends(locations.get(0).getWoeid());
                }
            }

            @Override
            public void gotPlaceTrends(Trends trends) {
                super.gotPlaceTrends(trends);
                SearchData.getInstance().getTrendsList().clear();
                for (int i = 0; i < trends.getTrends().length; i++) {
                    SearchData.getInstance().getTrendsList().add(new SimpleModel(i,
                            trends.getTrends()[i].getName()));
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTrendsAdapter.notifyDataSetChanged();
                        viewModel.setTrends(SearchData.getInstance().getTrendsList().size() != 0);
                        binding.srlSearch.setRefreshing(false);
                        viewModel.setLoading(false);
                    }
                });
            }

            @Override
            public void onException(final TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        binding.srlSearch.setRefreshing(false);
                        viewModel.setLoading(false);
                        Log.e(TAG, "Error loading saved search - " + te.getErrorMessage());
                        Toast.makeText(getContext(), "Error loading saved search " + te.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mActivity);
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            loadGlobalTrends();
            return;
        }

        mFusedLocationClient.getLastLocation().addOnCompleteListener(mActivity, new OnCompleteListener<android.location.Location>() {
            @Override
            public void onComplete(@NonNull Task<android.location.Location> task) {
                if (task.getResult() != null) {
                    final android.location.Location currentLocation = task.getResult();
                    asyncTwitter.getClosestTrends(new GeoLocation(
                            currentLocation.getLatitude(),
                            currentLocation.getLongitude()));

                    AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
                    asyncTwitter.addListener(new TwitterAdapter() {
                        @Override
                        public void searchedPlaces(final ResponseList<Place> places) {
                            super.searchedPlaces(places);
                            if (places.size() > 0) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        viewModel.setCountry(places.get(0).getName() + ", " +
                                                places.get(0).getCountry());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onException(TwitterException te, TwitterMethod method) {
                            super.onException(te, method);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    viewModel.setCountry("Local".toUpperCase());
                                }
                            });
                        }
                    });

                    asyncTwitter.searchPlaces(new GeoQuery(new GeoLocation(currentLocation.getLatitude(),
                            currentLocation.getLongitude())));
                    asyncTwitter.getClosestTrends(new GeoLocation(currentLocation.getLatitude(),
                            currentLocation.getLongitude()));
                } else {
                    loadGlobalTrends();
                }
            }
        });

    }

    /**
     * Load recent search from memory
     */
    private void loadRecentSearch() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SearchData.getInstance().loadRecentCache();

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mRecentAdapter.notifyDataSetChanged();
                        viewModel.setRecent(SearchData.getInstance().getRecentList().size() > 0);
                    }
                });
            }
        }).start();
    }

    private void loadCacheSearch() {
        Type resultType = new TypeToken<List<SearchModel>>() {
        }.getType();
        Reservoir.getAsync(Cache.SavedSearch, resultType, new ReservoirGetCallback<List<SearchModel>>() {
            @Override
            public void onSuccess(List<SearchModel> searchModels) {
                SearchData.getInstance().getSavedList().clear();
                SearchData.getInstance().getSavedList().addAll(searchModels);
                mSavedAdapter.notifyDataSetChanged();
                viewModel.setSaved(searchModels.size() != 0);
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    private void loadSavedSearch() {
        final Handler handler = new Handler();
        AsyncTwitter asyncTwitter = Utilities.getAsyncTwitter();
        asyncTwitter.addListener(new TwitterAdapter() {
            @Override
            public void onException(final TwitterException te, TwitterMethod method) {
                super.onException(te, method);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "Error loading saved search - " + te.getErrorMessage());
                        Toast.makeText(getContext(), "Error loading saved search " + te.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void gotSavedSearches(ResponseList<SavedSearch> savedSearches) {
                super.gotSavedSearches(savedSearches);
                SearchData.getInstance().getSavedList().clear();

                for (SavedSearch savedSearch : savedSearches)
                    SearchData.getInstance().getSavedList().add(new SearchModel(savedSearch.getId(), 0, savedSearch.getName(), false));

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSavedAdapter.notifyDataSetChanged();
                        viewModel.setSaved(SearchData.getInstance().getSavedList().size() != 0);
                        try {
                            Reservoir.put(Cache.SavedSearch, SearchData.getInstance().getSavedList());
                        } catch (IOException | ConcurrentModificationException e) {
                            Log.e(TAG, "Error saving search");
                        }
                    }
                });
            }
        });
        asyncTwitter.getSavedSearches();
    }

    private void loadData() {
        loadSavedSearch();
        loadRecentSearch();
        if (viewModel.isGlobal()) {
            loadGlobalTrends();
        } else {
            loadLocalTrends();
        }
    }

    public void initPopup() {
        popupMenu = new PopupMenu(mActivity, mActivity.binding.btnLoggedMore, 0, 0, R.style.popup_menu);
        final MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.menu_search_start, popupMenu.getMenu());

        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            popupMenu.getMenu().getItem(2).setVisible(true);
            popupMenu.getMenu().getItem(3).setVisible(false);
        } else {
            popupMenu.getMenu().getItem(2).setVisible(false);
            popupMenu.getMenu().getItem(3).setVisible(true);
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final Twitter twitter = Utilities.getTwitterInstance();
                switch (item.getItemId()) {
                    case R.id.clear_save:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (SearchModel searchModel : SearchData.getInstance().getSavedList()) {
                                    try {
                                        twitter.destroySavedSearch(searchModel.getId());
                                    } catch (TwitterException e) {
                                        Log.e(TAG, "Error deleting search " + e.getLocalizedMessage());
                                    }
                                }
                            }
                        }).start();

                        SearchData.getInstance().getSavedList().clear();
                        mSavedAdapter.notifyDataSetChanged();
                        viewModel.setSaved(false);
                        Toast.makeText(getContext(), getString(R.string.search_cleared), Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.clear_recent:
                        SearchData.getInstance().getRecentList().clear();
                        mRecentAdapter.notifyDataSetChanged();
                        viewModel.setRecent(false);

                        try {
                            Reservoir.put(Cache.RecentSearch + AppData.ME.getId(), SearchData.getInstance().getRecentList());
                        } catch (IOException e) {
                            Log.e(TAG, "Error clear recent " + e.getLocalizedMessage());
                        }
                        break;

                    case R.id.show_global_trends:
                        viewModel.setCountry(getString(R.string.trends_global).toUpperCase());
                        viewModel.setGlobal(true);
                        loadGlobalTrends();

                        popupMenu.getMenu().getItem(2).setVisible(true);
                        item.setVisible(false);
                        break;

                    case R.id.show_local_trends:
                        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, mActivity.REQUEST_LOCATION_CODE);
                        } else {
                            viewModel.setCountry("");
                            viewModel.setGlobal(false);
                            loadLocalTrends();

                            popupMenu.getMenu().getItem(3).setVisible(true);
                            item.setVisible(false);
                        }

                        break;
                }
                return true;
            }
        });

        mActivity.binding.btnLoggedMore.setOnClickListener(moreClickListener);
    }

    private View.OnClickListener moreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            popupMenu.show();
        }
    };

    private SimpleAdapter.SimpleClickListener trendsClickListener = new SimpleAdapter.SimpleClickListener() {
        @Override
        public void onItemClick(SimpleModel model, View v) {
            AppData.searchQuery = model.getTitle();
            getActivity().startActivity(new Intent(getContext(), SearchActivity.class));
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            SearchModel searchModel = new SearchModel(0, 0, model.getTitle(), false);
            if (!SearchData.getInstance().getRecentList().contains(searchModel)) {
                SearchData.getInstance().getRecentList().add(searchModel);
                SearchData.getInstance().saveRecentCache();
            }
        }
    };

    private SearchAdapter.SearchClickListener savedClickListener = new SearchAdapter.SearchClickListener() {
        @Override
        public void onClick(SearchModel model, View v) {
            AppData.searchQuery = model.getTitle();
            getActivity().startActivity(new Intent(getContext(), SearchActivity.class));
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

        @Override
        public void onDelete(final SearchModel model, View v) {
            int position = SearchData.getInstance().getSavedList().indexOf(model);
            SearchData.getInstance().getSavedList().remove(position);
            mSavedAdapter.notifyItemRemoved(position);
            mSavedAdapter.notifyItemRangeChanged(position, mSavedAdapter.getItemCount());

            viewModel.setSaved(SearchData.getInstance().getSavedList().size() > 0);
            binding.recyclerSearchSaved.requestLayout();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Twitter twitterInstance = Utilities.getTwitterInstance();
                    try {
                        twitterInstance.destroySavedSearch(model.getId());
                    } catch (TwitterException e) {
                        Log.e(TAG, "Error deleting search - " + e.getLocalizedMessage());
                    }
                }
            }).start();
        }
    };

    private SearchAdapter.SearchClickListener recentClickListener = new SearchAdapter.SearchClickListener() {
        @Override
        public void onClick(SearchModel model, View v) {
            AppData.searchQuery = model.getTitle();
            getActivity().startActivity(new Intent(getContext(), SearchActivity.class));
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

        @Override
        public void onDelete(SearchModel model, View v) {
            SearchData.getInstance().getRecentList().remove(model);
            mRecentAdapter.notifyDataSetChanged();

            viewModel.setRecent(SearchData.getInstance().getRecentList().size() > 0);
            binding.recyclerSearchRecent.requestLayout();

            SearchData.getInstance().saveRecentCache();
        }
    };
}
