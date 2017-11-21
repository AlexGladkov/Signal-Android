package solonsky.signal.twitter.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import com.arellomobile.mvp.MvpActivity
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import solonsky.signal.twitter.R
import kotlinx.android.synthetic.main.activity_search.*
import solonsky.signal.twitter.fragments.SearchedFragment
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.presenters.SearchPresenter
import solonsky.signal.twitter.views.SearchView

/**
 * Created by neura on 01.11.17.
 */
class MVPSearchActivity: MvpAppCompatActivity(), SearchView {
    private val TAG = MVPSearchActivity::class.java.simpleName

    @InjectPresenter
    lateinit var mPresenter: SearchPresenter

    private var searchedFragment: SearchedFragment = SearchedFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (App.getInstance().isNightEnabled) {
            setTheme(R.style.ActivityThemeDark)
        }
        setContentView(R.layout.activity_search)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        Utilities.setWindowFlag(this@MVPSearchActivity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)

        mPresenter.paintStatusBar()
        mPresenter.startSearch()
    }

    override fun updateStatusBarColor(color: Int) {
        window.statusBarColor = resources.getColor(color)
    }

    override fun showFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.fl_search_main, searchedFragment)
                .commitAllowingStateLoss()
    }

    override fun setupSearch(query: String) {
        txt_search_input.setText(query)
        fl_search_main.requestFocus()
    }
}