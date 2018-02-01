package solonsky.signal.twitter.providers

import android.os.Handler
import android.util.Log
import com.google.gson.Gson
import org.joda.time.DateTime
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.models.ConfigurationModel
import solonsky.signal.twitter.presenters.SplashPresenter
import solonsky.signal.twitter.room.converters.SettingsConverter
import solonsky.signal.twitter.room.converters.SettingsConverterImpl
import solonsky.signal.twitter.room.converters.UsersConverterImpl
import solonsky.signal.twitter.room.models.HosterEntity
import twitter4j.*
import java.time.LocalDateTime

/**
 * Created by agladkov on 01.02.18.
 */
class SplashProvider(val presenter: SplashPresenter) {
    private val TAG: String = SplashProvider::class.java.simpleName
    private val settingsConverter = SettingsConverterImpl()
    private val usersConverter = UsersConverterImpl()
    private val handler = Handler()
    private val profileDelay = 1000L

    private fun fetchRemoteProfile(isLoaded: Boolean) {
        val gson = Gson()
        val asyncTwitter = Utilities.getAsyncTwitter()
        asyncTwitter.addListener(object : TwitterAdapter() {
            override fun lookedupUsers(users: ResponseList<User>) {
                super.lookedupUsers(users)
                AppData.ME = gson.fromJson(gson.toJsonTree(users[0]), solonsky.signal.twitter.models.User::class.java)
                AppData.ME.biggerProfileImageURL = users[0].biggerProfileImageURL
                AppData.ME.originalProfileImageURL = users[0].originalProfileImageURL

                App.db.usersDao().insert(usersConverter.apiToDb(users[0]))
                App.db.hostersDao().insert(
                        HosterEntity(AppData.ME.id,
                        DateTime().toString("dd.MM.yyyy HH:mm:ss"), AppData.ME.id))

                if (!isLoaded) {
                    handler.post {
                        presenter.setupProfile()
                    }
                }
            }

            override fun onException(te: TwitterException?, method: TwitterMethod?) {
                super.onException(te, method)
                handler.postDelayed({
                    fetchRemoteProfile(isLoaded = isLoaded)
                }, profileDelay)
            }
        })

        Thread(Runnable {
            try {
                val myId = asyncTwitter.id
                asyncTwitter.lookupUsers(myId)
            } catch (e: TwitterException) {
                handler.postDelayed({
                    fetchRemoteProfile(isLoaded = isLoaded)
                }, profileDelay)
            }
        }).start()
    }

    fun fetchSettings() {
        Thread({
            val settings: List<ConfigurationModel> = App.db.settingsDao().getAll().map {
                settingsConverter.dbToModel(it)
            }

            if (settings.isNotEmpty()) {
                handler.post {
                    presenter.setupSettings(model = settings[0])
                }
            } else {
                if (AppData.appConfiguration != null) {
                    App.db.settingsDao().insert(settingsConverter.modelToDb(AppData.appConfiguration))
                } else {
                    handler.post {
                        presenter.reloadSettings()
                    }
                }
            }
        }).start()
    }

    fun fetchProfile() {
        Thread({
            val hosters = App.db.hostersDao().getAll()
            if (hosters.isNotEmpty()) {
                val users = App.db.usersDao().getById(hosters[0].userId)
                if (users.isNotEmpty()) {
                    AppData.ME = usersConverter.dbToApi(users[0])
                    handler.post {
                        presenter.setupProfile()
                    }

                    handler.postDelayed({ fetchRemoteProfile(isLoaded = true) }, profileDelay)
                } else {
                    handler.post {
                        fetchRemoteProfile(isLoaded = false)
                    }
                }
            } else {
                handler.post{
                    fetchRemoteProfile(isLoaded = false)
                }
            }
        }).start()
    }

    fun saveSettings(settings: ConfigurationModel) {
        Thread({
            App.db.settingsDao().insert(settingsConverter.modelToDb(settings))
        }).start()
    }
}