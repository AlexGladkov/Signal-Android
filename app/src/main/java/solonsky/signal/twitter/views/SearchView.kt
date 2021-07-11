package solonsky.signal.twitter.views

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType


/**
 * Created by neura on 01.11.17.
 */

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface SearchView: MvpView {
    fun updateStatusBarColor(color: Int)
    fun showFragment()
    fun setupSearch(query: String)
    fun showMessage(text: String)
    fun performExit()
    fun openPopup(resource: Int)
}