package solonsky.signal.twitter.views

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

/**
 * Created by neura on 01.11.17.
 */

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface SearchView: MvpView {
    fun updateStatusBarColor(color: Int)
    fun showFragment()
    fun setupSearch(query: String)
}