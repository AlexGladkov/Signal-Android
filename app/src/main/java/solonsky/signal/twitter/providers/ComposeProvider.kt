package solonsky.signal.twitter.providers

import android.os.Handler
import android.util.Log
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.models.User
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

    fun fetchMentions() {
        Thread({
            val data = App.db.userIDsDao().getAllById(AppData.ME.id)
            if (data.size > 0) {
                val users: MutableList<User> = LinkedList()
                App.db.usersDao().all
                        .map { converter.dbToModel(it) }
                        .forEach({
                            users.add(it)
                        })

                handler.post {
                    presenter.setupData(users = users)
                }
            }
        }).start()
    }
}