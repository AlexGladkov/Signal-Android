package solonsky.signal.twitter.views

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import solonsky.signal.twitter.models.User
import solonsky.signal.twitter.models.UserModel

/**
 * Created by sunwi on 23.01.2018.
 */
@StateStrategyType(value = AddToEndSingleStrategy::class)
interface ComposeView: MvpView {
    fun setupMentions(data: List<UserModel>)
}