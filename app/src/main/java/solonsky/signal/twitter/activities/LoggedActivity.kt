package solonsky.signal.twitter.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle

import solonsky.signal.twitter.api.DirectApi
import solonsky.signal.twitter.data.DirectData
import solonsky.signal.twitter.data.FeedData
import solonsky.signal.twitter.data.LikesData
import solonsky.signal.twitter.data.LoggedData
import solonsky.signal.twitter.data.MentionsData
import solonsky.signal.twitter.data.MuteData
import solonsky.signal.twitter.data.NotificationsAllData
import solonsky.signal.twitter.data.NotificationsFollowData
import solonsky.signal.twitter.data.NotificationsLikeData
import solonsky.signal.twitter.data.NotificationsReplyData
import solonsky.signal.twitter.data.NotificationsRetweetData
import solonsky.signal.twitter.data.SearchData
import solonsky.signal.twitter.data.ShareData
import solonsky.signal.twitter.data.StreamData
import solonsky.signal.twitter.data.UsersData
import solonsky.signal.twitter.fragments.MVPMentionsFragment
import solonsky.signal.twitter.fragments.MVPProfileFragment

import android.os.Handler
import android.support.annotation.ColorRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.Toast

import com.anupcowkur.reservoir.Reservoir
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.fatboyindustrial.gsonjodatime.Converters
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.TwitterCore
import com.twitter.sdk.android.core.TwitterSession

import org.joda.time.LocalTime

import java.io.IOException
import java.util.ArrayList
import java.util.Collections

import solonsky.signal.twitter.R
import solonsky.signal.twitter.adapters.SelectorAdapter
import solonsky.signal.twitter.databinding.ActivityLoggedBinding
import solonsky.signal.twitter.fragments.DirectFragment
import solonsky.signal.twitter.fragments.FeedFragment
import solonsky.signal.twitter.fragments.LikesFragment
import solonsky.signal.twitter.fragments.MuteFragment
import solonsky.signal.twitter.fragments.NotificationsFragment
import solonsky.signal.twitter.fragments.SearchFragment
import solonsky.signal.twitter.fragments.SearchedFragment
import solonsky.signal.twitter.helpers.*
import solonsky.signal.twitter.interfaces.ActivityListener
import solonsky.signal.twitter.interfaces.FragmentCounterListener
import solonsky.signal.twitter.interfaces.NotificationListener
import solonsky.signal.twitter.interfaces.UpdateHandler
import solonsky.signal.twitter.libs.ShareContent
import solonsky.signal.twitter.libs.bottomBar.AGBottomBar
import solonsky.signal.twitter.libs.bottomBar.AGBottomBarItem
import solonsky.signal.twitter.libs.bottomBar.AGBottomBarSingleItem
import solonsky.signal.twitter.libs.bottomBar.AGBottomBarMultipleItem
import solonsky.signal.twitter.models.ConfigurationModel
import solonsky.signal.twitter.models.ConfigurationUserModel
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.overlays.AccountSwitcherOverlay
import solonsky.signal.twitter.presenters.LoggedPresenter
import solonsky.signal.twitter.services.MyLocationService
import solonsky.signal.twitter.viewmodels.LoggedViewModel
import solonsky.signal.twitter.views.LoggedView
import twitter4j.ResponseList
import twitter4j.Status
import twitter4j.TwitterAdapter
import twitter4j.TwitterException
import twitter4j.TwitterMethod
import twitter4j.User

class LoggedActivity : MvpAppCompatActivity(), LoggedView, ActivityListener {

    @InjectPresenter
    lateinit var mPresenter: LoggedPresenter

    val requestLocationCode = 1
    private val TAG = "LOGGEDACTIVITY"
    lateinit var binding: ActivityLoggedBinding
    lateinit var viewModel: LoggedViewModel
    private val mAdapter: SelectorAdapter? = null
    private val gson = Gson()

    private val BOTTOM_SHADOW_HEIGHT = 1
    private val BOTTOM_SHADOW_DEFAULT_MARGIN = 47

    private var feedFragment: FeedFragment? = null
    private var mentionsFragment: MVPMentionsFragment? = null
    private var notificationsFragment: NotificationsFragment? = null
    private var likesFragment: LikesFragment? = null
    private var directFragment: DirectFragment? = null
    private var searchFragment: SearchFragment? = null
    private var profileFragment: MVPProfileFragment? = null
    private var muteFragment: MuteFragment? = null

    private var bottomBar: AGBottomBar? = null
    private var lastFragment: Fragment? = null
    private var feedCount = 0
    private var previousTBState = AppData.TOOLBAR_LOGGED_MAIN

    private val gestureDetector = GestureDetector(object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            AppData.isRecreate = true
            App.getInstance().isNightEnabled = !App.getInstance().isNightEnabled
            startActivity(Intent(applicationContext, LoggedActivity::class.java))
            this@LoggedActivity.finish()
            return true
        }
    })

    var shareContent: ShareContent = ShareContent(this@LoggedActivity)

    /**
     * Hides top bar or bottom bar depends on configuration
     *
     * @param isHide - @true for hide
     * @link .models.ConfigurationModel
     */
    private var isDown = false

    fun prepareToRecreate(isChange: Boolean) {
        if (isChange) {
            AppData.lastSwitchTime = System.currentTimeMillis()

            DirectData.setInstance(null)
            FeedData.setInstance(null)
            LikesData.setInstance(null)
            MentionsData.instance.clear()
            MuteData.setInstance(null)
            NotificationsAllData.setInstance(null)
            NotificationsFollowData.setInstance(null)
            NotificationsLikeData.setInstance(null)
            NotificationsRetweetData.setInstance(null)
            NotificationsReplyData.setInstance(null)
            SearchData.setInstance(null)
            UsersData.setInstance(null)
            StreamData.getInstance().endStream()
            StreamData.setInstance(null)
        }

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (feedFragment != null && feedFragment!!.isAdded)
            fragmentTransaction.remove(feedFragment)
        if (mentionsFragment != null && mentionsFragment!!.isAdded)
            fragmentTransaction.remove(mentionsFragment)
        if (notificationsFragment != null && notificationsFragment!!.isAdded)
            fragmentTransaction.remove(notificationsFragment)
        if (likesFragment != null && likesFragment!!.isAdded)
            fragmentTransaction.remove(likesFragment)
        if (searchFragment != null && searchFragment!!.isAdded)
            fragmentTransaction.remove(searchFragment)
        if (directFragment != null && directFragment!!.isAdded)
            fragmentTransaction.remove(directFragment)
        if (muteFragment != null && muteFragment!!.isAdded)
            fragmentTransaction.remove(muteFragment)
        if (profileFragment != null && profileFragment!!.isAdded)
            fragmentTransaction.remove(profileFragment)
        fragmentTransaction.commit()
        System.gc()
    }

    override fun onResume() {
        super.onResume()
        mPresenter.fetchUsers()
    }

    override fun updateCounter(count: Int) {
        viewModel.statusType = if (count > 0)
            Flags.STATUS_TYPE.COUNTER
        else
            Flags.STATUS_TYPE.TITLE
        viewModel.feedCount = count
    }

    override fun updateStatusType(statusType: Flags.STATUS_TYPE) {
        viewModel.statusType = statusType
    }

    override fun updateToolbarState(state: Int, @ColorRes statusBarColor: Int) {
        viewModel.toolbarState = state
        setStatusBarColor(statusBarColor)
    }

    override fun updateBars(dy: Int) {
        hidePopup()
        if (!AppData.appConfiguration.isStaticBottomBar && !viewModel.isStaticBottomBar) {
            var params = binding.agLoggedBottom.layoutParams as RelativeLayout.LayoutParams
            var bottomDiff = params.bottomMargin - dy
            params.bottomMargin = if (bottomDiff >= 0)
                0
            else if (bottomDiff < -binding.agLoggedBottom.height)
                -binding.agLoggedBottom.height
            else
                bottomDiff
            binding.agLoggedBottom.layoutParams = params

            val startPosition = Utilities.convertDpToPixel(47f, applicationContext).toInt()
            params = binding.viewLoggedBottomShadow.layoutParams as RelativeLayout.LayoutParams
            bottomDiff = params.bottomMargin - dy
            params.bottomMargin = when {
                bottomDiff >= startPosition -> startPosition
                bottomDiff < -binding.viewLoggedBottomShadow.height -> -binding.viewLoggedBottomShadow.height
                else -> bottomDiff
            }
            binding.viewLoggedBottomShadow.layoutParams = params
        }

        if (!AppData.appConfiguration.isStaticTopBars && !viewModel.isStaticToolbar) {
            var params = binding.tbLogged.layoutParams as RelativeLayout.LayoutParams
            val searchParams = binding.tbSearch.layoutParams as RelativeLayout.LayoutParams
            val toolbarHeight = Utilities.convertDpToPixel(80f, applicationContext).toInt()
            val dropShadowHeight = Utilities.convertDpToPixel(4f, applicationContext).toInt()
            var topDiff = params.topMargin - dy

            params.topMargin = if (topDiff > 0) 0 else if (topDiff <= -(toolbarHeight + dropShadowHeight)) -(toolbarHeight + dropShadowHeight) else topDiff
            searchParams.topMargin = if (topDiff > 0) 0 else if (topDiff <= -(toolbarHeight + dropShadowHeight)) -(toolbarHeight + dropShadowHeight) else topDiff
            binding.tbLogged.layoutParams = params
            binding.tbSearch.layoutParams = searchParams

            params = binding.viewLoggedDropShadow.layoutParams as RelativeLayout.LayoutParams
            topDiff = params.topMargin - dy
            params.topMargin = if (topDiff > toolbarHeight) toolbarHeight else if (topDiff <= -dropShadowHeight) -dropShadowHeight else topDiff
            binding.viewLoggedDropShadow.layoutParams = params
        }

        isDown = !isDown
    }

    override fun updateSettings(title: Int, isStaticTop: Boolean, isStaticBottom: Boolean) {
        endSearching(false)
        resetBars()
        viewModel.isStaticBottomBar = isStaticBottom
        viewModel.isStaticToolbar = isStaticTop
        viewModel.title = getString(title)
    }

    override fun updateTitle(title: Int) {
        viewModel.title = getString(title)
    }

    override fun onStartSearch(searchQuery: String) {
        Utilities.hideKeyboard(this@LoggedActivity)
        Cache.saveRecentSearch(searchQuery)
        binding.txtLoggedSearch.setText(searchQuery)
        binding.rlLoggedContainer.requestFocus()
        viewModel.isSearch = true

        if (binding.flLoggedSearch.visibility == View.VISIBLE) {
            supportFragmentManager.beginTransaction().replace(R.id.fl_logged_search, SearchedFragment()).commit()
        } else {
            previousTBState = viewModel.toolbarState
            viewModel.toolbarState = AppData.TOOLBAR_LOGGED_SEARCHED
            supportFragmentManager.beginTransaction().replace(R.id.fl_logged_search, SearchedFragment()).commit()

            binding.flLoggedSearch.visibility = View.VISIBLE
            binding.flLoggedSearch.animate().translationX(Utilities.getScreenWidth(this@LoggedActivity).toFloat()).setDuration(0).start()
            binding.flLoggedSearch.animate().translationX(0f).setDuration(200).start()

            binding.flLoggedMain.animate().setDuration(200).alpha(0.7f)
                    .translationX(-(Utilities.getScreenWidth(this@LoggedActivity) * 0.1f))
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            binding.flLoggedMain.visibility = View.GONE
                        }
                    })
        }
    }

    override fun onEndSearch() {

    }

    override fun checkState(): Flags.STATUS_TYPE = viewModel.statusType
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            val gson = Converters.registerLocalDateTime(GsonBuilder()).create()
            Reservoir.init(applicationContext, (50 * 1024 * 1024).toLong(), gson)
            Log.e(TAG, "Cache successfully init")
        } catch (e: IOException) {
            Log.e(TAG, "Failure init cache - " + e.localizedMessage)
        }

        if (savedInstanceState == null) {
            loadConfigurations() // load users configuration

            AppData.lastSwitchTime = System.currentTimeMillis()
            ShareData.getInstance().loadCache() // Load shares

            if (!AppData.isRecreate) {
                when (AppData.appConfiguration.darkMode) {
                    ConfigurationModel.DARK_ALWAYS -> App.getInstance().isNightEnabled = true
                    ConfigurationModel.DARK_AT_NIGHT -> App.getInstance().isNightEnabled = LocalTime().hourOfDay >= 18
                    ConfigurationModel.DARK_OFF -> App.getInstance().isNightEnabled = false
                    else -> App.getInstance().isNightEnabled = false
                }
            } else {
                AppData.isRecreate = false
            }
        }

        if (App.getInstance().isNightEnabled) {
            setTheme(R.style.ActivityThemeDark)
        }

        shareContent = ShareContent(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_logged)
        UsersData.getInstance().init()
        LoggedData.getInstance().loadCache() // Load bullets

        binding.tbLogged.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
        binding.tbLoggedMute.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }

        binding.txtLoggedFeedCount.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> binding.txtLoggedFeedCount.startAnimation(
                        AnimationUtils.loadAnimation(applicationContext, R.anim.scale_down))

                MotionEvent.ACTION_UP -> binding.txtLoggedFeedCount.clearAnimation()
            }
            false
        }

        binding.imgLoggedFeedArrow.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> binding.imgLoggedFeedArrow.startAnimation(
                        AnimationUtils.loadAnimation(applicationContext, R.anim.scale_down))

                MotionEvent.ACTION_UP -> binding.imgLoggedFeedArrow.clearAnimation()
            }
            false
        }

        binding.txtLoggedFeedTitle.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> binding.txtLoggedFeedTitle.startAnimation(
                        AnimationUtils.loadAnimation(applicationContext, R.anim.scale_down))

                MotionEvent.ACTION_UP -> binding.txtLoggedFeedTitle.clearAnimation()
            }
            false
        }

        //        Log.e(TAG, "current name - " + AppData.userConfiguration.getUser().getName());
        //        Log.e(TAG, "current client token - " + AppData.userConfiguration.getClientToken());
        //        Log.e(TAG, "current client secret - " + AppData.userConfiguration.getClientSecret());
        //        Log.e(TAG, "current consumer key - " + AppData.userConfiguration.getConsumerKey());
        //        Log.e(TAG, "current consumer secret - " + AppData.userConfiguration.getConsumerSecret());

        if (AppData.configurationUserModels != null) {
            AppData.CLIENT_TOKEN = AppData.userConfiguration.clientToken
            AppData.CLIENT_SECRET = AppData.userConfiguration.clientSecret
        }

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setStatusBarColor(if (App.getInstance().isNightEnabled)
            R.color.dark_status_bar_timeline_color
        else
            R.color.light_status_bar_timeline_color)

        val switcherOverlay = AccountSwitcherOverlay(this)
        switcherOverlay.createSwitcher()
        viewModel = LoggedViewModel(switcherOverlay.getmAdapter(), applicationContext, feedCount)

        if (AppData.ME != null)
            viewModel.avatar = AppData.ME.originalProfileImageURL

        if (intent != null && intent.extras != null) {
            parseNotification(
                    Integer.valueOf(intent.extras!!.getInt(Flags.NOTIFICATION_ID).toString())!!,
                    intent.extras!!.get(Flags.NOTIFICATION_TYPE),
                    intent.extras!!.get(Flags.NOTIFICATION_USERNAME) as String,
                    java.lang.Long.valueOf(intent.extras!!.get(Flags.NOTIFICATION_STATUS_ID).toString())!!)
        }

        binding.logged = viewModel
        binding.click = object : LoggedViewModel.LoggedClickHandler {
            override fun onComposeClick(v: View) {
                Flags.CURRENT_COMPOSE = Flags.COMPOSE_NONE
                makeCompose()
                hidePopup()
            }

            override fun onUserClick(v: View) {
                switcherOverlay.showOverlay()
                hidePopup()
            }

            override fun onUpdateClick(v: View) {
                makeUpdate()
                hidePopup()
            }

            override fun onCancelClick(v: View) {
                switcherOverlay.hideOverlay()
                hidePopup()
            }

            override fun onAddClick(v: View) {
                if (!viewModel.isAdding) {
                    binding.btnLoggedTwitter.performClick()
                }
                hidePopup()
            }

            override fun onSettingsClick(v: View) {
                if (!viewModel.isAdding) {
                    startActivity(Intent(applicationContext, SettingsActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
                hidePopup()
            }

            override fun onMuteAddClick(v: View) {
                startActivity(Intent(applicationContext, MuteAddActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                hidePopup()
            }

            override fun onDMClick(v: View) {
                startActivity(Intent(applicationContext, ChatSelectActivity::class.java))
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_no_animation)
                hidePopup()
            }

            override fun onBackSearchClick(v: View) {
                endSearching(true)
            }
        }

        initBottomBar()
        initFragments()

        binding.btnLoggedTwitter.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                val handler = Handler()
                viewModel.isAdding = true
                val session = TwitterCore.getInstance().sessionManager.activeSession
                val authToken = session.authToken
                AppData.CLIENT_TOKEN = authToken.token
                AppData.CLIENT_SECRET = authToken.secret

                val asyncTwitter = Utilities.getAsyncTwitter()
                asyncTwitter.addListener(object : TwitterAdapter() {
                    override fun onException(te: twitter4j.TwitterException?, method: TwitterMethod?) {
                        super.onException(te, method)
                        handler.post { Toast.makeText(applicationContext, "Error adding user", Toast.LENGTH_SHORT).show() }
                    }

                    override fun lookedupUsers(users: ResponseList<User>?) {
                        super.lookedupUsers(users)
                        val fileWork = FileWork(applicationContext)

                        /* Save user for next performance */
                        val user = solonsky.signal.twitter.models.User.getFromUserInstance(users!![0])
                        AppData.ME = user
                        fileWork.writeToFile(AppData.ME.id.toString(), FileNames.USERS_LAST_ID)
                        fileWork.writeToFile(authToken.token, FileNames.CLIENT_TOKEN)
                        fileWork.writeToFile(authToken.secret, FileNames.CLIENT_SECRET)

                        /* Create configuration user for notifications and bottom tabs */
                        val hasUser = AppData.configurationUserModels.any { it.user.id == user.id }
                        if (!hasUser) {
                            AppData.userConfiguration = ConfigurationUserModel.getDefaultInstance(user,
                                    AppData.CONSUMER_KEY, AppData.CONSUMER_SECRET, authToken.token, authToken.secret)
                            AppData.configurationUserModels.add(AppData.userConfiguration)
                            ConfigurationUserModel.saveData()
                        }

                        prepareToRecreate(true)
                        FileWork(applicationContext).writeToFile(AppData.ME.id.toString(),
                                FileNames.USERS_LAST_ID)

                        saveProfile()
                        handler.post { this@LoggedActivity.recreate() }
                    }
                })

                Thread(Runnable {
                    try {
                        asyncTwitter.lookupUsers(asyncTwitter.id)
                    } catch (e: twitter4j.TwitterException) {
                        Log.e(TAG, "Error loading user - " + e.localizedMessage)
                    }
                }).start()
            }

            override fun failure(exception: com.twitter.sdk.android.core.TwitterException) {
                Log.e(TAG, "Error adding user - " + exception.localizedMessage)
                Toast.makeText(applicationContext, getString(R.string.error_add_account), Toast.LENGTH_SHORT).show()
            }
        }

        LoggedData.getInstance().updateHandler = UpdateHandler {
            bottomBar!!.setCaptionVisibility(if (LoggedData.getInstance().isNewFeed)
                View.VISIBLE
            else
                View.GONE, 0)
            bottomBar!!.setCaptionVisibility(if (LoggedData.getInstance().isNewMention)
                View.VISIBLE
            else
                View.GONE, 1)
            bottomBar!!.setCaptionVisibility(if (LoggedData.getInstance().isNewActivity)
                View.VISIBLE
            else
                View.GONE, 2)

            bottomBar!!.barItems
                    .asSequence()
                    .filter { it.id == 3 }
                    .forEach {
                        bottomBar!!.setCaptionVisibility(if (LoggedData.getInstance().isNewMessage)
                            View.VISIBLE
                        else
                            View.GONE, bottomBar!!.barItems.indexOf(it))
                    }
        }

        val notificationHelper = NotificationHelper(applicationContext)
        StreamData.getInstance().startStream()
        StreamData.getInstance().notificationListener = object : NotificationListener {
            override fun onCreateLikeNotification(text: String, sender: String, senderScreenName: String,
                                                  receiver: String, avatar: String) {
                notificationHelper.createLikeNotification(text, sender, senderScreenName, receiver, avatar)
            }

            override fun onCreateRetweetNotification(text: String, sender: String, senderScreenName: String,
                                                     receiver: String, avatar: String, statusId: Long) {
                notificationHelper.createRetweetNotification(text, sender, senderScreenName, receiver, avatar, statusId)
            }

            override fun onCreateQuoteNotification(text: String, sender: String, senderScreenName: String,
                                                   receiver: String, avatar: String, statusId: Long) {
                notificationHelper.createQuoteNotification(text, sender, senderScreenName, receiver, avatar, statusId)
            }

            override fun onCreateMentionNotification(text: String, sender: String, senderScreenName: String,
                                                     receiver: String, avatar: String) {
                notificationHelper.createMentionNotification(text, sender, senderScreenName, receiver, avatar)
            }

            override fun onCreateDirectNotitifcation(text: String, sender: String, senderScreenName: String,
                                                     receiver: String, avatar: String) {
                notificationHelper.createDirectNotification(text, sender, senderScreenName, receiver, avatar)
            }

            override fun onCreateReplyNotification(text: String, sender: String, senderScreenName: String,
                                                   receiver: String, avatar: String) {
                notificationHelper.createReplyNotification(text, sender, senderScreenName, receiver, avatar, null)
            }

            override fun onCreateFollowNotification(sender: String, senderScreenName: String, receiver: String, avatar: String) {
                notificationHelper.createFollowNotification(sender, senderScreenName, receiver, avatar)
            }

            override fun onCreateListedNotification(listName: String, sender: String, senderScreenName: String,
                                                    receiver: String, avatar: String) {
                notificationHelper.createListNotification(listName, sender, senderScreenName, receiver, avatar)
            }
        }

        startService(Intent(applicationContext, MyLocationService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        System.gc()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        System.gc()
    }

    /**
     * Reset bars state to default state
     */
    fun resetBars() {
        var params = binding.agLoggedBottom.layoutParams as RelativeLayout.LayoutParams
        params.bottomMargin = 0
        binding.agLoggedBottom.layoutParams = params

        params = binding.viewLoggedBottomShadow.layoutParams as RelativeLayout.LayoutParams
        params.bottomMargin = (Utilities.convertDpToPixel(BOTTOM_SHADOW_DEFAULT_MARGIN.toFloat(), applicationContext) + Utilities.convertDpToPixel(BOTTOM_SHADOW_HEIGHT.toFloat(), applicationContext)).toInt()
        binding.viewLoggedBottomShadow.layoutParams = params

        params = binding.tbLogged.layoutParams as RelativeLayout.LayoutParams
        params.topMargin = 0
        binding.tbLogged.layoutParams = params

        isDown = false
    }

    /**
     * Load user's configurations
     */
    private fun loadConfigurations() {
        val resultType = object : TypeToken<List<ConfigurationUserModel>>() {

        }.type
        try {
            val configurationUserModels = Reservoir.get<List<ConfigurationUserModel>>(Cache.UsersConfigurations, resultType)
            val ids = ArrayList<Long>()
            AppData.configurationUserModels.clear()
            Log.e(TAG, "Me id - " + AppData.ME.id + " Me name - " + AppData.ME.name)
            for (configurationModel in configurationUserModels) {
                if (!ids.contains(configurationModel.user.id)) {
                    AppData.configurationUserModels.add(configurationModel)
                    ids.add(configurationModel.user.id)

                    if (AppData.ME != null && configurationModel.user.id == AppData.ME.id) {
                        AppData.userConfiguration = configurationModel
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error load configurations cache " + e.localizedMessage)
            AppData.userConfiguration = ConfigurationUserModel.getDefaultInstance(AppData.ME,
                    AppData.CONSUMER_KEY, AppData.CONSUMER_SECRET, AppData.CLIENT_TOKEN, AppData.CLIENT_SECRET)
            AppData.configurationUserModels = ArrayList()
            AppData.configurationUserModels.add(AppData.userConfiguration)
        } catch (e: NullPointerException) {
            Log.e(TAG, "Error load configurations cache " + e.localizedMessage)
            AppData.userConfiguration = ConfigurationUserModel.getDefaultInstance(AppData.ME, AppData.CONSUMER_KEY, AppData.CONSUMER_SECRET, AppData.CLIENT_TOKEN, AppData.CLIENT_SECRET)
            AppData.configurationUserModels = ArrayList()
            AppData.configurationUserModels.add(AppData.userConfiguration)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.btnLoggedTwitter.onActivityResult(requestCode, resultCode, data)
    }

    fun saveProfile() {
        val gson = GsonBuilder()
                .setPrettyPrinting()
                .create()
        val fileWork = FileWork(applicationContext)
        fileWork.writeToFile(gson.toJson(AppData.ME), FileNames.USER)
    }

    /**
     * Load cached profile
     */
//    private fun loadProfile() {
//        val jsonParser = JsonParser()
//        val gson = Gson()
//        val fileWork = FileWork(applicationContext)
//        val userString = fileWork.readFromFile(FileNames.USER)
//        if (userString == "") {
//            loadMe()
//        } else {
//            AppData.ME = gson.fromJson(jsonParser.parse(userString), solonsky.signal.twitter.models.User::class.java)
//            Log.e(TAG, "My id - " + AppData.ME.id)
//            Log.e(TAG, "My name - " + AppData.ME.name)
//            Handler().postDelayed({ loadMe() }, 1000)
//        }
//    }

//    private fun loadMe() {
//        val asyncTwitter = Utilities.getAsyncTwitter()
//        asyncTwitter.addListener(object : TwitterAdapter() {
//            override fun lookedupUsers(users: ResponseList<User>?) {
//                super.lookedupUsers(users)
//                AppData.ME = gson.fromJson(gson.toJsonTree(users!![0]), solonsky.signal.twitter.models.User::class.java)
//                AppData.ME.biggerProfileImageURL = users[0].biggerProfileImageURL
//                AppData.ME.originalProfileImageURL = users[0].originalProfileImageURL
//
//                saveProfile()
//                loadConfigurations()
//                viewModel.avatar = AppData.ME.originalProfileImageURL
//            }
//        })
//
//        Thread(Runnable {
//            try {
//                val myId = asyncTwitter.id
//                Log.e(TAG, "loaded id - " + myId)
//                asyncTwitter.lookupUsers(myId)
//            } catch (e: TwitterException) {
//                Log.e(TAG, "error - " + e.localizedMessage)
//            }
//        }).start()
//    }

    override fun onBackPressed() {
        if (viewModel.isSearch) {
            endSearching(true)
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        LoggedData.getInstance().saveCache()
    }

    fun setStatusBarColor(color: Int) {
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = resources.getColor(color)
    }

    /**
     * Scroll feed to top
     */
    private fun makeUpdate() {
        if (lastFragment != null) {
            if (viewModel.statusType == Flags.STATUS_TYPE.COUNTER) {
                if (lastFragment is FeedFragment)
                    viewModel.statusType = Flags.STATUS_TYPE.ARROW
                if (lastFragment is FragmentCounterListener) {
                    (lastFragment as FragmentCounterListener).onUpdate()
                    (lastFragment as FragmentCounterListener).onScrollToTop()
                }
            } else if (viewModel.statusType == Flags.STATUS_TYPE.ARROW) {
                viewModel.statusType = Flags.STATUS_TYPE.COUNTER
                if (lastFragment is FragmentCounterListener) {
                    (lastFragment as FragmentCounterListener).onBackToPosition()
                }
            } else {
                if (lastFragment is FragmentCounterListener) {
                    (lastFragment as FragmentCounterListener).onScrollToTopWithAnimation(250, 500, 250)
                }
            }
        }
    }

    /**
     * Performs end of searching
     *
     * @param isAnimated - @true for animated
     */
    fun endSearching(isAnimated: Boolean) {
        Utilities.hideKeyboard(this@LoggedActivity)

        if (searchFragment != null) {
            if (Flags.isSearchSaved) {
                searchFragment!!.updateData()
            } else {
                searchFragment!!.initPopup()
            }
        }
        this@LoggedActivity.viewModel.toolbarState = previousTBState
        this@LoggedActivity.viewModel.isSearch = false
        this@LoggedActivity.binding.txtLoggedSearch.setText("")
        this@LoggedActivity.binding.txtLoggedSearch.requestFocus()

        if (isAnimated) {
            binding.flLoggedMain.visibility = View.VISIBLE
            binding.flLoggedSearch.animate().setDuration(300).translationX(
                    Utilities.getScreenWidth(this@LoggedActivity).toFloat()).start()
            binding.flLoggedMain.animate().setDuration(300).alpha(1f).translationX(0f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    binding.flLoggedSearch.visibility = View.GONE
                }
            }).start()
        } else {
            binding.flLoggedMain.animate().setDuration(0).alpha(1f).translationX(0f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    binding.flLoggedMain.visibility = View.VISIBLE
                    binding.flLoggedSearch.visibility = View.GONE
                }
            }).start()
        }
    }

    /**
     * Starts activity to compose new tweet
     */
    private fun makeCompose() {
        startActivity(Intent(applicationContext, ComposeActivity::class.java))
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_no_animation)
    }

    /**
     * Hide multiple popup menu
     */
    fun hidePopup() {
        if (bottomBar != null && bottomBar!!.popup != null) bottomBar!!.popup.dismiss()
    }

    /**
     * Init initial fragments to show in
     */
    private fun initFragments() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        if (AppData.userConfiguration != null) {
            when (AppData.userConfiguration.tabPosition) {
                0 -> {
                    if (feedFragment == null)
                        feedFragment = FeedFragment()
                    if (!feedFragment!!.isAdded)
                        fragmentTransaction.add(R.id.fl_logged_main,
                                feedFragment, feedFragment!!.tag)
                    lastFragment = feedFragment
                }

                1 -> {
                    if (mentionsFragment == null)
                        mentionsFragment = MVPMentionsFragment.newInstance()
                    if (!mentionsFragment!!.isAdded)
                        fragmentTransaction.add(R.id.fl_logged_main,
                                mentionsFragment, mentionsFragment!!.tag)
                    lastFragment = mentionsFragment
                }

                2 -> {
                    if (notificationsFragment == null)
                        notificationsFragment = NotificationsFragment()
                    if (!notificationsFragment!!.isAdded)
                        fragmentTransaction.add(R.id.fl_logged_main,
                                notificationsFragment, notificationsFragment!!.tag)
                    lastFragment = notificationsFragment
                }

                3 -> {
                    if (directFragment == null)
                        directFragment = DirectFragment()
                    if (!directFragment!!.isAdded)
                        fragmentTransaction.add(R.id.fl_logged_main,
                                directFragment, directFragment!!.tag)
                    lastFragment = directFragment
                }

                4 -> {
                    if (searchFragment == null)
                        searchFragment = SearchFragment()
                    if (!searchFragment!!.isAdded)
                        fragmentTransaction.add(R.id.fl_logged_main,
                                searchFragment, searchFragment!!.tag)
                    lastFragment = searchFragment
                }

                5 -> {
                    if (muteFragment == null)
                        muteFragment = MuteFragment()
                    if (!muteFragment!!.isAdded)
                        fragmentTransaction.add(R.id.fl_logged_main,
                                muteFragment, muteFragment!!.tag)
                    lastFragment = muteFragment
                }

                6 -> {
                    if (profileFragment == null)
                        profileFragment = MVPProfileFragment()
                    if (!profileFragment!!.isAdded)
                        fragmentTransaction.add(R.id.fl_logged_main,
                                profileFragment, profileFragment!!.tag)
                    lastFragment = profileFragment
                }

                7 -> {
                    if (likesFragment == null)
                        likesFragment = LikesFragment()
                    if (!likesFragment!!.isAdded)
                        fragmentTransaction.add(R.id.fl_logged_main,
                                likesFragment, likesFragment!!.tag)
                    lastFragment = likesFragment
                }
            }
        } else {
            if (feedFragment == null)
                feedFragment = FeedFragment()
            if (!feedFragment!!.isAdded)
                fragmentTransaction.add(R.id.fl_logged_main,
                        feedFragment, feedFragment!!.tag)
            lastFragment = feedFragment
        }

        fragmentTransaction.commit()
        viewModel.toolbarState = AppData.TOOLBAR_LOGGED_MAIN
    }

    /**
     * Init bottom bar with properly count of items and options
     */
    private fun initBottomBar() {
        bottomBar = findViewById<View>(R.id.ag_logged_bottom) as AGBottomBar
        bottomBar!!.setActivity(this)

        if (AppData.userConfiguration != null) {
            bottomBar!!.setCURRENT_TAB_ID(AppData.userConfiguration.tabPosition)
        }

        val bottomBarItem = AGBottomBarSingleItem(0, false, true, R.color.light_bar_inactive_color,
                R.drawable.ic_tabbar_icons_home)
        val bottomBarItem1 = AGBottomBarSingleItem(1, false, true, R.color.light_bar_inactive_color,
                R.drawable.ic_tabbar_icons_mentions)
        val bottomBarItem2 = AGBottomBarSingleItem(2, false, true, R.color.light_bar_inactive_color,
                R.drawable.ic_tabbar_icons_activity)

        val bottomBarItem3 = AGBottomBarMultipleItem(3, false,
                AppData.userConfiguration.bottomIds.contains(3),
                if (AppData.userConfiguration.bottomIds.contains(3))
                    R.drawable.ic_tabbar_icons_messages_active
                else
                    R.drawable.ic_tabbar_icons_messages,
                R.drawable.ic_tabbar_icons_more)
        val bottomBarItem4 = AGBottomBarMultipleItem(4, false,
                AppData.userConfiguration.bottomIds.contains(4),
                R.drawable.ic_tabbar_icons_search, R.drawable.ic_tabbar_icons_more)
        val bottomBarItem5 = AGBottomBarMultipleItem(5, false,
                AppData.userConfiguration.bottomIds.contains(5),
                R.drawable.ic_tabbar_icons_mute, R.drawable.ic_tabbar_icons_more)
        val bottomBarItem6 = AGBottomBarMultipleItem(6, false,
                AppData.userConfiguration.bottomIds.contains(6),
                R.drawable.ic_tabbar_icons_profile, R.drawable.ic_tabbar_icons_more)
        val bottomBarItem7 = AGBottomBarMultipleItem(7, false,
                AppData.userConfiguration.bottomIds.contains(7),
                R.drawable.ic_tabbar_icons_likes, R.drawable.ic_tabbar_icons_more)

        val items = ArrayList<AGBottomBarItem>()
        items.add(bottomBarItem)
        items.add(bottomBarItem1)
        items.add(bottomBarItem2)
        items.add(bottomBarItem3)
        items.add(bottomBarItem4)
        items.add(bottomBarItem5)
        items.add(bottomBarItem6)
        items.add(bottomBarItem7)

        var firstPosition = 0
        var secondPosition = 1

        for (agBottomBarItem in items) {
            if (agBottomBarItem.id == AppData.userConfiguration.bottomIds[3])
                firstPosition = items.indexOf(agBottomBarItem)
            if (agBottomBarItem.id == AppData.userConfiguration.bottomIds[4])
                secondPosition = items.indexOf(agBottomBarItem)
        }

        Collections.swap(items, firstPosition, 3)
        Collections.swap(items, secondPosition, 4)

        for (agBottomBarItem in items) {
            if (agBottomBarItem is AGBottomBarMultipleItem)
                bottomBar!!.addItem(agBottomBarItem)
            if (agBottomBarItem is AGBottomBarSingleItem)
                bottomBar!!.addItem(agBottomBarItem)
        }

        bottomBar!!.setAgPopup { position, id ->
            AppData.userConfiguration.bottomIds[position] = id
            ConfigurationUserModel.saveCache()
            bottomBar!!.setCaptionVisibility(View.GONE, 3)
            bottomBar!!.setCaptionVisibility(View.GONE, 4)

            if (id == 3) {
                LoggedData.getInstance().updateHandler.onUpdate()
            }
        }

        bottomBar!!.setAgHandler { view, id ->
            if (id != 6) AppData.userConfiguration.tabPosition = id
            when (id) {
                0 -> {
                    LoggedData.getInstance().isNewFeed = false
                    bottomBar!!.updateIcon(3, R.drawable.ic_tabbar_icons_messages)
                    binding.agLoggedBottom.setCaptionVisibility(View.GONE, 0)
                    if (feedFragment == null) feedFragment = FeedFragment()
                    showFragment(feedFragment)
                }

                1 -> {
                    LoggedData.getInstance().isNewMention = false
                    bottomBar!!.updateIcon(3, R.drawable.ic_tabbar_icons_messages)
                    binding.agLoggedBottom.setCaptionVisibility(View.GONE, 1)
                    if (mentionsFragment == null) mentionsFragment = MVPMentionsFragment.newInstance()
                    showFragment(mentionsFragment)
                }

                2 -> {
                    LoggedData.getInstance().isNewActivity = false
                    bottomBar!!.updateIcon(3, R.drawable.ic_tabbar_icons_messages)
                    binding.agLoggedBottom.setCaptionVisibility(View.GONE, 2)
                    if (notificationsFragment == null)
                        notificationsFragment = NotificationsFragment()
                    showFragment(notificationsFragment)
                }

                3 -> {
                    bottomBar!!.updateIcon(3, R.drawable.ic_tabbar_icons_messages_active)
                    if (directFragment == null) directFragment = DirectFragment()
                    showFragment(directFragment)
                }

                4 -> {
                    bottomBar!!.updateIcon(3, R.drawable.ic_tabbar_icons_messages)
                    if (searchFragment == null) searchFragment = SearchFragment()
                    showFragment(searchFragment)
                }

                5 -> {
                    bottomBar!!.updateIcon(3, R.drawable.ic_tabbar_icons_messages)
                    if (muteFragment == null) muteFragment = MuteFragment()
                    showFragment(muteFragment)
                }

                6 -> {
                    bottomBar!!.updateIcon(3, R.drawable.ic_tabbar_icons_messages)
                    if (profileFragment == null) profileFragment = MVPProfileFragment()
                    showFragment(profileFragment)
                }

                7 -> {
                    bottomBar!!.updateIcon(3, R.drawable.ic_tabbar_icons_messages)
                    if (likesFragment == null) likesFragment = LikesFragment()
                    showFragment(likesFragment)
                }
            }

            ConfigurationUserModel.saveCache()
            hidePopup()
        }

        bottomBar!!.build()
    }

    fun showFragment(fragment: Fragment?) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (lastFragment != null) fragmentTransaction.hide(lastFragment)
        if (!fragment!!.isAdded) {
            fragmentTransaction.add(R.id.fl_logged_main, fragment)
        } else {
            fragmentTransaction.show(fragment)
        }

        fragmentTransaction.commit()
        lastFragment = fragment
    }

    private fun parseNotification(notificationId: Int, input: Any?, sender: String, statusId: Long) {
        if (notificationId > 0) {
            val mNotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.cancel(applicationContext.getString(R.string.app_name), notificationId)
        }

        if (input == Flags.NotificationTypes.FAV || input == Flags.NotificationTypes.UNDEFINED
                || input == Flags.NotificationTypes.FOLLOW || input == Flags.NotificationTypes.MENTION) {
            val profileIntent = Intent(applicationContext, MVPProfileActivity::class.java)
            profileIntent.putExtra(Flags.PROFILE_SCREEN_NAME, sender)
            startActivity(profileIntent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } else if (input == Flags.NotificationTypes.DIRECT) {
            DirectApi.getInstance().clear()
            DirectApi.getInstance().screenName = sender
            startActivity(Intent(applicationContext, ChatActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        } else if (input == Flags.NotificationTypes.RT || input == Flags.NotificationTypes.QUOTED) {
            val handler = Handler()
            val asyncTwitter = Utilities.getAsyncTwitter()
            asyncTwitter.addListener(object : TwitterAdapter() {
                override fun gotShowStatus(status: Status?) {
                    super.gotShowStatus(status)
                    val statusModel = Gson().fromJson(Gson().toJsonTree(status), StatusModel::class.java)
                    statusModel.tuneModel(status)
                    statusModel.linkClarify()

                    handler.post {
                        if (input == Flags.NotificationTypes.RT) {
                            AppData.CURRENT_STATUS_MODEL = statusModel.retweetedStatus
                        } else {
                            AppData.CURRENT_STATUS_MODEL = statusModel
                        }
                        startActivity(Intent(applicationContext, DetailActivity::class.java))
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                }
            })

            asyncTwitter.showStatus(statusId)
        } else if (input == Flags.NotificationTypes.REPLY) {
            Flags.CURRENT_COMPOSE = Flags.COMPOSE_MENTION
            AppData.COMPOSE_MENTION = "@" + sender
            startActivity(Intent(applicationContext, ComposeActivity::class.java))
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_no_animation)
        }
    }

    fun setFeedCount(feedCount: Int) {
        this.feedCount = feedCount
    }

    // MARK: - View implementation
    override fun showMessage(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }
}
