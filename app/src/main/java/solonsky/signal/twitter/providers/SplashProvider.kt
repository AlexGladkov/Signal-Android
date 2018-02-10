package solonsky.signal.twitter.providers

import android.os.Handler
import android.util.Log
import com.anupcowkur.reservoir.Reservoir
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Cache
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.models.ConfigurationModel
import solonsky.signal.twitter.models.ConfigurationUserModel
import solonsky.signal.twitter.presenters.SplashPresenter
import solonsky.signal.twitter.room.converters.ConfigurationConverterImpl
import solonsky.signal.twitter.room.converters.SettingsConverterImpl
import solonsky.signal.twitter.room.converters.UsersConverterImpl
import solonsky.signal.twitter.room.models.HosterEntity
import twitter4j.*

/**
 * Created by agladkov on 01.02.18.
 */
class SplashProvider(val presenter: SplashPresenter) {
    private val TAG: String = SplashProvider::class.java.simpleName
    private val settingsConverter = SettingsConverterImpl()
    private val usersConverter = UsersConverterImpl()
    private val configurationsConverter = ConfigurationConverterImpl()
    private val handler = Handler()
    private val profileDelay = 0L

    /** Download current profile from Twitter api
     * @param isLoaded = if false then perform next move else just update profile
     */
    private fun fetchRemoteProfile(isLoaded: Boolean) {
        val gson = Gson()
        val asyncTwitter = Utilities.getAsyncTwitter()
        asyncTwitter.addListener(object : TwitterAdapter() {
            override fun lookedupUsers(users: ResponseList<User>) {
                super.lookedupUsers(users)
                AppData.ME = solonsky.signal.twitter.models.User.getFromUserInstance(users[0])
                Log.e(TAG, "New avatar ${usersConverter.apiToDb(users[0]).originalProfileImageURL}")

                App.db.usersDao().insert(usersConverter.apiToDb(users[0]))
                App.db.hostersDao().update(
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

    /** Get settings from DB and perform legacy loaded if empty */
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

    /** Perform fetching profile from DB and get it from backend if empty
     * @see fetchRemoteProfile
     */
    fun fetchProfile() {
        Thread({
            val hosters = App.db.hostersDao().getAllByDate()
            if (hosters.isNotEmpty()) {
                val users = App.db.usersDao().getById(hosters[0].userId)
                if (users.isNotEmpty()) {
                    AppData.ME = usersConverter.dbToApi(users[0])
                    Log.e(TAG, "Me avatar ${users[0].originalProfileImageURL}")
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
                handler.post {
                    fetchRemoteProfile(isLoaded = false)
                }
            }
        }).start()
    }

    /** Save settings from legacy model
     * @param settings - legacy model
     */
    fun saveSettings(settings: ConfigurationModel) {
        Thread({
            App.db.settingsDao().insert(settingsConverter.modelToDb(settings))
        }).start()
    }

    /** Performs fetch user configuration from DB or load it from legacy if empty */
    fun fetchConfiguration() {
        Thread({
            AppData.configurationUserModels = App.db.configurationDao().getAllConfigurations()
                    .map { configurationsConverter.dbToModel(it) }
            Log.e(TAG, "config size ${AppData.configurationUserModels.size}, me id ${AppData.ME.id}")
            val configurationModels = AppData.configurationUserModels
                        .filter { it.user.id == AppData.ME.id }

            if (configurationModels.isEmpty()) { // Legacy support
                val resultType = object : TypeToken<List<ConfigurationUserModel>>() {}.type
                val configurationUserModels = Reservoir.get<List<ConfigurationUserModel>>(Cache.UsersConfigurations, resultType)
                        .filter { it.user.id == AppData.ME.id }

                if (configurationUserModels.isEmpty()) {
                    presenter.errorLoadingChain()
                } else {
                    saveConfiguration(configurationUserModel = configurationUserModels[0])
                    handler.post {
                        presenter.setupConfigurations(model = configurationUserModels[0])
                    }
                }
            } else {
                handler.post {
                    presenter.setupConfigurations(model = configurationModels[0])
                }
            }
        }).start()
    }

    /** Performs saving configuration in DB
     * @param configurationUserModel - model to save */
    private fun saveConfiguration(configurationUserModel: ConfigurationUserModel) {
        App.db.configurationDao().update(configurationsConverter
                .modelToDb(configurationUserModel = configurationUserModel))
    }
}