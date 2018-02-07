package solonsky.signal.twitter.views

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

/**
 * Created by agladkov on 07.02.18.
 */
@StateStrategyType(value = AddToEndSingleStrategy::class)
interface LoginView: MvpView {
    fun performLogged()
}