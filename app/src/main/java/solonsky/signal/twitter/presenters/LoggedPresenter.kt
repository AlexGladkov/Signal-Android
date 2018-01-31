package solonsky.signal.twitter.presenters

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.models.ConfigurationModel
import solonsky.signal.twitter.providers.LoggedProvider
import solonsky.signal.twitter.room.AppDatabase
import solonsky.signal.twitter.views.LoggedView

/**
 * Created by sunwi on 22.01.2018.
 */
@InjectViewState
class LoggedPresenter: MvpPresenter<LoggedView>() {
    private val TAG: String = LoggedPresenter::class.java.simpleName
    private val provider = LoggedProvider(presenter = this@LoggedPresenter)

    // Fetch and setup app settings
    fun initState() {
        provider.fetchSettings()
    }

    fun fetchUsers() {
        provider.fetchUsers()
    }

    fun showMessage(text: String) {
        viewState.showMessage(text = text)
    }

    fun setupSettings(model: ConfigurationModel) {
        Log.e(TAG, "setup settings")
        AppData.appConfiguration = model
        App.getInstance().isNightEnabled = AppData.appConfiguration.darkMode == ConfigurationModel.DARK_ALWAYS
    }

    fun reloadSettings() {
        viewState.setupSettings()
    }

    fun saveSettings(settings: ConfigurationModel) {
        provider.saveSettings(settings = settings)
        setupSettings(settings)
    }


}