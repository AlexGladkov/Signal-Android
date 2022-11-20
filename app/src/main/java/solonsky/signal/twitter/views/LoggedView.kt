package solonsky.signal.twitter.views

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType


/**
 * Created by sunwi on 22.01.2018.
 */
@StateStrategyType(value = AddToEndSingleStrategy::class)
interface LoggedView: MvpView {
    fun showMessage(text: String)
    fun setupProfile(avatar: String?)
    fun updateLocale(newLocale: String)
    fun onResumeFragments()
}