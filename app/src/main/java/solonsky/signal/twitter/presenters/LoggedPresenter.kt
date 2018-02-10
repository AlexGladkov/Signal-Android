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

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.setupProfile(avatar = AppData.ME.originalProfileImageURL)
    }

    // Fetch and setup app settings
    fun fetchUsers() {
        provider.fetchUsers()
    }

    fun showMessage(text: String) {
        viewState.showMessage(text = text)
    }

    fun saveConfiguration() {
        for (configurationUserModel in AppData.configurationUserModels) {
            if (configurationUserModel.user.id == AppData.userConfiguration.user.id) {
                configurationUserModel.tabPosition = AppData.userConfiguration.tabPosition
                configurationUserModel.bottomIds = AppData.userConfiguration.bottomIds
            }
        }

        provider.saveConfiguration(configurationUserModel = AppData.userConfiguration)
    }
}