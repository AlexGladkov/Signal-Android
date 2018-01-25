package solonsky.signal.twitter.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.PopupMenu
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.arellomobile.mvp.MvpActivity
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import kotlinx.android.synthetic.main.activity_mvp_search.*
import solonsky.signal.twitter.R
import kotlinx.android.synthetic.main.activity_search.*
import okhttp3.internal.Util
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.SupportFragmentNavigator
import ru.terrakok.cicerone.commands.Replace
import solonsky.signal.twitter.fragments.*
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.Keys
import solonsky.signal.twitter.helpers.ScreenKeys
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.presenters.SearchPresenter
import solonsky.signal.twitter.views.SearchView
import javax.inject.Inject

/**
 * Created by neura on 01.11.17.
 */
class MVPSearchActivity : MvpAppCompatActivity(), SearchView, SmartTabLayout.TabProvider {
    private val TAG = MVPSearchActivity::class.java.simpleName
    private var currentPosition = 0

    @InjectPresenter
    lateinit var mPresenter: SearchPresenter

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    private var searchedFragment: SearchedFragment = SearchedFragment()
    private var navigator: Navigator = object : SupportFragmentNavigator(supportFragmentManager, R.id.fl_search) {
        override fun exit() {
            finish()
        }

        override fun createFragment(screenKey: String, data: Any?): Fragment?  {
            return if (data != null && data is Bundle) {
                when (screenKey) {
                    ScreenKeys.SearchAll.value -> SearchAllFragment.getNewInstance(data)
                    ScreenKeys.SearchHome.value -> SearchAllFragment.getNewInstance(data)
                    ScreenKeys.SearchMedia.value -> SearchAllFragment.getNewInstance(data)
                    ScreenKeys.SearchUsers.value -> SearchPeopleFragment.getNewInstance(data)
                    else -> SearchAllFragment()
                }
            } else {
                SearchAllFragment()
            }
        }

        override fun showSystemMessage(message: String) {
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        App.getInstance().appComponent.inject(this@MVPSearchActivity)
        super.onCreate(savedInstanceState)
        if (App.getInstance().isNightEnabled) {
            setTheme(R.style.ActivityThemeDarkNoAnimation)
        }
        setContentView(R.layout.activity_mvp_search)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        Utilities.setWindowFlag(this@MVPSearchActivity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = resources.getColor(if (App.getInstance().isNightEnabled)
            R.color.dark_status_bar_timeline_color
        else
            R.color.light_status_bar_timeline_color)

        stb_search.setCustomTabView(this@MVPSearchActivity)
        stb_search.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mPresenter.switchPage(position = position)

                val oldTab = stb_search.getTabAt(currentPosition)
                val currentTab = stb_search.getTabAt(position)

                val oldView = oldTab.findViewById<View>(R.id.tab_layout) as RelativeLayout
                val currentView = currentTab.findViewById<View>(R.id.tab_layout) as RelativeLayout

                oldView.background = resources.getDrawable(R.drawable.tab_shape_transparent)
                currentView.background = resources.getDrawable(if (App.getInstance().isNightEnabled)
                    R.drawable.tab_shape_dark
                else
                    R.drawable.tab_shape_light)

                currentPosition = position
            }
        })

        txtSearchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mPresenter.startSearch(query = txtSearchInput.text.toString())
                Utilities.hideKeyboard(this@MVPSearchActivity)
            }
            false
        }

        btnSearchBack.setOnClickListener { mPresenter.onBackClick() }
        btnSearchMore.setOnClickListener { mPresenter.onMoreClick() }

        if (savedInstanceState == null) {
            val bundle = Bundle()
            bundle.putBoolean(Keys.SearchLoaded.value, false)
            navigator.applyCommands(arrayOf(Replace(ScreenKeys.SearchAll.value, bundle)))
        }

        val adapter = FragmentPagerItemAdapter(
                supportFragmentManager, FragmentPagerItems.with(this@MVPSearchActivity)
                .add("All", DummyFragment::class.java)
                .add("3", DummyFragment::class.java)
                .add("7", DummyFragment::class.java)
                .add("5", DummyFragment::class.java)
                .create())

        vp_search.adapter = adapter
        stb_search.setViewPager(vp_search)

        mPresenter.fetchRequest(intent = intent)
    }

    override fun onBackPressed() {
        mPresenter.onBackClick()
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    // MARK: - View implementation
    override fun updateStatusBarColor(color: Int) {
        window.statusBarColor = resources.getColor(color)
    }

    override fun showFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.fl_search_main, searchedFragment)
                .commitAllowingStateLoss()
    }

    override fun showMessage(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    override fun setupSearch(query: String) {
        txtSearchInput.setText(query)
        flSearchMain.requestFocus()
    }

    lateinit var popupMenuAll: PopupMenu
    override fun openPopup(resource: Int) {
        popupMenuAll = PopupMenu(this@MVPSearchActivity, btnSearchMore, 0, 0, R.style.popup_menu)
        val menuInflater = popupMenuAll.menuInflater
        menuInflater.inflate(resource, popupMenuAll.menu)

        mPresenter.tunePopup(popupMenu = popupMenuAll)

        popupMenuAll.show()
    }

    override fun performExit() {
        finish()
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left)
    }

    // MARK: - TabProvider implementation
    override fun createTabView(container: ViewGroup, position: Int, adapter: PagerAdapter): View {
        val inflater = LayoutInflater.from(container.context)
        val res = container.context.resources
        val tab = inflater.inflate(R.layout.tab_search_item, container, false)
        val imageView = tab.findViewById<View>(R.id.tab_iv) as ImageView
        val layoutView = tab.findViewById<View>(R.id.tab_layout) as RelativeLayout

        layoutView.background = if (currentPosition == position)
            res.getDrawable(if (App.getInstance().isNightEnabled)
                R.drawable.tab_shape_dark
            else
                R.drawable.tab_shape_light)
        else
            res.getDrawable(R.drawable.tab_shape_transparent)

        val isNightEnabled = App.getInstance().isNightEnabled
        when (position) {
            0 -> {
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_search_all))
                imageView.setColorFilter(res.getColor(if (isNightEnabled)
                    R.color.dark_profile_tint_color
                else
                    R.color.light_profile_tint_color))
            }

            1 -> {
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_search_user))
                imageView.setColorFilter(res.getColor(if (isNightEnabled)
                    R.color.dark_profile_tint_color
                else
                    R.color.light_profile_tint_color))
            }

            2 -> {
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_search_media))
                imageView.setColorFilter(res.getColor(if (isNightEnabled)
                    R.color.dark_profile_tint_color
                else
                    R.color.light_profile_tint_color))
            }

            3 -> {
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_search_feed))
                imageView.setColorFilter(res.getColor(if (App.getInstance().isNightEnabled)
                    R.color.dark_profile_tint_color
                else
                    R.color.light_profile_tint_color))
            }
        }

        return tab
    }
}