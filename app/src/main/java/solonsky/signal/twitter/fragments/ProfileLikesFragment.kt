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

import java.util.ArrayList

import solonsky.signal.twitter.R
import solonsky.signal.twitter.activities.MVPSearchActivity
import solonsky.signal.twitter.adapters.StatusAdapter
import solonsky.signal.twitter.databinding.FragmentProfileLikesBinding
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Keys
import solonsky.signal.twitter.helpers.TweetActions
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.viewmodels.ProfileLikesViewModel

/**
 * Created by neura on 27.05.17.
 */

class ProfileLikesFragment : Fragment() {
    private val TAG = ProfileLikesFragment::class.java.simpleName
    private var mAdapter: StatusAdapter? = null
    private var likesArray = ArrayList<StatusModel>()
    private lateinit var binding: FragmentProfileLikesBinding
    private var viewModel: ProfileLikesViewModel? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.e(TAG, "likes array ${likesArray.size}")
        binding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_profile_likes, container, false)

        mAdapter = StatusAdapter(likesArray, activity as AppCompatActivity, true,
                true, StatusAdapter.StatusClickListener { searchText, _ ->
            val searchIntent = Intent(context, MVPSearchActivity::class.java)
            searchIntent.putExtra(Keys.SearchQuery.value, searchText)
            activity.startActivity(searchIntent)
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }, TweetActions.MoreCallback { statusModel ->
            val position = likesArray.indexOf(statusModel)
            likesArray.remove(statusModel)
            mAdapter!!.notifyItemRemoved(position)
        })

        viewModel = ProfileLikesViewModel(mAdapter, context)
        viewModel!!.state = if (likesArray.size == 0)
            AppData.UI_STATE_LOADING
        else
            AppData.UI_STATE_VISIBLE

        binding.model = viewModel

        return binding.root
    }

    fun setupLikes(likesArray: ArrayList<StatusModel>) {
        this.likesArray.clear()
        this.likesArray.addAll(likesArray)

        mAdapter?.notifyDataSetChanged()

        viewModel?.state = if (likesArray.size == 0)
            AppData.UI_STATE_NO_ITEMS
        else
            AppData.UI_STATE_VISIBLE
    }
}
