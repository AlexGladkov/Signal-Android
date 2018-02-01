package solonsky.signal.twitter.presenters

import android.content.Intent
import android.util.Log
import com.anupcowkur.reservoir.Reservoir
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig
import com.fatboyindustrial.gsonjodatime.Converters
import com.google.gson.GsonBuilder
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import solonsky.signal.twitter.activities.LoggedActivity
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.FileNames
import solonsky.signal.twitter.models.ConfigurationModel
import solonsky.signal.twitter.providers.SplashProvider
import solonsky.signal.twitter.views.SplashView
import java.io.IOException

/**
 * Created by agladkov on 01.02.18.
 */
@InjectViewState
class SplashPresenter: MvpPresenter<SplashView>() {
    private val provider = SplashProvider(presenter = this)

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
    }

    fun initState() {
        provider.fetchProfile()
    }

    fun setupSettings(model: ConfigurationModel) {
        AppData.appConfiguration = model
        App.getInstance().isNightEnabled = AppData.appConfiguration.darkMode == ConfigurationModel.DARK_ALWAYS
        viewState.performLogged()
    }

    fun reloadSettings() {
        viewState.setupSettings()
    }

    fun saveSettings(settings: ConfigurationModel) {
        provider.saveSettings(settings = settings)
        setupSettings(settings)
    }

    fun setupProfile() {
        provider.fetchSettings()
    }
}