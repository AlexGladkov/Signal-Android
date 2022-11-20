package solonsky.signal.twitter.viewmodels;

import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 28.05.17.
 */

public class ProfileStatsViewModel extends BaseObservable {
    private String currentFollowers;
    private String currentFollowing;
    private String currentTweets;
    private String currentListed;
    private int differenceFollowers;
    private int differenceFollowing;
    private int differenceTweets;
    private int differenceListed;

    public interface ProfileStatsClickHandler {
        void onFollowersClick(View view);
        void onFollowingClick(View view);
        void onTweetsClick(View view);
        void onListedClick(View view);
    }

    public ProfileStatsViewModel(String currentFollowers, String currentFollowing, String currentTweets, String currentListed,
                                 int differenceFollowers, int differenceFollowing, int differenceTweets, int differenceListed) {
        this.currentFollowers = currentFollowers;
        this.currentFollowing = currentFollowing;
        this.currentTweets = currentTweets;
        this.currentListed = currentListed;
        this.differenceFollowers = differenceFollowers;
        this.differenceFollowing = differenceFollowing;
        this.differenceTweets = differenceTweets;
        this.differenceListed = differenceListed;
    }

    @Bindable
    public String getCurrentFollowers() {
        return currentFollowers;
    }

    public void setCurrentFollowers(String currentFollowers) {
        this.currentFollowers = currentFollowers;
        notifyPropertyChanged(BR.currentFollowers);
    }

    @Bindable
    public String getCurrentFollowing() {
        return currentFollowing;
    }

    public void setCurrentFollowing(String currentFollowing) {
        this.currentFollowing = currentFollowing;
        notifyPropertyChanged(BR.currentFollowing);
    }

    @Bindable
    public String getCurrentTweets() {
        return currentTweets;
    }

    public void setCurrentTweets(String currentTweets) {
        this.currentTweets = currentTweets;
        notifyPropertyChanged(BR.currentTweets);
    }

    @Bindable
    public String getCurrentListed() {
        return currentListed;
    }

    public void setCurrentListed(String currentListed) {
        this.currentListed = currentListed;
        notifyPropertyChanged(BR.currentListed);
    }

    @Bindable
    public int getDifferenceFollowers() {
        return differenceFollowers;
    }

    public void setDifferenceFollowers(int differenceFollowers) {
        this.differenceFollowers = differenceFollowers;
        notifyPropertyChanged(BR.differenceFollowers);
    }

    @Bindable
    public int getDifferenceFollowing() {
        return differenceFollowing;
    }

    public void setDifferenceFollowing(int differenceFollowing) {
        this.differenceFollowing = differenceFollowing;
        notifyPropertyChanged(BR.differenceFollowing);
    }

    @Bindable
    public int getDifferenceTweets() {
        return differenceTweets;
    }

    public void setDifferenceTweets(int differenceTweets) {
        this.differenceTweets = differenceTweets;
        notifyPropertyChanged(BR.differenceTweets);
    }

    @Bindable
    public int getDifferenceListed() {
        return differenceListed;
    }

    public void setDifferenceListed(int differenceListed) {
        this.differenceListed = differenceListed;
        notifyPropertyChanged(BR.differenceListed);
    }
}
