package solonsky.signal.twitter.providers

import android.os.Handler
import android.util.Log
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.models.User
import solonsky.signal.twitter.models.UserModel
import solonsky.signal.twitter.presenters.ComposePresenter
import solonsky.signal.twitter.room.converters.UsersConverterImpl
import java.util.*

/**
 * Created by sunwi on 23.01.2018.
 */
class ComposeProvider(private val presenter: ComposePresenter) {
    private val handler = Handler()
    private val TAG: String = ComposeProvider::class.java.simpleName
    private val converter = UsersConverterImpl()

    // Load mentions from DB
    fun fetchMentions() {
        Thread({
            val users: MutableList<UserModel> = LinkedList()
            App.db.usersDao().all
                    .map { converter.dbToModel(it) }
                    .forEach({
                        users.add(it)
                    })

            handler.post {
                presenter.setupData(users = users)
            }
        }).start()
    }

    fun addUser(user: twitter4j.User) {
        Thread({
            App.db.usersDao().insert(converter.apiToDb(user))
        }).start()
    }
}