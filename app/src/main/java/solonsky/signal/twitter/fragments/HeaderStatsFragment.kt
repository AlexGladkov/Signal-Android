package solonsky.signal.twitter.fragments

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation
import android.widget.RelativeLayout

import solonsky.signal.twitter.R
import solonsky.signal.twitter.activities.ProfileActivity
import solonsky.signal.twitter.activities.StatsFollowersActivity
import solonsky.signal.twitter.activities.StatsFollowingActivity
import solonsky.signal.twitter.activities.StatsListedActivity
import solonsky.signal.twitter.activities.StatsTweetsActivity
import solonsky.signal.twitter.databinding.FragmentProfileStatsBinding
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.interfaces.HeaderListener
import solonsky.signal.twitter.interfaces.ProfileListener
import solonsky.signal.twitter.models.User
import solonsky.signal.twitter.viewmodels.ProfileStatsViewModel
import solonsky.signal.twitter.views.ProfileView

/**
 * Created by neura on 27.05.17.
 */

class HeaderStatsFragment : Fragment() {
    private var viewModel: ProfileStatsViewModel? = null
    private var binding: FragmentProfileStatsBinding? = null
    private var profileListener: ProfileView? = null
    private val TAG: String = HeaderStatsFragment::class.java.simpleName

    companion object {
        fun newInstance(user: User): HeaderStatsFragment {
            val fragment = HeaderStatsFragment()
            val args = Bundle()
            args.putParcelable(User.TAG, user)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_profile_stats, container, false)

        if (arguments != null && arguments.get(User.TAG) != null) {
            val user = arguments.get(User.TAG) as User
            viewModel = ProfileStatsViewModel(Utilities.parseFollowers(user.followersCount, ""),
                    Utilities.parseFollowers(user.friendsCount, ""),
                    Utilities.parseFollowers(user.statusesCount, ""),
                    Utilities.parseFollowers(user.listedCount, ""),
                    0, 0,
                    0, 0)

            binding!!.model = viewModel
            binding!!.click = object : ProfileStatsViewModel.ProfileStatsClickHandler {
                override fun onFollowersClick(view: View) {
                    activity.startActivity(Intent(context, StatsFollowersActivity::class.java))
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                override fun onFollowingClick(view: View) {
                    activity.startActivity(Intent(context, StatsFollowingActivity::class.java))
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                override fun onTweetsClick(view: View) {
                    activity.startActivity(Intent(context, StatsTweetsActivity::class.java))
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                override fun onListedClick(view: View) {
                    activity.startActivity(Intent(context, StatsListedActivity::class.java))
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            }

            changeProfileHeight(false)
        }

        return binding!!.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        if (profileListener != null)
//            profileListener!!.updateStats()
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
    fun setViewModel(currentFollowers: Long, currentFollowing: Long, currentTweets: Long, currentListed: Long,
                     differenceFollowers: Int, differenceFollowing: Int, differenceTweets: Int, differenceListed: Int) {
        viewModel = ProfileStatsViewModel(
                Utilities.parseFollowers(currentFollowers, ""),
                Utilities.parseFollowers(currentFollowing, ""),
                Utilities.parseFollowers(currentTweets, ""),
                Utilities.parseFollowers(currentListed, ""),
                differenceFollowers, differenceFollowing,
                differenceTweets, differenceListed)
        binding!!.model = viewModel
    }

    /**
     * Change height for fader in activity
     *
     * @param isAnimated - @true for animated
     */
    fun changeProfileHeight(isAnimated: Boolean) {
        profileListener?.updateHeader(0, isAnimated)
    }

    fun setProfileListener(profileListener: ProfileView) {
        this.profileListener = profileListener
    }
}
