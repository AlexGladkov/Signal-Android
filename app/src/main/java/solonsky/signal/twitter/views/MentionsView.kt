package solonsky.signal.twitter.views

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import solonsky.signal.twitter.models.StatusModel
import java.util.*

/**
 * Created by neura on 03.11.17.
 */

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface MentionsView: MvpView {
    fun onScrollToTop()
    fun onSmoothScrollToTop()
    fun onAnimatedScrollToTop(position: Int)
    fun setupEmptyView()
    fun setupData(statusModels: ArrayList<StatusModel>)
    fun startLoading()
    fun endLoading()
    fun showToast(text: String)
    fun loadingMore(isLoaded: Boolean)
    fun refreshed()
    fun addNew(statusModels: ArrayList<StatusModel>, newCount: Int)
    fun updateHost(title: Int, newCount: Int)
    fun updateBars(dy: Int)
    fun updateHighlight(isHighlight: Boolean, position: Int)
}