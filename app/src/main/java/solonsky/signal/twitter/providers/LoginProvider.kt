package solonsky.signal.twitter.providers

import android.os.Handler
import org.joda.time.DateTime
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.models.ConfigurationModel
import solonsky.signal.twitter.models.ConfigurationUserModel
import solonsky.signal.twitter.presenters.LoggedPresenter
import solonsky.signal.twitter.presenters.LoginPresenter
import solonsky.signal.twitter.room.converters.ConfigurationConverterImpl
import solonsky.signal.twitter.room.converters.SettingsConverterImpl
import solonsky.signal.twitter.room.converters.UsersConverterImpl
import solonsky.signal.twitter.room.models.HosterEntity
import twitter4j.User

/**
 * Created by agladkov on 07.02.18.
 */
class LoginProvider(val presenter: LoginPresenter) {
    private val configurationConverter = ConfigurationConverterImpl()
    private val settingsConverter = SettingsConverterImpl()
    private val usersConverter = UsersConverterImpl()
    private val handler = Handler()

    fun saveConfiguration(configurationUserModel: ConfigurationUserModel) {
        Thread({
            App.db.configurationDao().insert(configurationConverter.modelToDb(configurationUserModel = configurationUserModel))

            handler.post {
                presenter.configLoaded()
            }
        }).start()
    }

    fun saveSettings() {
        Thread({
            AppData.appConfiguration = ConfigurationModel.defaultSettings()
            App.db.settingsDao().insert(settingsConverter.modelToDb(AppData.appConfiguration))

            handler.post {
                presenter.settingsLoaded()
            }
        }).start()
    }

    fun saveMe(user: User) {
        Thread({
            AppData.ME = solonsky.signal.twitter.models.User.getFromUserInstance(user)

            App.db.usersDao().insert(usersConverter.apiToDb(user))
            App.db.hostersDao().insert(
                    HosterEntity(AppData.ME.id,
                            DateTime().toString("dd.MM.yyyy HH:mm:ss"), AppData.ME.id))

            handler.post {
                presenter.userLoaded()
            }
        }).start()
    }
}