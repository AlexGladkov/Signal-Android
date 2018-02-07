package solonsky.signal.twitter.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.providers.LoginProvider
import solonsky.signal.twitter.views.LoginView
import twitter4j.User

/**
 * Created by agladkov on 07.02.18.
 */
@InjectViewState
class LoginPresenter: MvpPresenter<LoginView>() {
    val provider = LoginProvider(this)
    lateinit var user: User

    fun prepareSignal(user: twitter4j.User) {
        this.user = user
        provider.saveConfiguration(configurationUserModel = AppData.userConfiguration)
    }

    fun configLoaded() {
        provider.saveSettings()
    }

    fun settingsLoaded() {
        provider.saveMe(user = user)
    }

    fun userLoaded() {
        viewState.performLogged()
    }
}