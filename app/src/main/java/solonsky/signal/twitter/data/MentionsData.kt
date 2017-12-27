package solonsky.signal.twitter.data

import android.util.Log

import com.anupcowkur.reservoir.Reservoir
import solonsky.signal.twitter.helpers.AppData

import java.io.IOException
import java.util.ArrayList
import java.util.Collections
import java.util.ConcurrentModificationException

import solonsky.signal.twitter.helpers.Cache
import solonsky.signal.twitter.interfaces.UpdateAddHandler
import solonsky.signal.twitter.models.StatusModel

/**
 * Created by neura on 09.09.17.
 */

class MentionsData private constructor() {
    val TAG: String = MentionsData::class.simpleName.toString()
    var mentionsStatuses: ArrayList<StatusModel> = ArrayList()
    var newStatuses: ArrayList<StatusModel> = ArrayList()
    var entryCount: Int = 0
    var cacheEntryCount: Int = 0
    var scrollPosition: Int = 0
    var scrollTop: Int = 0
    var topId: Long = 0
    var updateHandler: UpdateAddHandler? = null

    init {
        this.mentionsStatuses = ArrayList()
        this.newStatuses = ArrayList()
        this.entryCount = 0
        this.cacheEntryCount = 0
        this.scrollPosition = 0
        this.scrollTop = 0
        this.updateHandler = null
    }

    fun clear() {
        this.mentionsStatuses.clear()
        this.newStatuses.clear()
        this.entryCount = 0
        this.cacheEntryCount = 0
        this.scrollPosition = 0
        this.scrollTop = 0
        this.topId = 0
    }

    fun addItemToNew(statusModel: StatusModel, isChangePosition: Boolean) {
        //        if (!Utilities.validateTweet(statusModel)) return;
        statusModel.isHighlighted = isChangePosition
        Log.e(TAG, "isHighlighted  " + statusModel.isHighlighted)

        if (!newStatuses.contains(statusModel))
            newStatuses.add(0, statusModel)

        if (isChangePosition) {
            entryCount = cacheEntryCount + newStatuses!!.size
        }
    }

    fun saveCache(saveTAG: String) {
        if (mentionsStatuses!!.size > 0) {
            try {
                Reservoir.put(Cache.Mentions + AppData.ME.id.toString(), this)
            } catch (e: IOException) {
                Log.e(TAG, "Error saving mentions " + e.localizedMessage)
            } catch (e: ConcurrentModificationException) {
                Log.e(TAG, "Error saving mentions " + e.localizedMessage)
            } catch (e: NullPointerException) {
                Log.e(TAG, "Error saving mentions " + e.localizedMessage)
            }

        } else {
            Log.e(TAG, "Try to save empty array from " + saveTAG)
        }
    }

    fun incEntryCount() {
        this.entryCount = entryCount + 1
    }

    fun updateEntryCount() {
        this.entryCount = cacheEntryCount + newStatuses.size
        this.cacheEntryCount = 0
    }

    fun addNewItems() {
        Collections.reverse(newStatuses)
        for (statusModel in newStatuses) {
            if (!mentionsStatuses.contains(statusModel)) {
                statusModel.isHighlighted = true
                this.mentionsStatuses.add(0, statusModel)
            }
        }

        this.newStatuses.clear()
    }

    fun decEntryCount() {
        this.entryCount = entryCount - 1
        if (entryCount < 0) this.entryCount = 0
    }

    private object Holder { val instance = MentionsData() }
    companion object {
        val instance: MentionsData by lazy { Holder.instance }
    }
}
