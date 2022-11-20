package solonsky.signal.twitter.views

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType


/**
 * Created by agladkov on 07.02.18.
 */
@StateStrategyType(value = AddToEndSingleStrategy::class)
interface LoginView: MvpView {
    fun performLogged()
}