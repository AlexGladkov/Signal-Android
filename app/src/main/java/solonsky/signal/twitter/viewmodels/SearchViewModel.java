package solonsky.signal.twitter.viewmodels;

import android.content.Context;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.adapters.SearchAdapter;
import solonsky.signal.twitter.adapters.SimpleAdapter;
import solonsky.signal.twitter.helpers.ListConfig;

/**
 * Created by neura on 26.05.17.
 */

public class SearchViewModel extends BaseObservable {
    private String country;
    private ListConfig savedConfig;
    private ListConfig recentConfig;
    private ListConfig trendsConfig;
    private boolean isSaved;
    private boolean isRecent;
    private boolean isTrends;
    private boolean isSearch;
    private boolean isLoading;
    private boolean isGlobal;

    public SearchViewModel(String country, SearchAdapter savedAdapter, SearchAdapter recentAdapter,
                           SimpleAdapter trendsAdapter, Context context) {
        this.country = country;
        this.isSearch = false;

        this.isSaved = false;
        this.isRecent = false;
        this.isTrends = false;
        this.isLoading = false;
        this.isGlobal = false;

        this.savedConfig = new ListConfig.Builder(savedAdapter)
                .setDefaultDividerEnabled(true)
                .setHasFixedSize(true)
                .setHasNestedScroll(false)
                .setLayoutManagerProvider(new ListConfig.SimpleLinearLayoutManagerProvider())
                .build(context);

        this.recentConfig = new ListConfig.Builder(recentAdapter)
                .setDefaultDividerEnabled(true)
                .setHasFixedSize(true)
                .setHasNestedScroll(false)
                .setLayoutManagerProvider(new ListConfig.SimpleLinearLayoutManagerProvider())
                .build(context);

        this.trendsConfig = new ListConfig.Builder(trendsAdapter)
                .setDefaultDividerEnabled(true)
                .setHasFixedSize(true)
                .setHasNestedScroll(false)
                .setLayoutManagerProvider(new ListConfig.SimpleLinearLayoutManagerProvider())
                .build(context);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }

    @Bindable
    public boolean isSearch() {
        return isSearch;
    }
    public void setSearch(boolean search) {
        isSearch = search;
        notifyPropertyChanged(BR.search);
    }

    @Bindable
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
        notifyPropertyChanged(BR.country);
    }

    @Bindable
    public ListConfig getSavedConfig() {
        return savedConfig;
    }
    public void setSavedConfig(ListConfig savedConfig) {
        this.savedConfig = savedConfig;
        notifyPropertyChanged(BR.savedConfig);
    }

    @Bindable
    public ListConfig getRecentConfig() {
        return recentConfig;
    }
    public void setRecentConfig(ListConfig recentConfig) {
        this.recentConfig = recentConfig;
        notifyPropertyChanged(BR.recentConfig);
    }

    @Bindable
    public ListConfig getTrendsConfig() {
        return trendsConfig;
    }
    public void setTrendsConfig(ListConfig trendsConfig) {
        this.trendsConfig = trendsConfig;
        notifyPropertyChanged(BR.trendsConfig);
    }

    @Bindable
    public boolean isSaved() {
        return isSaved;
    }
    public void setSaved(boolean saved) {
        isSaved = saved;
        notifyPropertyChanged(BR.saved);
    }

    @Bindable
    public boolean isRecent() {
        return isRecent;
    }
    public void setRecent(boolean recent) {
        isRecent = recent;
        notifyPropertyChanged(BR.recent);
    }

    @Bindable
    public boolean isTrends() {
        return isTrends;
    }
    public void setTrends(boolean trends) {
        isTrends = trends;
        notifyPropertyChanged(BR.trends);
    }


}
