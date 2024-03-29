package solonsky.signal.twitter.fragments

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

import solonsky.signal.twitter.R
import solonsky.signal.twitter.activities.MVPSearchActivity
import solonsky.signal.twitter.adapters.StatusAdapter
import solonsky.signal.twitter.api.ProfileDataApi
import solonsky.signal.twitter.databinding.FragmentProfileTweetsBinding
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Keys
import solonsky.signal.twitter.helpers.TweetActions
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.viewmodels.ProfileTweetsViewModel

/**
 * Created by neura on 27.05.17.
 */

class ProfileTweetsFragment : Fragment() {
    private val TAG = ProfileTweetsFragment::class.java.simpleName
    private lateinit var viewModel: ProfileTweetsViewModel
    private lateinit var mAdapter: StatusAdapter
    private lateinit var binding: FragmentProfileTweetsBinding
    private val tweetsArray = ArrayList<StatusModel>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_profile_tweets, container, false)
        mAdapter = StatusAdapter(tweetsArray, activity as AppCompatActivity, true,
                true, StatusAdapter.StatusClickListener { searchText, v ->
            val searchIntent = Intent(context, MVPSearchActivity::class.java)
            searchIntent.putExtra(Keys.SearchQuery.value, searchText)
            activity.startActivity(searchIntent)
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }, TweetActions.MoreCallback { statusModel ->
            val position = ProfileDataApi.getInstance().tweets.indexOf(statusModel)
            ProfileDataApi.getInstance().tweets.remove(statusModel)
            mAdapter.notifyItemRemoved(position)
        })

        viewModel = ProfileTweetsViewModel(mAdapter, context)
        viewModel.state = if (tweetsArray.size == 0)
            AppData.UI_STATE_LOADING
        else
            AppData.UI_STATE_VISIBLE
        binding.model = viewModel

        return binding.root
    }

    fun setupTweets(newTweetsArray: ArrayList<StatusModel>) {
        tweetsArray.addAll(newTweetsArray)
        mAdapter.notifyDataSetChanged()

        viewModel.state = if (tweetsArray.size == 0)
            AppData.UI_STATE_NO_ITEMS
        else
            AppData.UI_STATE_VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.profileBottomMore.setOnClickListener(null)
    }
}
