package solonsky.signal.twitter.views

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import solonsky.signal.twitter.models.ImageModel
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.models.User
import java.util.*

/**
 * Created by sunwi on 22.11.2017.
 */

@StateStrategyType(value = AddToEndSingleStrategy::class)
interface ProfileView: MvpView {
    fun performBack(startAnim: Int, endAnim: Int)
    fun showError(errorMessage: Int)
    fun setupUser(user: User, homeUser: Boolean)
    fun setupTweets(tweetsArray: ArrayList<StatusModel>)
    fun setupMedia(mediaArray: ArrayList<ImageModel>)
    fun setupLikes(likesArray: ArrayList<StatusModel>)
    fun updateAvatar(verified: Int, avatar: Int, progress: Float)
    fun updateTab(diff: Int)
    fun updateTitle(scrollY: Int)
    fun updateHeader(diff: Int, isAnimated: Boolean)
    fun showImage(urls: ArrayList<String>, startPosition: Int)
}