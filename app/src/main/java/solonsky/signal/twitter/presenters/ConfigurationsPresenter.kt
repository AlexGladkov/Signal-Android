package solonsky.signal.twitter.presenters

import android.os.Handler
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import org.joda.time.DateTime
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.models.ConfigurationModel
import solonsky.signal.twitter.models.ConfigurationUserModel
import solonsky.signal.twitter.models.User
import solonsky.signal.twitter.room.converters.ConfigurationConverterImpl
import solonsky.signal.twitter.room.converters.SettingsConverterImpl
import solonsky.signal.twitter.room.converters.UsersConverter
import solonsky.signal.twitter.room.converters.UsersConverterImpl
import solonsky.signal.twitter.room.models.HosterEntity
import solonsky.signal.twitter.views.ConfigurationView

/**
 * Created by neura on 10.02.18.
 */
@InjectViewState
class ConfigurationsPresenter: MvpPresenter<ConfigurationView>() {
    private val TAG: String = ConfigurationsPresenter::class.java.simpleName
    private var configConverter = ConfigurationConverterImpl()
    private var settingsConverter = SettingsConverterImpl()
    private var userConverter = UsersConverterImpl()
    private val handler = Handler()

    /** Updates app settings
     * @param model - new app settings model */
    fun updateAppSettings(model: ConfigurationModel) {
        Thread({
            App.db.settingsDao().insert(settingsConverter.modelToDb(configurationModel = model))
            handler.post {
                viewState.settingsUpdated()
            }
        }).start()
    }

    /** Update current user settings
     * @param model - new user settings model */
    fun updateUserConfiguration(model: ConfigurationUserModel) {
        Thread({
            App.db.configurationDao().update(entity = configConverter.modelToDb(model))
            handler.post {
                viewState.settingsUpdated()
            }
        }).start()
    }

    /** Add new user model
     * @param newConfig - new user settings model */
    fun addNewUserConfiguration(newConfig: ConfigurationUserModel) {
        Thread {
            App.db.configurationDao().insert(entity = configConverter.modelToDb(newConfig))
        }.start()
    }

    /** Update user's timestamps
      * @param userId - User id to update */
    fun updateUser(userId: Long) {
        Thread({
            App.db.hostersDao().update(hosterEntity = HosterEntity(userId,
                    DateTime().toString("dd.MM.yyyy HH:mm:ss"), userId))
        }).start()
    }

    fun addNewHost(user: twitter4j.User) {
        Thread({
            App.db.usersDao().insert(userConverter.apiToDb(user))
            App.db.hostersDao().insert(hosterEntity = HosterEntity(user.id,
                    DateTime().toString("dd.MM.yyyy HH:mm:ss"), user.id))
        }).start()
    }
}