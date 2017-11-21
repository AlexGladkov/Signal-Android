package solonsky.signal.twitter.data;

import android.util.Log;

import com.anupcowkur.reservoir.Reservoir;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Cache;
import solonsky.signal.twitter.models.SearchModel;
import solonsky.signal.twitter.models.SimpleModel;

/**
 * Created by neura on 10.09.17.
 */

public class SearchData {
    private static volatile SearchData instance;
    private String TAG = SearchData.class.getSimpleName();

    public static SearchData getInstance() {
        SearchData localInstance = instance;
        if (localInstance == null) {
            synchronized (SearchData.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new SearchData();
                }
            }
        }
        return localInstance;
    }

    private ArrayList<SearchModel> savedList;
    private ArrayList<SearchModel> recentList;
    private ArrayList<SimpleModel> trendsList;
    private boolean isRecentLoaded = false;

    public void saveRecentCache() {
        try {
            Reservoir.put(Cache.RecentSearch + AppData.ME.getId(), recentList);
        } catch (Exception e) {
            //Do nothing
        }
    }

    public void loadRecentCache() {
        try {
            Type resultType = new TypeToken<List<SearchModel>>() {
            }.getType();
            recentList.clear();
            List<SearchModel> loadedList = Reservoir.get(Cache.RecentSearch + AppData.ME.getId(), resultType);
            recentList.addAll(loadedList);
            isRecentLoaded = true;
        } catch (Exception e) {
            //Do nothing
        }
    }

    private SearchData() {
        this.savedList = new ArrayList<>();
        this.recentList = new ArrayList<>();
        this.trendsList = new ArrayList<>();
    }

    public void clear() {
        this.savedList.clear();
        this.recentList.clear();
        this.trendsList.clear();
    }

    public ArrayList<SearchModel> getSavedList() {
        return savedList;
    }

    public ArrayList<SearchModel> getRecentList() {
        return recentList;
    }

    public ArrayList<SimpleModel> getTrendsList() {
        return trendsList;
    }

    public static void setInstance(SearchData instance) {
        SearchData.instance = instance;
    }

    public boolean isRecentLoaded() {
        return isRecentLoaded;
    }

    public void setRecentLoaded(boolean recentLoaded) {
        isRecentLoaded = recentLoaded;
    }
}
