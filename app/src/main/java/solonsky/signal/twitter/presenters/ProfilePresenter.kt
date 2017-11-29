package solonsky.signal.twitter.presenters

import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.Log
import android.view.View
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import solonsky.signal.twitter.R
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Flags
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.libs.DownloadFiles
import solonsky.signal.twitter.models.ImageModel
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.models.User
import solonsky.signal.twitter.overlays.ImageOverlay
import solonsky.signal.twitter.providers.ProfileProvider
import solonsky.signal.twitter.views.ProfileView
import java.util.*

/**
 * Created by sunwi on 22.11.2017.
 */

@InjectViewState
class ProfilePresenter : MvpPresenter<ProfileView>() {
    private val TAG: String = ProfilePresenter::class.simpleName.toString()
    private var user: User? = null
    private var tweetsArray = ArrayList<StatusModel>()

    fun getUser(user: User) {
        val provider = ProfileProvider(presenter = this)
        setupUser(user = user)
        provider.loadTweets(id = user.id)
        provider.loadFavorites(id = user.id)
    }

    fun getUser(intent: Intent?) {
        val provider = ProfileProvider(presenter = this)
        when {
            intent == null || intent.extras == null -> performError()
            intent.extras.get(Flags.PROFILE_DATA) != null -> {
                val extrasUser = intent.extras.get(Flags.PROFILE_DATA) as User
                setupUser(user = extrasUser)
                provider.loadTweets(id = extrasUser.id)
                provider.loadFavorites(id = extrasUser.id)
            }
            intent.extras.get(Flags.PROFILE_ID) != null -> {
                provider.loadUser(id = intent.extras.get(Flags.PROFILE_ID) as Long)
                provider.loadTweets(id = intent.extras.get(Flags.PROFILE_ID) as Long)
                provider.loadFavorites(id = intent.extras.get(Flags.PROFILE_ID) as Long)
            }
            intent.extras.get(Flags.PROFILE_SCREEN_NAME) != null -> {
                provider.loadUser(screenName = intent.extras.get(Flags.PROFILE_SCREEN_NAME) as String)
                provider.loadTweets(screenName = intent.extras.get(Flags.PROFILE_SCREEN_NAME) as String)
                provider.loadFavorites(screenName = intent.extras.get(Flags.PROFILE_SCREEN_NAME) as String)
            }
            else -> performError()
        }
    }

    fun setupUser(user: User) {
        this.user = user
        viewState.setupUser(user = user, homeUser = user.id == AppData.ME.id)
    }

    fun setupTweets(tweetsArray: ArrayList<StatusModel>) {
        this.tweetsArray.clear()
        this.tweetsArray.addAll(tweetsArray)
        viewState.setupTweets(tweetsArray = this.tweetsArray)
    }

    fun setupLikes(favoritesArray: ArrayList<StatusModel>) {
        viewState.setupLikes(likesArray = favoritesArray)
    }

    fun setupMedia(mediaArray: ArrayList<ImageModel>) {
        viewState.setupMedia(mediaArray = mediaArray)
    }

    fun performError() {
        viewState.showError(errorMessage = R.string.error_loading_user)
        viewState.performBack(startAnim = R.anim.slide_out_right, endAnim = R.anim.slide_in_left)
    }

    fun updateUI(dY: Int, avatarSize: Int, verifiedSize: Int, tailOffset: Int) {
        val verified = if (verifiedSize - dY < 0) 0 else verifiedSize - dY
        val avatar = if (avatarSize - dY < 0) 0 else (avatarSize - dY)
        val progress = ((avatarSize - dY).toFloat() / avatarSize)
        val diff = tailOffset - dY
        viewState.updateAvatar(verified = verified, avatar = avatar, progress = progress)
        viewState.updateTab(diff = diff)
        viewState.updateTitle(scrollY = dY)
    }

    fun selectFragment(fragment: Fragment, fragmentManager: FragmentManager) {
        try {
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fl_profile, fragment).commitNow()
        } catch (e: NullPointerException) {
            viewState.showError(errorMessage = R.string.error_loading_user)
        }

    }

    fun showAvatar() {
        user?.let { it ->
            val urls = ArrayList<String>()
            urls.add(it.originalProfileImageURL)
            viewState.showImage(urls = urls, startPosition = 0)
        }
    }

    fun showBackdrop() {
        user?.let { it ->
            val urls = ArrayList<String>()
            urls.add(it.profileBannerImageUrl)
            viewState.showImage(urls = urls, startPosition = 0)
        }
    }

    fun backClick() {
        viewState.performBack(startAnim = R.anim.slide_out_right, endAnim = R.anim.slide_in_left)
    }
}