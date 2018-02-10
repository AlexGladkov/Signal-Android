package solonsky.signal.twitter.providers

import android.os.Handler
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.models.ConfigurationUserModel
import solonsky.signal.twitter.presenters.LoggedPresenter
import solonsky.signal.twitter.room.converters.ConfigurationConverterImpl
import solonsky.signal.twitter.room.converters.UsersConverterImpl
import solonsky.signal.twitter.room.models.UserEntity
import solonsky.signal.twitter.room.models.UserIDEntity
import twitter4j.*
import java.util.*

/**
 * Created by sunwi on 22.01.2018.
 */

class LoggedProvider(val presenter: LoggedPresenter) {
    private val handler = Handler()
    private val TAG: String = LoggedProvider::class.java.simpleName
    private val userConverter = UsersConverterImpl()
    private val configurationConverter = ConfigurationConverterImpl()
    private val maxUsers = 100

    fun fetchUsers() {
        fetchRemoteUsers()
    }

    private fun fetchRemoteUsers() {
        val asyncTwitter = Utilities.getAsyncTwitter()
        val idSet: MutableSet<Long> = HashSet()

        asyncTwitter.addListener(object : TwitterAdapter() {
            override fun onException(te: TwitterException?, method: TwitterMethod?) {
                super.onException(te, method)
                method?.let {
                    val arr = LongArray(idSet.size, { idSet.elementAt(it) })
                    val userIds = UserIDEntity(AppData.ME.id, idSet.toString())
                    App.db.userIDsDao().insert(userIds)
                    asyncTwitter.lookupUsers(*arr)
                }
            }

            override fun lookedupUsers(users: ResponseList<twitter4j.User>) {
                super.lookedupUsers(users)
                val entities: List<UserEntity> = users.map { userConverter.apiToDb(it) }
                App.db.usersDao().insertAll(*entities.toTypedArray())
            }

            override fun gotFollowersIDs(ids: IDs) {
                super.gotFollowersIDs(ids)
                ids.iDs.forEach {
                    if (idSet.size < maxUsers)
                        idSet.add(it)
                }

                if (idSet.size >= maxUsers) {
                    val arr = LongArray(idSet.size, { idSet.elementAt(it) })
                    val userIds = UserIDEntity(AppData.ME.id, idSet.toString())
                    App.db.userIDsDao().insert(userIds)
                    asyncTwitter.lookupUsers(*arr)
                } else {
                    if (ids.nextCursor == 0L) {
                        val arr = LongArray(idSet.size, { idSet.elementAt(it) })
                        val userIds = UserIDEntity(AppData.ME.id, idSet.toString())
                        App.db.userIDsDao().insert(userIds)
                        asyncTwitter.lookupUsers(*arr)
                    } else {
                        asyncTwitter.getFollowersIDs(ids.nextCursor)
                    }
                }
            }

            override fun gotFriendsIDs(ids: IDs) {
                super.gotFriendsIDs(ids)
                ids.iDs.forEach {
                    if (idSet.size < maxUsers)
                        idSet.add(it)
                }

                if (idSet.size >= maxUsers) {
                    val arr = LongArray(idSet.size, { idSet.elementAt(it) })
                    val userIds = UserIDEntity(AppData.ME.id, idSet.toString())
                    App.db.userIDsDao().insert(userIds)
                    asyncTwitter.lookupUsers(*arr)
                } else {
                    if (ids.nextCursor == 0L) {
                        asyncTwitter.getFollowersIDs(-1)
                    } else {
                        asyncTwitter.getFriendsIDs(ids.nextCursor)
                    }
                }
            }
        })

        asyncTwitter.getFriendsIDs(-1)
    }

    fun saveConfiguration(configurationUserModel: ConfigurationUserModel) {
        Thread({
            App.db.configurationDao().update(configurationConverter.modelToDb(configurationUserModel = configurationUserModel))
        })
    }
}