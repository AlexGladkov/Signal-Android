package solonsky.signal.twitter.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.anupcowkur.reservoir.Reservoir
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
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
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.FileNames
import solonsky.signal.twitter.helpers.FileWork
import solonsky.signal.twitter.models.ConfigurationModel
import solonsky.signal.twitter.presenters.SplashPresenter
import solonsky.signal.twitter.views.SplashView
import java.io.IOException

/**
 * Created by agladkov on 01.02.18.
 */
class SplashActivity: MvpAppCompatActivity(), SplashView {
    private val TAG: String = SplashActivity::class.java.simpleName

    @InjectPresenter
    lateinit var mPresenter: SplashPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            mPresenter.cleanBoot()
        }
    }

    // View implementation
    override fun performLogin() {
        startActivity(Intent(applicationContext, LoginActivity::class.java))
        finish()
    }

    override fun performLogged() {
        startActivity(Intent(applicationContext, LoggedActivity::class.java))
        finish()
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