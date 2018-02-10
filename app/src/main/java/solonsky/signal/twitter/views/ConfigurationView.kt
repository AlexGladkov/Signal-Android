package solonsky.signal.twitter.views

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

/**
 * Created by neura on 10.02.18.
 */
@StateStrategyType(value = AddToEndSingleStrategy::class)
interface ConfigurationView: MvpView {
    fun settingsUpdated()
}