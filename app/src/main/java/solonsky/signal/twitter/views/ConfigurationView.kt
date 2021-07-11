package solonsky.signal.twitter.views

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType


/**
 * Created by neura on 10.02.18.
 */
@StateStrategyType(value = AddToEndSingleStrategy::class)
interface ConfigurationView: MvpView {
    fun settingsUpdated()
}