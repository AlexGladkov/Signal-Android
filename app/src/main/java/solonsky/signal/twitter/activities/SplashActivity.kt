package solonsky.signal.twitter.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.anupcowkur.reservoir.Reservoir
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig
import com.fatboyindustrial.gsonjodatime.Converters
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import kotlinx.android.synthetic.main.activity_splash.*
import moxy.MvpAppCompatActivity
import moxy.presenter.InjectPresenter
import solonsky.signal.twitter.R
import solonsky.signal.twitter.helpers.*
import solonsky.signal.twitter.models.ConfigurationModel
import solonsky.signal.twitter.presenters.SplashPresenter
import solonsky.signal.twitter.views.SplashView
import java.io.IOException
import java.util.*

/**
 * Created by agladkov on 01.02.18.
 */
class SplashActivity: MvpAppCompatActivity(), SplashView {
    private val TAG: String = SplashActivity::class.java.simpleName

    @InjectPresenter
    lateinit var mPresenter: SplashPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter.initState()

        if (App.getInstance().isNightEnabled) {
            setTheme(R.style.ActivityThemeDark)
        } else {
            setTheme(R.style.ActivityThemeLight)
        }

        setContentView(R.layout.activity_splash)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = resources.getColor(android.R.color.transparent)

        try {
            val imagePipelineConfig = ImagePipelineConfig.newBuilder(this)
                    .setProgressiveJpegConfig(SimpleProgressiveJpegConfig())
                    .setResizeAndRotateEnabledForNetwork(true)
                    .setDownsampleEnabled(true)
                    .build()
            Fresco.initialize(this, imagePipelineConfig)
            Log.e(TAG, "Fresco successfully init")
        } catch (e: Exception) {
            Log.e(TAG, "Fresco wasnt init " + e.localizedMessage)
        }


        try {
            val gson = Converters.registerLocalDateTime(GsonBuilder()).create()
            Reservoir.init(applicationContext, (50 * 1024 * 1024).toLong(), gson)
            Log.e(TAG, "Cache successfully init")
        } catch (e: IOException) {
            Log.e(TAG, "Failure init cache - " + e.localizedMessage)
        }

        val config = TwitterConfig.Builder(this)
                .logger(DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(TwitterAuthConfig(AppData.CONSUMER_KEY, AppData.CONSUMER_SECRET))
                .debug(true)
                .build()
        Twitter.initialize(config)

        val fileWork = FileWork(applicationContext)
        if (fileWork.readFromFile(FileNames.CLIENT_SECRET) != "" && fileWork.readFromFile(FileNames.CLIENT_TOKEN) != "") {
            AppData.CLIENT_TOKEN = fileWork.readFromFile(FileNames.CLIENT_TOKEN)
            AppData.CLIENT_SECRET = fileWork.readFromFile(FileNames.CLIENT_SECRET)
            mPresenter.initState()
        } else {
            mPresenter.performCleanBoot()
        }
    }

    // View implementation
    override fun performLogin() {
        startActivity(Intent(applicationContext, LoginActivity::class.java))
        finish()
    }

    override fun performLogged() {
        startActivity(Intent(applicationContext, LoggedActivity::class.java))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    override fun setupLocale(newLocale: String) {
        val locale = Locale(newLocale)
        val config = Configuration()

        Locale.setDefault(locale)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun setupSettings() {
        val fileWork = FileWork(applicationContext)
        val loadedConfig = fileWork.readFromFile(FileNames.APP_CONFIGURATION)

        if (loadedConfig == "") {
            mPresenter.saveSettings(settings = ConfigurationModel.defaultSettings())
        } else {
            val jsonParser = JsonParser()
            val jsonObject = jsonParser.parse(loadedConfig) as JsonObject
            mPresenter.saveSettings(settings = ConfigurationModel.createFromJson(jsonObject))
        }
    }
}