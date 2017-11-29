package solonsky.signal.twitter.views

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import solonsky.signal.twitter.helpers.Flags
import solonsky.signal.twitter.models.ImageModel
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.models.User
import java.text.FieldPosition
import java.util.*

/**
 * Created by neura on 03.11.17.
 */

@StateStrategyType(value = AddToEndSingleStrategy::class, tag = "StatusView")
interface StatusView: MvpView {
    fun showToast(text: String)
    fun showToast(text: Int)
    fun showDialog(type: Flags.Dialogs, title: String, isVideo: Boolean)

    fun setClipboard(text: String)

    fun openProfile(screenName: String)
    fun openProfile(user: User)
    fun openDetail()
    fun openCompose()
    fun openMedia()
    fun openSearch()
    fun openImages(urls: ArrayList<String>, startPosition: Int)
    fun openYoutube(url: String)
    fun openLink(link: String)

    fun setupBigMedia(imageModels: ArrayList<ImageModel>)
    fun setupSmallMedia(type: String, imageUrl: String)
    fun setupNoMedia()

    fun updateArrow(dX: Float, diff: Float)

    fun shareText(text: String)
    fun shareTextWithApp(text: String, packageName: String, packageActivity: String)

    fun translate(text: String)
    fun makeRt(statusModel: StatusModel?)

    fun changeActions(isExpand: Boolean, isHighlighted: Boolean)
    fun changeLike(isLike: Boolean)
}