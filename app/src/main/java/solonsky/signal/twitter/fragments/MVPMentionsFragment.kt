package solonsky.signal.twitter.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_mentions.*
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import solonsky.signal.twitter.R
import solonsky.signal.twitter.adapters.MVPStatusAdapter
import solonsky.signal.twitter.data.MentionsData
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.interfaces.ActivityListener
import solonsky.signal.twitter.interfaces.UpdateAddHandler
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.presenters.MentionsPresenter
import solonsky.signal.twitter.views.MentionsView
import java.util.*

/**
 * Created by neura on 03.11.17.
 */

class MVPMentionsFragment : MvpAppCompatFragment(), MentionsView {
    companion object {
        const val TAG = "MVPMentionsFragment"

        fun newInstance(): MVPMentionsFragment {
            val fragment = MVPMentionsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    @InjectPresenter
    lateinit var mMentionsPresenter: MentionsPresenter
    private lateinit var mCallback: ActivityListener
    private var mAdapter: MVPStatusAdapter = MVPStatusAdapter(mvpDelegate)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_mentions, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mMentionsPresenter.oldLoadData()
        mMentionsPresenter.isHiddenChanged(false)

        mAdapter.attachActivity(requireActivity())
        recycler_mentions.adapter = mAdapter
        recycler_mentions.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recycler_mentions.setHasFixedSize(true)

        recycler_mentions.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                mMentionsPresenter.onScrolled(recyclerView, dx, dy)
            }
        })

        srl_mentions_main.setOnRefreshListener {
            mMentionsPresenter.refreshData()
        }
        srl_mentions_main.setProgressViewOffset(false,
                Utilities.convertDpToPixel(80f, context).toInt(),
                Utilities.convertDpToPixel(96f, context).toInt())

        MentionsData.instance.updateHandler = object : UpdateAddHandler {
            override fun onUpdate() {

            }

            override fun onAdd() {
                mMentionsPresenter.addNew()
            }

            override fun onError() {

            }

            override fun onDelete(position: Int) {

            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mCallback = context as ActivityListener
        } catch (e: ClassCastException) {
            //Do nothing
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        mMentionsPresenter.isHiddenChanged(hidden)
    }

    override fun onScrollToTop() {
        if (isAdded) {
            recycler_mentions.scrollToPosition(0)
        }
    }

    override fun onSmoothScrollToTop() {
        if (isAdded) {
            recycler_mentions.smoothScrollToPosition(0)
        }
    }

    override fun onAnimatedScrollToTop(position: Int) {
        if (isAdded) {
            val layoutManager = recycler_mentions.layoutManager as LinearLayoutManager
            val currentPosition = layoutManager.findFirstVisibleItemPosition()
            if (currentPosition > position) {
                recycler_mentions.scrollToPosition(0)
                recycler_mentions.smoothScrollToPosition(0)
            } else {
                recycler_mentions.smoothScrollToPosition(0)
            }
        }
    }

    override fun setupEmptyView() {
        if (isAdded) {
            recycler_mentions.visibility = View.GONE
            txt_mentions_no_items.visibility = View.VISIBLE
        }
    }

    override fun setupData(statusModels: ArrayList<StatusModel>) {
        if (isAdded) {
            mAdapter.setStatuses(statusModels)
            recycler_mentions.visibility = View.VISIBLE
            txt_mentions_no_items.visibility = View.GONE
        }
    }

    override fun startLoading() {
        if (isAdded) {
            recycler_mentions.visibility = View.GONE
            txt_mentions_no_items.visibility = View.GONE
            avl_mentions.visibility = View.VISIBLE
        }
    }

    override fun endLoading() {
        if (isAdded) {
            avl_mentions.visibility = View.GONE
        }
    }

    override fun showToast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    override fun loadingMore(isLoaded: Boolean) {
        if (isLoaded) {
            MentionsData.instance.updateHandler?.onUpdate()
            mAdapter.removeLoader()
            mAdapter.notifyDataSetChanged()
        } else {
            mAdapter.addLoader()
        }
    }

    override fun refreshed() {
        srl_mentions_main.isRefreshing = false
        mCallback.updateCounter(0)
        mAdapter.changeHighlight(isOn = false)
    }

    override fun addNew(statusModels: ArrayList<StatusModel>, newCount: Int) {
        mAdapter.addNewStatuses(statusModels)
        mCallback.updateCounter(newCount)
    }

    override fun updateHost(title: Int, newCount: Int) {
        mCallback.updateCounter(newCount)
        mCallback.updateTitle(title)
        mCallback.updateToolbarState(AppData.TOOLBAR_LOGGED_MAIN, if (App.getInstance().isNightEnabled)
            R.color.dark_status_bar_timeline_color
        else
            R.color.light_status_bar_timeline_color)
    }

    override fun updateBars(dy: Int) {
        mCallback.updateBars(dy)
    }

    override fun updateHighlight(isHighlight: Boolean, position: Int) {
        mAdapter.updateStatus(isHighlight = isHighlight, position = position)
    }
}