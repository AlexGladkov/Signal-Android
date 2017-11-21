package solonsky.signal.twitter.presenters

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.google.gson.Gson
import solonsky.signal.twitter.R
import solonsky.signal.twitter.data.LoggedData
import solonsky.signal.twitter.data.MentionsData
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.providers.MentionsProvider
import solonsky.signal.twitter.views.MentionsView
import java.util.*

/**
 * Created by neura on 03.11.17.
 */

@InjectViewState
class MentionsPresenter : MvpPresenter<MentionsView>() {
    val gson = Gson()
    var isLoading = false
    private val TAG: String = MentionsPresenter::class.simpleName.toString()
    private val mProvider = MentionsProvider(presenter = this)

    fun oldLoadData() {
        viewState.startLoading()
        mProvider.getData()
    }

    fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
        val hiddenPosition = linearLayoutManager.findFirstVisibleItemPosition()

        viewState.updateBars(dy)

        if (hiddenPosition > 0) {
            if (MentionsData.instance.mentionsStatuses[hiddenPosition - 1].isHighlighted) {
                MentionsData.instance.mentionsStatuses[hiddenPosition - 1].isHighlighted = false
                MentionsData.instance.decEntryCount()
                highlightCheck(mentionsStatuses = MentionsData.instance.mentionsStatuses)
                viewState.updateHighlight(isHighlight = false, position = hiddenPosition - 1)
            }
        }

        if (dy > 0) {
            val visibleItemCount = recyclerView.layoutManager.childCount
            val totalItemCount = recyclerView.layoutManager.itemCount
            val pastVisibleItems = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

            if (!isLoading) {
                if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                    isLoading = true
                    viewState.loadingMore(isLoaded = false)
                    MentionsProvider(presenter = this).oldLoadMore()
                }
            }
        }
    }

    fun loadedMore() {
        isLoading = false
        viewState.loadingMore(isLoaded = true)
    }

    fun showException(errorMessage: String?) {
        isLoading = false
        if (errorMessage != null)
            viewState.showToast(errorMessage)
    }

    fun loaded(mentionsStatuses: ArrayList<StatusModel>) {
        viewState.endLoading()
        viewState.setupData(mentionsStatuses)
        highlightCheck(mentionsStatuses = mentionsStatuses)
    }

    private fun highlightCheck(mentionsStatuses: ArrayList<StatusModel>) {
        var entryCount = 0
        mentionsStatuses.forEach {
            if (it.isHighlighted) {
                entryCount +=1
            }
        }

        LoggedData.getInstance().isNewMention = entryCount > 0
        LoggedData.getInstance().updateHandler.onUpdate()

        viewState.updateHost(title = R.string.title_mentions, newCount = entryCount)
    }

    fun refreshData() {
        viewState.refreshed()
        LoggedData.getInstance().isNewMention = false
        LoggedData.getInstance().updateHandler.onUpdate()
    }

    fun addNew() {
        val newItems = MentionsData.instance.newStatuses
        newItems.reverse()
        newItems.forEach {
            MentionsData.instance.mentionsStatuses.add(0, it)
        }

        var newCount = 0
        MentionsData.instance.mentionsStatuses.forEach {
            if (it.isHighlighted) {
                newCount += 1
            }
        }

        viewState.addNew(newItems, newCount)
        MentionsData.instance.saveCache(TAG)
        MentionsData.instance.newStatuses.clear()
    }

    fun isHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            var newCount = 0
            MentionsData.instance.mentionsStatuses.forEach {
                if (it.isHighlighted) {
                    newCount += 1
                }
            }

            viewState.updateHost(R.string.title_mentions, newCount)
        } else {
            MentionsData.instance.saveCache(TAG)
        }
    }
}