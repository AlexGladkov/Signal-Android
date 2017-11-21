package solonsky.signal.twitter.data;

import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirGetCallback;
import com.anupcowkur.reservoir.ReservoirPutCallback;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import solonsky.signal.twitter.helpers.Cache;

/**
 * Created by neura on 21.09.17.
 */

public class ShareData {
    private static final String TAG = ShareData.class.getSimpleName();
    private static volatile ShareData instance;

    public static ShareData getInstance() {
        ShareData localInstance = instance;
        if (localInstance == null) {
            synchronized (ShareData.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ShareData();
                }
            }
        }
        return localInstance;
    }

    private List<String> shares;
    private boolean isCacheLoaded = false;

    private ShareData() {
        shares = new ArrayList<>();
    }

    public void addShare(String packageName) {
        if (!shares.contains(packageName)) {
            shares.add(0, packageName);
            shares = shares.subList(0, shares.size() > 3 ? 3 : shares.size());
        }
    }

    public void loadCache() {
        isCacheLoaded = true;
        Type resultType = new TypeToken<List<String>>() {
        }.getType();
        try {
            List<String> domains = Reservoir.get(Cache.Shares, resultType);
            for (String packageName : domains) {
                if (!shares.contains(packageName))
                    shares.add(packageName);
            }
        } catch (IOException | NullPointerException e) {
            //Do nothing
        }
    }

    public void saveCache() {
        Reservoir.putAsync(Cache.Shares, shares, new ReservoirPutCallback() {
            @Override
            public void onSuccess() {
                //Do nothing
            }

            @Override
            public void onFailure(Exception e) {
                //Do nothing
            }
        });
    }

    public List<String> getShares() {
        return shares;
    }

    public void setShares(List<String> shares) {
        this.shares = shares;
    }

    public boolean isCacheLoaded() {
        return isCacheLoaded;
    }

    public void setCacheLoaded(boolean cacheLoaded) {
        isCacheLoaded = cacheLoaded;
    }


}
