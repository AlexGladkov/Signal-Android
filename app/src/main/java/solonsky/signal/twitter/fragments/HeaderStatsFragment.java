package solonsky.signal.twitter.fragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

import solonsky.signal.twitter.R;
import solonsky.signal.twitter.activities.ProfileActivity;
import solonsky.signal.twitter.activities.StatsFollowersActivity;
import solonsky.signal.twitter.activities.StatsFollowingActivity;
import solonsky.signal.twitter.activities.StatsListedActivity;
import solonsky.signal.twitter.activities.StatsTweetsActivity;
import solonsky.signal.twitter.databinding.FragmentProfileStatsBinding;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Utilities;
import solonsky.signal.twitter.interfaces.HeaderListener;
import solonsky.signal.twitter.interfaces.ProfileListener;
import solonsky.signal.twitter.models.User;
import solonsky.signal.twitter.viewmodels.ProfileStatsViewModel;

/**
 * Created by neura on 27.05.17.
 */

public class HeaderStatsFragment extends Fragment {
    private ProfileStatsViewModel viewModel;
    private FragmentProfileStatsBinding binding;
    private ProfileListener profileListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_stats, container, false);
        viewModel = new ProfileStatsViewModel("", "", "", "", 0, 0, 0, 0);

        binding.setModel(viewModel);
        binding.setClick(new ProfileStatsViewModel.ProfileStatsClickHandler() {
            @Override
            public void onFollowersClick(View view) {
                if (profileListener != null)
                    profileListener.openActivity(new Intent(getContext(), StatsFollowersActivity.class));
            }

            @Override
            public void onFollowingClick(View view) {
                if (profileListener != null)
                    profileListener.openActivity(new Intent(getContext(), StatsFollowingActivity.class));

            }

            @Override
            public void onTweetsClick(View view) {
                if (profileListener != null)
                    profileListener.openActivity(new Intent(getContext(), StatsTweetsActivity.class));
            }

            @Override
            public void onListedClick(View view) {
                if (profileListener != null)
                    profileListener.openActivity(new Intent(getContext(), StatsListedActivity.class));
            }
        });

        changeProfileHeight(false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (profileListener != null)
            profileListener.updateStats();
    }

    /**
     * Setup view models
     *
     * @param currentFollowers    - user's followers count
     * @param currentFollowing    - user's following count
     * @param currentTweets       - user's tweets count
     * @param currentListed       - user's listed count
     * @param differenceFollowers - user's followers difference
     * @param differenceFollowing - user's following difference
     * @param differenceTweets    - user's following tweets
     * @param differenceListed    - user's following listed
     */
    public void setViewModel(long currentFollowers, long currentFollowing, long currentTweets, long currentListed,
                             int differenceFollowers, int differenceFollowing, int differenceTweets, int differenceListed) {
        viewModel = new ProfileStatsViewModel(
                Utilities.parseFollowers(currentFollowers, ""),
                Utilities.parseFollowers(currentFollowing, ""),
                Utilities.parseFollowers(currentTweets, ""),
                Utilities.parseFollowers(currentListed, ""),
                differenceFollowers, differenceFollowing,
                differenceTweets, differenceListed);
        binding.setModel(viewModel);
    }

    /**
     * Change height for fader in activity
     *
     * @param isAnimated - @true for animated
     */
    public void changeProfileHeight(boolean isAnimated) {
        if (profileListener != null)
            profileListener.updateHeader(0, isAnimated);
    }

    public void setProfileListener(ProfileListener profileListener) {
        this.profileListener = profileListener;
    }
}
