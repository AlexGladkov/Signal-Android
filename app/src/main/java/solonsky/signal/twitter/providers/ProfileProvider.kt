package solonsky.signal.twitter.providers

import android.os.Handler
import android.util.Log
import com.google.gson.Gson
import com.pawegio.kandroid.i
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.models.ImageModel
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.presenters.ProfilePresenter
import twitter4j.*
import java.util.*


/**
 * Created by sunwi on 22.11.2017.
 */

class ProfileProvider(presenter: ProfilePresenter) {
    private val TAG: String = ProfileProvider::class.simpleName.toString()
    private val mPresenter = presenter

    private fun getTwitter(): AsyncTwitter {
        val handler = Handler()
        val asyncTwitter = Utilities.getAsyncTwitter()

        asyncTwitter.addListener(object : TwitterAdapter() {
            override fun onException(te: TwitterException?, method: TwitterMethod?) {
                super.onException(te, method)
                handler.post {
                    mPresenter.performError()
                }
            }

            override fun lookedupUsers(users: ResponseList<User>) {
                super.lookedupUsers(users)
                handler.post {
                    mPresenter.setupUser(solonsky.signal.twitter.models.User.getFromUserInstance(users[0]))
                }
            }

            override fun gotFavorites(statuses: ResponseList<Status>) {
                super.gotFavorites(statuses)
                val gson = Gson()
                val favoritesArray = ArrayList<StatusModel>()

                statuses.forEach({
                    val statusModel = gson.fromJson(gson.toJsonTree(it), StatusModel::class.java)
                    statusModel.tuneModel(it)
                    statusModel.linkClarify()

                    favoritesArray.add(statusModel)
                })

                handler.post {
                    mPresenter.setupLikes(favoritesArray = favoritesArray)
                }
            }

            override fun gotUserTimeline(statuses: ResponseList<Status>) {
                Log.e(TAG, "timeline loaded ${System.currentTimeMillis()}")
                super.gotUserTimeline(statuses)
                val gson = Gson()
                val tweetsArray = ArrayList<StatusModel>()
                val mediaArray = ArrayList<ImageModel>()

                statuses.forEach({
                    val statusModel = gson.fromJson(gson.toJsonTree(it), StatusModel::class.java)
                    statusModel.tuneModel(it)
                    statusModel.linkClarify()

                    tweetsArray.add(statusModel)
                    it.mediaEntities.forEach { media ->
                        mediaArray.add(ImageModel(media.mediaURL))
                    }
                })

                handler.post {
                    mPresenter.setupMedia(mediaArray = mediaArray)
                    mPresenter.setupTweets(tweetsArray = tweetsArray)
                }
            }
        })

        return asyncTwitter
    }

    var maxId: Long = 0
    var counts = 0

    fun loadMedia(id: Long, screenName: String) {
        val handler = Handler()
        val mediaAsync = Utilities.getAsyncTwitterMediaOnly()
        val mediaArray = ArrayList<ImageModel>()
        val tweetsArray = ArrayList<StatusModel>()
        mediaAsync.addListener(object: TwitterAdapter() {
            override fun onException(te: TwitterException?, method: TwitterMethod?) {
                super.onException(te, method)
                handler.post {
                    mPresenter.performError()
                }
            }

            override fun gotUserTimeline(statuses: ResponseList<Status>) {
                super.gotUserTimeline(statuses)
                val gson = Gson()

                statuses.forEach({
                    val statusModel = gson.fromJson(gson.toJsonTree(it), StatusModel::class.java)
                    statusModel.tuneModel(it)
                    statusModel.linkClarify()

                    tweetsArray.add(statusModel)
                    it.mediaEntities.forEach { media ->
                        mediaArray.add(ImageModel(media.mediaURL))
                    }
                })

                maxId = if (statuses.size > 0) statuses[statuses.size - 1].id else maxId

                if (tweetsArray.size >= 50) {
                    handler.post {
                        mPresenter.setupTweets(tweetsArray = tweetsArray)
                    }
                }

                if (mediaArray.size < 16 && counts < 4) {
                    counts += 1
                    val paging = Paging()
                    paging.maxId = maxId
                    paging.count = 50
                    if (id == Long.MIN_VALUE) {
                        mediaAsync.getUserTimeline(screenName, paging)
                    } else {
                        mediaAsync.getUserTimeline(id, paging)
                    }
                } else {
                    handler.post({
                        mPresenter.setupMedia(mediaArray = mediaArray)
                    })
                }
            }
        })

        val paging = Paging(1, 50)
        Log.e(TAG, "start loading timeline ${System.currentTimeMillis()}")
        counts = 0
        if (id == Long.MIN_VALUE) {
            mediaAsync.getUserTimeline(screenName, paging)
        } else {
            mediaAsync.getUserTimeline(id, paging)
        }

    }


    fun loadFavorites(id: Long) {
        val paging = Paging(1, 50)
        getTwitter().getFavorites(id, paging)
    }

    fun loadFavorites(screenName: String) {
        val paging = Paging(1, 50)
        getTwitter().getFavorites(screenName, paging)
    }

    fun loadUser(id: Long) {
        getTwitter().lookupUsers(id)
    }

    fun loadUser(screenName: String) {
        getTwitter().lookupUsers(screenName)
    }
}