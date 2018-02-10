package solonsky.signal.twitter.presenters

import android.content.res.Configuration
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.models.ConfigurationModel
import solonsky.signal.twitter.models.ConfigurationUserModel
import solonsky.signal.twitter.providers.SplashProvider
import solonsky.signal.twitter.room.models.ConfigurationEntity
import solonsky.signal.twitter.views.SplashView

/**
 * Created by agladkov on 01.02.18.
 */
@InjectViewState
class SplashPresenter: MvpPresenter<SplashView>() {
    private val provider = SplashProvider(presenter = this)

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
    }

    fun initState() {
        provider.fetchProfile()
    }

    fun setupSettings(model: ConfigurationModel) {
        AppData.appConfiguration = model
        App.getInstance().isNightEnabled = AppData.appConfiguration.darkMode == ConfigurationModel.DARK_ALWAYS
        provider.fetchConfiguration()
    }

    fun reloadSettings() {
        viewState.setupSettings()
    }

    fun saveSettings(settings: ConfigurationModel) {
        provider.saveSettings(settings = settings)
        setupSettings(settings)
    }

    fun setupProfile() {
        provider.fetchSettings()
    }

    fun setupConfigurations(model: ConfigurationUserModel) {
        AppData.CLIENT_TOKEN = model.clientToken
        AppData.CLIENT_SECRET = model.clientSecret
        AppData.userConfiguration = model
        viewState.performLogged()
    }

    fun cleanBoot() {
        viewState.performLogin()
    }

    fun errorLoadingChain() {
        viewState.performLogin()
    }
}