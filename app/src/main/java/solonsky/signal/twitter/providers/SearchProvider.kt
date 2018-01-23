package solonsky.signal.twitter.providers

import android.os.Handler
import com.google.gson.Gson
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.presenters.SearchPresenter
import twitter4j.*
import java.util.*

/**
 * Created by sunwi on 13.01.2018.
 */

class SearchProvider(private val searchPresenter: SearchPresenter) {
    private val asyncTwitter = Utilities.getAsyncTwitter()
    private val handler = Handler()
    private val gson = Gson()
    private var query = ""

    init {
        asyncTwitter.addListener(object: TwitterAdapter() {
            override fun onException(te: TwitterException?, method: TwitterMethod?) {
                super.onException(te, method)
                handler.post {
                    te?.let {
                        searchPresenter.showError(errorMessage = it.errorMessage)
                    }
                }
            }

            override fun searched(queryResult: QueryResult) {
                super.searched(queryResult)
                val searchedTweets: MutableList<StatusModel> = LinkedList()
                val searchedMedia: MutableList<StatusModel> = LinkedList()

                queryResult.tweets.forEach({
                    val statusModel = StatusModel.getNewInstance(it, gson)
                    searchedTweets.add(statusModel)

                    if (statusModel.mediaEntities.size() > 0 || statusModel.urlEntities.size() > 0) {
                        searchedMedia.add(statusModel)
                    }
                })

                handler.post {
                    searchPresenter.loadAll(dataList = searchedTweets)
                    searchPresenter.loadMedia(dataList = searchedMedia)
                }
            }

            override fun searchedUser(userList: ResponseList<User>) {
                super.searchedUser(userList)
                val searchedUsers: MutableList<solonsky.signal.twitter.models.User> = LinkedList()

                userList.forEach({
                    val user = gson.fromJson(gson.toJsonTree(it), solonsky.signal.twitter.models.User::class.java)
                    user.biggerProfileImageURL = it.biggerProfileImageURL
                    user.originalProfileImageURL = it.originalProfileImageURL
                    searchedUsers.add(user)
                })

                handler.post {
                    searchPresenter.loadUsers(dataList = searchedUsers)
                }
            }

            override fun gotHomeTimeline(statuses: ResponseList<Status>) {
                super.gotHomeTimeline(statuses)
                val searchedHome: MutableList<StatusModel> = LinkedList()

                statuses.forEach({
                    val statusModel = StatusModel.getNewInstance(it, gson)

                    if (isHome(it)) {
                        searchedHome.add(statusModel)
                    }
                })

                handler.post {
                    searchPresenter.loadHome(dataList = searchedHome)
                }
            }
        })
    }

    private fun isHome(statusModel: Status): Boolean {
        if (statusModel.text.toLowerCase().contains(query.toLowerCase())) {
            return true
        }

        if (statusModel.user.name.toLowerCase().contains(query.toLowerCase())) {
            return true
        }

        if (statusModel.user.screenName.toLowerCase().contains(query.toLowerCase())) {
            return true
        }

        if (statusModel.retweetedStatus != null && statusModel.retweetedStatus.user.name.toLowerCase().contains(query.toLowerCase())) {
            return true
        }

        if (statusModel.retweetedStatus != null && statusModel.retweetedStatus.user.screenName.toLowerCase().contains(query.toLowerCase())) {
            return true
        }

        return statusModel.urlEntities.any { it.displayURL.toLowerCase().contains(query.toLowerCase()) }
    }

    fun fetchSearchData(searchQuery: String) {
        this.query = searchQuery
        val query = Query(searchQuery)
        query.count = 100

        asyncTwitter.search(query)
        asyncTwitter.searchUsers(searchQuery, 0)
        asyncTwitter.getHomeTimeline(Paging(1, 100))
    }
}