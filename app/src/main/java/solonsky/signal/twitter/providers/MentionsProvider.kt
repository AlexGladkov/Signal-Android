package solonsky.signal.twitter.providers

import android.os.Handler
import com.anupcowkur.reservoir.Reservoir
import com.anupcowkur.reservoir.ReservoirGetCallback
import com.fatboyindustrial.gsonjodatime.Converters
import com.google.gson.GsonBuilder
import solonsky.signal.twitter.data.MentionsData
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Cache
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.presenters.MentionsPresenter
import twitter4j.*
import java.util.*

/**
 * Created by neura on 06.11.17.
 */
class MentionsProvider(presenter: MentionsPresenter) {
    val TAG = MentionsProvider::class.simpleName.toString()
    val mPresenter = presenter
    val gson = Converters.registerLocalDateTime(GsonBuilder()).create()

    fun testLoadMore() {
        Handler().postDelayed({
            mPresenter.loadedMore()
        }, 1000)
    }

    fun getData() {
        val handler = Handler()
        if (MentionsData.instance.mentionsStatuses.size == 0) {
            Thread {
                Reservoir.getAsync(Cache.Mentions, MentionsData::class.java,
                    object : ReservoirGetCallback<MentionsData> {
                        override fun onSuccess(mentionsData: MentionsData) {
                            MentionsData.instance.mentionsStatuses.addAll(
                                mentionsData.mentionsStatuses.filter { it.user.id != AppData.ME.id })

                            var entryCount = 0
                            MentionsData.instance.mentionsStatuses
                                .asSequence()
                                .filter { it.isHighlighted }
                                .map { it.isExpand = false }
                                .forEach { entryCount += 1 }

                            handler.post {
                                mPresenter.loaded(MentionsData.instance.mentionsStatuses)
                                loadNew(MentionsData.instance.mentionsStatuses[0].id)
                            }
                        }

                        override fun onFailure(e: Exception) {
                            handler.post { loadClear() }
                        }
                    })
            }.start()
        } else {
            mPresenter.loaded(mentionsStatuses = MentionsData.instance.mentionsStatuses)
        }
    }

    private fun loadClear() {
        val handler = Handler()
        val paging = Paging(1, 100)

        val asyncTwitter = Utilities.getAsyncTwitter()
        asyncTwitter.addListener(object : TwitterAdapter() {
            override fun onException(te: TwitterException?, method: TwitterMethod?) {
                super.onException(te, method)
                handler.post {
                    mPresenter.showException(te?.errorMessage)
                }
            }

            override fun gotMentions(statuses: ResponseList<Status>) {
                super.gotMentions(statuses)
                val prepared = prepareStatuses(statuses)
                MentionsData.instance.mentionsStatuses.addAll(prepared)

                handler.post {
                    mPresenter.loaded(prepared)
                }
            }
        })
        asyncTwitter.getMentions(paging)
    }

    fun loadNew(sinceId: Long) {
        val handler = Handler()
        val paging = Paging(1, 100)
        paging.sinceId = sinceId

        val asyncTwitter = Utilities.getAsyncTwitter()
        asyncTwitter.addListener(object : TwitterAdapter() {
            override fun onException(te: TwitterException?, method: TwitterMethod?) {
                super.onException(te, method)
                handler.post {
                    mPresenter.showException(te?.errorMessage)
                }
            }

            override fun gotMentions(statuses: ResponseList<Status>) {
                super.gotMentions(statuses)
                statuses.reverse()
                val prepared = prepareStatuses(statuses = statuses)
                        .filter { it.user.id != AppData.ME.id }

                prepared.forEach {
                    it.isHighlighted = true
                    MentionsData.instance.mentionsStatuses.add(0, it)
                }

                handler.post {
                    MentionsData.instance.saveCache(TAG)
                    mPresenter.loaded(mentionsStatuses = MentionsData.instance.mentionsStatuses)
                }
            }
        })

        asyncTwitter.getMentions(paging)
    }

    fun oldLoadMore() {
        val handler = Handler()
        val maxId = MentionsData.instance.mentionsStatuses[MentionsData.instance.mentionsStatuses.size - 1].id
        val paging = Paging()
        paging.maxId = maxId
        paging.count = 50

        val asyncTwitter = Utilities.getAsyncTwitter()
        asyncTwitter.addListener(object : TwitterAdapter() {
            override fun onException(te: TwitterException?, method: TwitterMethod?) {
                super.onException(te, method)
                handler.post {
                    mPresenter.showException(te?.errorMessage)
                }
            }

            override fun gotMentions(statuses: ResponseList<Status>?) {
                super.gotMentions(statuses)
                statuses?.forEach({
                    if (it.id < maxId) {
                        val statusModel = gson.fromJson(gson.toJsonTree(it), StatusModel::class.java)
                        statusModel.tuneModel(it)
                        statusModel.linkClarify()
                        MentionsData.instance.mentionsStatuses.add(statusModel)
                    }
                })

                MentionsData.instance.saveCache(TAG)

                handler.post {
                    mPresenter.loaded(mentionsStatuses = MentionsData.instance.mentionsStatuses)
                    mPresenter.loadedMore()
                }
            }
        })

        asyncTwitter.getMentions(paging)
    }

    private fun prepareStatuses(statuses: ResponseList<Status>): ArrayList<StatusModel> {
        val result: ArrayList<StatusModel> = ArrayList()
        statuses
                .filter { it.user.id != AppData.ME.id }
                .forEach({
            val statusModel = gson.fromJson(gson.toJsonTree(it), StatusModel::class.java)
            statusModel.tuneModel(it)
            statusModel.linkClarify()

            result.add(statusModel)
        })

        return result
    }
}