package solonsky.signal.twitter.views

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import solonsky.signal.twitter.models.User
import solonsky.signal.twitter.models.UserModel

/**
 * Created by sunwi on 23.01.2018.
 */
@StateStrategyType(value = AddToEndSingleStrategy::class)
interface ComposeView: MvpView {
    fun setupMentions(data: MutableList<UserModel>)
}