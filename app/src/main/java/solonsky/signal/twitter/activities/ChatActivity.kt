package solonsky.signal.twitter.activities

import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.FrameLayout

import com.google.android.exoplayer.util.Util

import solonsky.signal.twitter.R
import solonsky.signal.twitter.api.DirectApi
import solonsky.signal.twitter.databinding.ActivityChatBinding
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Flags
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.interfaces.UpdateAddHandler
import solonsky.signal.twitter.models.ChatModel
import solonsky.signal.twitter.viewmodels.ChatViewModel
import twitter4j.AsyncTwitter
import twitter4j.TwitterAdapter
import twitter4j.TwitterException
import twitter4j.TwitterMethod

/**
 * Created by neura on 23.05.17.
 * Performs chat interaction activity
 */

class ChatActivity : AppCompatActivity() {
    private val TAG = ChatActivity::class.java.simpleName
    var binding: ActivityChatBinding? = null
        private set
    private var mActivity: ChatActivity? = null
    private var _xDelta: Int = 0
    private var _yDelta: Int = 0
    internal var orientationLocked = false
    internal var orientationHorizontal = true
    private var THRESHOLD = 0

    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (App.getInstance().isNightEnabled) {
            setTheme(R.style.ActivityThemeDarkNoAnimation)
        }

        THRESHOLD = Utilities.convertDpToPixel(2f, applicationContext).toInt()

        val isNight = App.getInstance().isNightEnabled
        mActivity = this

        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat)
        setSupportActionBar(binding!!.tbChat)

        if (Flags.DM_IS_NEW) {
            binding!!.txtChatMessage.requestFocus()
            Utilities.showKeyboard(binding!!.txtChatMessage)
        } else {
            binding!!.chatRlMain.requestFocus()
        }

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = resources.getColor(if (isNight)
            R.color.dark_status_bar_timeline_color
        else
            R.color.light_status_bar_timeline_color)

        val viewModel = ChatViewModel(this,
                if (TextUtils.isEmpty(DirectApi.getInstance().userName))
                    DirectApi.getInstance().screenName
                else
                    DirectApi.getInstance().userName, AppData.UI_STATE_LOADING)
        binding!!.model = viewModel
        binding!!.click = object : ChatViewModel.ChatClickHandler {
            override fun onSendClick(v: View) {
                val asyncTwitter = Utilities.getAsyncTwitter()
                asyncTwitter.addListener(object : TwitterAdapter() {
                    override fun onException(te: TwitterException?, method: TwitterMethod?) {
                        super.onException(te, method)
                        Log.e(TAG, "Error sending direct " + te!!.localizedMessage)
                    }
                })

                if (DirectApi.getInstance().userId > -1) {
                    asyncTwitter.sendDirectMessage(DirectApi.getInstance().userId, binding!!.txtChatMessage.text.toString())
                } else {
                    asyncTwitter.sendDirectMessage(DirectApi.getInstance().screenName, binding!!.txtChatMessage.text.toString())
                }
                binding!!.txtChatMessage.setText("")
            }

            override fun onBackClick(v: View) {
                onBackPressed()
            }

            override fun onDestroyClick(v: View) {
                val popupMenu = PopupMenu(mActivity!!, v, Gravity.BOTTOM, 0, R.style.popup_no_overlap_menu)
                val menuInflater = popupMenu.menuInflater
                menuInflater.inflate(R.menu.menu_direct, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.direct_delete -> {
                        }
                    }
                    false
                }

                popupMenu.show()
            }
        }

        DirectApi.getInstance().updateAddHandler = object : UpdateAddHandler {
            override fun onUpdate() {
                viewModel.chatAdapter.notifyDataSetChanged()
                viewModel.state = if (DirectApi.getInstance().chatModels.size > 0)
                    AppData.UI_STATE_VISIBLE
                else
                    AppData.UI_STATE_NO_ITEMS
                viewModel.title = if (TextUtils.isEmpty(DirectApi.getInstance().userName))
                    DirectApi.getInstance().screenName
                else
                    DirectApi.getInstance().userName
                if (DirectApi.getInstance().chatModels.size > 0)
                    binding!!.recyclerChat.scrollToPosition(DirectApi.getInstance().chatModels.size - 1)
            }

            override fun onAdd() {
                viewModel.chatAdapter.notifyDataSetChanged()
                viewModel.state = if (DirectApi.getInstance().chatModels.size > 0)
                    AppData.UI_STATE_VISIBLE
                else
                    AppData.UI_STATE_NO_ITEMS
                if (DirectApi.getInstance().chatModels.size > 0)
                    binding!!.recyclerChat.smoothScrollToPosition(DirectApi.getInstance().chatModels.size - 1)
            }

            override fun onError() {
                viewModel.state = AppData.UI_STATE_NO_ITEMS
            }

            override fun onDelete(position: Int) {

            }
        }

        DirectApi.getInstance().loadData()
        binding!!.chatSendDivider.setBackgroundColor(if (isNight) Color.parseColor("#15191D") else Color.parseColor("#DFE4E7"))
        binding!!.txtChatMessage.setHintTextColor(if (isNight) Color.parseColor("#4DBEC8D2") else Color.parseColor("#4D3D454C"))

        val params = binding!!.recyclerChat.layoutParams as FrameLayout.LayoutParams
        val startMargin = params.leftMargin
        val endMargin = params.rightMargin

        binding!!.recyclerChat.setOnScrollListener(onScrollListener)
        binding!!.recyclerChat.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, event: MotionEvent): Boolean {
                val X = event.rawX.toInt()
                val Y = event.rawY.toInt()
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_DOWN -> {
                        val lParams = binding!!.recyclerChat.layoutParams as FrameLayout.LayoutParams
                        _xDelta = X - lParams.leftMargin
                        _yDelta = Y - lParams.topMargin

                        orientationLocked = false
                        orientationHorizontal = true
                    }

                    MotionEvent.ACTION_MOVE -> return Math.abs(Y - _yDelta) <= Math.abs(X - _xDelta) - THRESHOLD
                }

                return false
            }

            override fun onTouchEvent(rv: RecyclerView, event: MotionEvent) {

            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

            }
        })
        binding!!.recyclerChat.setOnTouchListener(View.OnTouchListener { v, event ->
            val X = event.rawX.toInt()
            val Y = event.rawY.toInt()
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    val lParams = binding!!.recyclerChat.layoutParams as FrameLayout.LayoutParams
                    _xDelta = X - lParams.leftMargin
                    _yDelta = Y - lParams.topMargin
                }

                MotionEvent.ACTION_UP -> {
                    val params = binding!!.recyclerChat.layoutParams as FrameLayout.LayoutParams
                    val oldLeftMargin = params.leftMargin
                    val oldRightMargin = params.rightMargin

                    val a = object : Animation() {
                        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                            val params = binding!!.recyclerChat.layoutParams as FrameLayout.LayoutParams
                            params.leftMargin = oldLeftMargin + (-oldLeftMargin * interpolatedTime).toInt()
                            params.rightMargin = (oldRightMargin * (1 - interpolatedTime) + endMargin * interpolatedTime).toInt()
                            binding!!.recyclerChat.layoutParams = params
                        }
                    }

                    a.duration = 150 // in ms
                    if (oldLeftMargin < 0) {
                        binding!!.recyclerChat.startAnimation(a)
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (Math.abs(Y - _yDelta) > Math.abs(X - _xDelta) || !orientationHorizontal) {
                        orientationHorizontal = false
                        return@OnTouchListener false
                    }

                    orientationLocked = true

                    if (X - _xDelta <= -THRESHOLD) {
                        val alpha = 1.0f + (1.toFloat() - (endMargin - (X - _xDelta))) / endMargin.toFloat()
                        setAlpha(alpha)
                        val layoutParams = binding!!.recyclerChat.layoutParams as FrameLayout.LayoutParams
                        layoutParams.leftMargin = Math.max((X - _xDelta).toFloat(), -Utilities.convertDpToPixel(50f, applicationContext)).toInt()
                        layoutParams.rightMargin = Math.min(endMargin - (X - _xDelta), 0)
                        binding!!.recyclerChat.layoutParams = layoutParams
                    }
                }
            }

            binding!!.root.invalidate()
            false
        })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        finish()
        Utilities.hideKeyboard(mActivity)
        if (Flags.DM_IS_NEW) {
            overridePendingTransition(R.anim.slide_out_no_animation, R.anim.fade_out)
        } else {
            overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left)
        }
    }

    private fun setAlpha(alpha: Float) {
        for (chatModel in DirectApi.getInstance().chatModels) {
            chatModel.alpha = alpha
        }
    }
}
