package solonsky.signal.twitter.views

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

/**
 * Created by sunwi on 22.01.2018.
 */
@StateStrategyType(value = AddToEndSingleStrategy::class)
interface LoggedView: MvpView {
    fun showMessage(text: String)
}