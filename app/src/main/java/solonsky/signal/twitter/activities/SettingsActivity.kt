package solonsky.signal.twitter.activities

import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder

import javax.inject.Inject

import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.commands.Back
import ru.terrakok.cicerone.commands.Forward
import ru.terrakok.cicerone.commands.Replace
import ru.terrakok.cicerone.result.ResultListener
import solonsky.signal.twitter.R
import solonsky.signal.twitter.databinding.ActivitySettingsBinding
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.Codes
import solonsky.signal.twitter.helpers.Locales
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.viewmodels.SettingsViewModel

/**
 * Created by neura on 23.05.17.
 */

class SettingsActivity : AppCompatActivity() {
    private var viewModel: SettingsViewModel? = null
    private val TAG: String = SettingsActivity::class.java.simpleName

    @Inject
    lateinit var routerActivity: Router

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    private val navigator = Navigator { command ->
        when (command) {
            is Back -> {
                finish()
                overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        App.getInstance().appComponent.inject(this)
        super.onCreate(savedInstanceState)
        if (App.getInstance().isNightEnabled) {
            setTheme(R.style.ActivityThemeDarkNoAnimation)
        }

        val binding = DataBindingUtil.setContentView<ActivitySettingsBinding>(this, R.layout.activity_settings)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = resources.getColor(if (App.getInstance().isNightEnabled)
            android.R.color.transparent
        else
            R.color.light_status_bar_timeline_color)

        viewModel = SettingsViewModel()

        binding.model = viewModel
        binding.click = object : SettingsViewModel.SettingsClickHandler {
            override fun onAppearanceClick(v: View) {
                startActivity(Intent(applicationContext, AppearanceActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onTimelineClick(v: View) {
                startActivity(Intent(applicationContext, TimelineActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onGesturesClick(v: View) {
                startActivity(Intent(applicationContext, GesturesActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onNotificationsClick(v: View) {
                startActivity(Intent(applicationContext, UsersListActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onAdvancedClick(v: View) {
                startActivity(Intent(applicationContext, AdvancedActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onHelpClick(v: View) {
                val intent = Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse(getString(R.string.help_link)))
                startActivity(intent)
            }

            override fun onAboutClick(v: View) {
                startActivity(Intent(applicationContext, AboutActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onSupportClick(v: View) {
                startActivity(Intent(applicationContext, SupportActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onBackClick(v: View) {
                onBackPressed()
            }
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()

            else -> {
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left)
    }
}
