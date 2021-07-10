package solonsky.signal.twitter.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

import com.anupcowkur.reservoir.Reservoir
import com.anupcowkur.reservoir.ReservoirGetCallback
import com.google.common.reflect.TypeToken

import org.joda.time.LocalDateTime

import java.io.IOException

import solonsky.signal.twitter.R
import solonsky.signal.twitter.activities.ChatActivity
import solonsky.signal.twitter.activities.MVPProfileActivity
import solonsky.signal.twitter.adapters.DirectAdapter
import solonsky.signal.twitter.api.DirectApi
import solonsky.signal.twitter.data.DirectData
import solonsky.signal.twitter.data.LoggedData
import solonsky.signal.twitter.databinding.FragmentDirectBinding
import solonsky.signal.twitter.helpers.*
import solonsky.signal.twitter.interfaces.ActivityListener
import solonsky.signal.twitter.interfaces.UpdateHandler
import solonsky.signal.twitter.models.DirectModel
import solonsky.signal.twitter.models.User
import solonsky.signal.twitter.viewmodels.DirectViewModel
import twitter4j.*
import java.util.*

/**
 * Created by neura on 23.05.17.
 */

class DirectFragment : Fragment() {
    private val TAG = DirectFragment::class.java.simpleName
    private val newCount = 0
    private lateinit var mAdapter: DirectAdapter
    private lateinit var binding: FragmentDirectBinding
    private var viewModel: DirectViewModel? = null
    private var mCallback: ActivityListener? = null

    private val directScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            mCallback?.let { it.updateBars(dy) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_direct, container, false)

        mAdapter = DirectAdapter(DirectData.getInstance().getmMessagesList(), context, activity as AppCompatActivity,
                object : DirectAdapter.DirectClickHandler {
                    override fun onItemClick(v: View, model: DirectModel) {
                        AppData.DM_POSITION = DirectData.getInstance().getmMessagesList().indexOf(model)
                        AppData.DM_SELECTED_USER = model.username
                        AppData.DM_OTHER_ID = model.otherId
                        Flags.DM_IS_NEW = model.messageCount > 0

                        DirectApi.getInstance().clear()
                        DirectApi.getInstance().userId = model.otherId
                        (activity as AppCompatActivity).startActivity(Intent(context, ChatActivity::class.java))
                        if (model.isHighlighted) {
                            (activity as AppCompatActivity).overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_no_animation)
                        } else {
                            (activity as AppCompatActivity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        }

                        Handler().postDelayed({
                            model.isHighlighted = false
                            model.messageCount = 0
                            val hasNew = DirectData.getInstance().getmMessagesList().any { it.messageCount > 0 }

                            if (!hasNew) {
                                LoggedData.getInstance().isNewMessage = false
                                LoggedData.getInstance().updateHandler.onUpdate()
                            }

                            saveCache()
                        }, 300)
                    }

                    override fun onAvatarClick(v: View, directModel: DirectModel) {
                        val profileIntent = Intent(context, MVPProfileActivity::class.java)
                        profileIntent.putExtra(Flags.PROFILE_ID, directModel.otherId)
                        (activity as AppCompatActivity).startActivity(profileIntent)
                        (activity as AppCompatActivity).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                })
        mAdapter.setHasStableIds(true)

        viewModel = if (DirectData.getInstance().getmMessagesList().size == 0) {
            DirectViewModel(mAdapter, context, AppData.UI_STATE_LOADING)
        } else {
            DirectViewModel(mAdapter, context, AppData.UI_STATE_VISIBLE)
        }

        binding.model = viewModel

        mCallback?.let {
            it.updateCounter(DirectData.getInstance().entryCount)
            it.updateSettings(R.string.title_direct, false, false)
            it.updateToolbarState(AppData.TOOLBAR_LOGGED_DM, if (App.getInstance().isNightEnabled)
                R.color.dark_status_bar_timeline_color
            else
                R.color.light_status_bar_timeline_color)
        }

        binding.recyclerDirect.setOnScrollListener(directScrollListener)
        binding.srlDirectMain.setProgressViewOffset(false,
                Utilities.convertDpToPixel(80f, context).toInt(),
                Utilities.convertDpToPixel(96f, context).toInt())
        binding.srlDirectMain.setOnRefreshListener {
            refreshData()
        }

        DirectData.getInstance().updateHandler = UpdateHandler {
            mAdapter.notifyDataSetChanged()
            saveCache()
        }

        if (DirectData.getInstance().getmMessagesList().size == 0) {
            loadCache()
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        try {
            if (isAdded && Flags.needsToRedrawDirect) {
                Flags.needsToRedrawDirect = false
                mAdapter.notifyDataSetChanged()
            }

            if (Flags.isUpdated) {
                mAdapter.notifyItemRangeChanged(0, mAdapter.itemCount)
            }
            if (Flags.DELETE_THREAD) {
                Flags.DELETE_THREAD = false
                mAdapter.notifyItemRemoved(AppData.DM_POSITION)
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallback = context as ActivityListener?
    }

    override fun onDetach() {
        super.onDetach()
        mCallback = null
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            mCallback?.let {
                it.updateCounter(DirectData.getInstance().entryCount)
                it.updateSettings(R.string.title_direct, true, true)
                it.updateToolbarState(AppData.TOOLBAR_LOGGED_DM, if (App.getInstance().isNightEnabled)
                    R.color.dark_status_bar_timeline_color
                else
                    R.color.light_status_bar_timeline_color)
            }
        }
    }

    /**
     * Load cache from disk
     *
     * @serialData tweets array
     */
    private fun loadCache() {
        val resultType = object : TypeToken<List<DirectModel>>() {}.type
        Reservoir.getAsync(Cache.DirectsList + AppData.ME.id.toString(), resultType,
                object : ReservoirGetCallback<List<DirectModel>> {
                    override fun onFailure(e: Exception) {
                        loadApi(Paging(1, 200))
                    }

                    override fun onSuccess(directModels: List<DirectModel>) {
                        for (directModel in directModels) {
                            DirectData.getInstance().getmMessagesList().add(directModel)
                        }

                        mAdapter.notifyDataSetChanged()
                        (if (DirectData.getInstance().getmMessagesList().size == 0)
                            AppData.UI_STATE_NO_ITEMS
                        else
                            AppData.UI_STATE_VISIBLE).also { viewModel!!.state = it }
                        loadApi(Paging(1, 200))
                    }
                })
    }

    /**
     * Save cache
     *
     * @serialData bytecode to disk
     */
    private fun saveCache() {
        try {
            Reservoir.put(Cache.DirectsList + AppData.ME.id.toString(), DirectData.getInstance().getmMessagesList())
        } catch (e: IOException) {
            Log.e(TAG, "Error caching directs - " + e.localizedMessage)
        }

    }

    private fun refreshData() = if (DirectData.getInstance().getmMessagesList().size > 0) {
        val handler = Handler()
        val asyncTwitter = Utilities.getAsyncTwitter()
        val paging = Paging()
        paging.sinceId = DirectData.getInstance().getmMessagesList()[0].id

        asyncTwitter.addListener(object: TwitterAdapter() {
            val directDictionary: HashMap<Long, DirectMessage> = HashMap()

            override fun onException(te: TwitterException?, method: TwitterMethod?) {
                super.onException(te, method)
                handler.post {
                    Toast.makeText(context, getString(R.string.error_loading_direct), Toast.LENGTH_SHORT).show()
                    binding.srlDirectMain.isRefreshing = false
                }
            }

            override fun gotSentDirectMessages(messages: ResponseList<DirectMessage>?) {
                super.gotSentDirectMessages(messages)
                messages?.reverse()
                messages?.forEach {
                    val id = if (it.senderId == AppData.ME.id)
                        it.recipientId
                    else
                        it.senderId

                    if (directDictionary.containsKey(id)) {
                        directDictionary.remove(id)
                    }

                    directDictionary.put(id, it)
                }

                Log.e(TAG, "size ${directDictionary.size}")

                DirectData.getInstance().getmMessagesList().forEach {
                    if (directDictionary.containsKey(it.otherId)) {
                        Log.e(TAG, "contains")
                        directDictionary[it.otherId]?.let { direct ->
                            Log.e(TAG, "message ${direct.text}")
                            it.lastMessage = direct.text
                            it.isHighlighted = true
                        }
                        directDictionary.remove(it.otherId)
                    }
                }

                directDictionary.keys.forEach {
                    DirectData.getInstance().getmMessagesList()
                        .add(DirectModel.getInstance(directDictionary[it], AppData.ME.id))
                }

                handler.post {
                    binding.srlDirectMain.isRefreshing = false
                    mAdapter.notifyDataSetChanged()
                }
            }

            override fun gotDirectMessages(messages: ResponseList<DirectMessage>?) {
                super.gotDirectMessages(messages)
                messages?.reverse()
                messages?.forEach {
                    val id = if (it.senderId == AppData.ME.id)
                        it.recipientId
                    else
                        it.senderId

                    if (directDictionary.containsKey(id)) {
                        directDictionary.remove(id)
                    }

                    directDictionary[id] = it
                }

                handler.post {
                    asyncTwitter.getSentDirectMessages(paging)
                }
            }
        })

        asyncTwitter.getDirectMessages(paging)
    } else {
        binding.srlDirectMain.isRefreshing = false
    }

    /**
     * Loading direct messages from twitter backend
     */
    fun loadApi(paging: Paging) {
        val handler = Handler()
        val loadedDirects = ArrayList<twitter4j.DirectMessage>()
        val asyncTwitter = Utilities.getAsyncTwitter()
        asyncTwitter.addListener(object : TwitterAdapter() {
            override fun gotDirectMessages(messages: ResponseList<twitter4j.DirectMessage>) {
                super.gotDirectMessages(messages)
                loadedDirects.addAll(messages)
                handler.post { asyncTwitter.getSentDirectMessages(paging) }
            }

            override fun gotSentDirectMessages(messages: ResponseList<twitter4j.DirectMessage>) {
                super.gotSentDirectMessages(messages)
                loadedDirects.addAll(messages)

                loadedDirects.sortWith(Comparator { o1, o2 -> o2.createdAt.compareTo(o1.createdAt) })

                val newDirects = ArrayList<twitter4j.DirectMessage>()
                val newUserDirects = ArrayList<DirectMessage>()

                if (DirectData.getInstance().getmMessagesList().size == 0) {
                    for (directMessage in loadedDirects) {
                        val id = if (directMessage.senderId == AppData.ME.id) directMessage.recipientId else directMessage.senderId
                        val user = User.getFromUserInstance(if (directMessage.senderId == AppData.ME.id)
                            directMessage.recipient
                        else
                            directMessage.sender)

                        val text = directMessage.text
                        directMessage.urlEntities.forEach {
                            text.replace(it.url, it.displayURL)
                        }

                        val directModel = DirectModel(directMessage.id, id, user.originalProfileImageURL,
                                user.name, text, LocalDateTime(directMessage.createdAt),
                                false)
                        if (!DirectData.getInstance().getmMessagesList().contains(directModel)) {
                            DirectData.getInstance().getmMessagesList().add(directModel)
                        }
                    }
                } else {
                    for (directMessage in loadedDirects) {
                        var hasId = false
                        var position = 0
                        for (directModel in DirectData.getInstance().getmMessagesList()) {
                            if (directMessage.senderId == directModel.otherId || directMessage.recipientId == directModel.otherId) {
                                hasId = true
                                position = DirectData.getInstance().getmMessagesList().indexOf(directModel)
                                break
                            }
                        }

                        if (hasId) {
                            if (directMessage.id > DirectData.getInstance().getmMessagesList()[position].id) {
                                newDirects.add(directMessage)
                            }
                        } else {
                            newUserDirects.add(directMessage)
                        }
                    }

                    for (directMessage in newDirects) {
                        val id = if (AppData.ME.id == directMessage.senderId)
                            directMessage.recipientId
                        else
                            directMessage.senderId

                        val user = if (AppData.ME.id == directMessage.senderId)
                            User.getFromUserInstance(directMessage.recipient)
                        else
                            User.getFromUserInstance(directMessage.sender)

                        var entryCount = 0
                        for (directModel in DirectData.getInstance().getmMessagesList()) {
                            if (directModel.otherId == directMessage.senderId || directModel.otherId == directMessage.recipientId) {
                                entryCount = directModel.messageCount
                                DirectData.getInstance().getmMessagesList().removeAt(
                                        DirectData.getInstance().getmMessagesList().indexOf(directModel))
                                break
                            }
                        }

                        val text = directMessage.text
                        directMessage.urlEntities.forEach {
                            Log.e(TAG, "before replace $text")
                            text.replace(it.url, it.displayURL)
                            Log.e(TAG, "after replace $text")
                        }

                        val directModel = DirectModel(
                                directMessage.id, id, user.originalProfileImageURL,
                                user.name, text, LocalDateTime(directMessage.createdAt), false)
                        if (directMessage.senderId != AppData.ME.id)
                            directModel.messageCount = entryCount + 1
                        DirectData.getInstance().getmMessagesList().add(0, directModel)
                    }

                    for (directMessage in newUserDirects) {
                        val id = if (AppData.ME.id == directMessage.senderId)
                            directMessage.recipientId
                        else
                            directMessage.senderId
                        val hasId = DirectData.getInstance().getmMessagesList().any { it.otherId == id }

                        if (!hasId) {
                            val parsedUser = if (AppData.ME.id == directMessage.senderId)
                                directMessage.recipient
                            else
                                directMessage.sender
                            val user = User.getFromUserInstance(parsedUser)

                            val text = directMessage.text
                            directMessage.urlEntities.forEach {
                                text.replace(it.url, it.displayURL)
                            }

                            val directModel = DirectModel(
                                    directMessage.id, id, user.originalProfileImageURL,
                                    user.name, text, LocalDateTime(directMessage.createdAt), false)
                            if (!DirectData.getInstance().getmMessagesList().contains(directModel)) {
                                DirectData.getInstance().getmMessagesList().add(directModel)
                            }
                        }
                    }
                }

                val hasNew = DirectData.getInstance().getmMessagesList().any { it.messageCount > 0 }
                LoggedData.getInstance().isNewMessage = hasNew

                handler.post {
                    mAdapter.notifyDataSetChanged()
                    viewModel!!.state = if (DirectData.getInstance().getmMessagesList().size == 0)
                        AppData.UI_STATE_NO_ITEMS
                    else
                        AppData.UI_STATE_VISIBLE
                    LoggedData.getInstance().updateHandler.onUpdate()
                }
            }

            override fun onException(te: TwitterException?, method: TwitterMethod?) {
                super.onException(te, method)
                te?.let {
                    Log.e(TAG, "Error while loading data - " + it.localizedMessage)
                }
            }
        })

        asyncTwitter.getDirectMessages(paging)
    }
}
