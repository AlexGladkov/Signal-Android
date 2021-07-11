package solonsky.signal.twitter.views

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType


/**
 * Created by agladkov on 01.02.18.
 */
@StateStrategyType(value = AddToEndSingleStrategy::class)
interface SplashView: MvpView {
    fun performLogin()
    fun performLogged()
    fun setupSettings()
    fun setupLocale(newLocale: String)
}