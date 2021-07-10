package solonsky.signal.twitter.activities

import android.view.View
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import solonsky.signal.twitter.adapters.SettingsAdapter
import solonsky.signal.twitter.models.SettingsModel
import solonsky.signal.twitter.models.SettingsSwitchModel
import solonsky.signal.twitter.models.SettingsTextModel
import solonsky.signal.twitter.presenters.ConfigurationsPresenter
import solonsky.signal.twitter.views.ConfigurationView

/**
 * Created by neura on 23.05.17.
 */

class AdvancedActivity : MvpAppCompatActivity, ConfigurationView {
    private val TAG = AdvancedActivity::class.java.simpleName
    private var mSettingsList: ArrayList<SettingsModel>? = null
    private var mAdapter: SettingsAdapter? = null

    @InjectPresenter
    lateinit var presenter: ConfigurationsPresenter

    private val textClickListener = object : SettingsAdapter.SettingsClickListener {
        override fun onItemClick(model: SettingsTextModel, v: View) {

        }

        override fun onSwitchClick(model: SettingsSwitchModel, v: View) {
            presenter.updateAppSettings(AppData.appConfiguration)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (App.getInstance().isNightEnabled) {
            setTheme(R.style.ActivityThemeDarkNoAnimation)
        }

        val binding = DataBindingUtil.setContentView<ActivityAdvancedBinding>(this, R.layout.activity_advanced)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = ContextCompat.getColor(applicationContext, if (App.getInstance().isNightEnabled)
            android.R.color.transparent
        else
            R.color.light_status_bar_timeline_color)

        mSettingsList = ArrayList()

        //        mSettingsList.add(new SettingsSwitchModel(0, getString(R.string.settings_advanced_dim_media), AppData.appConfiguration.isDimMediaAtNight()));
        //        mSettingsList.add(new SettingsSwitchModel(1, getString(R.string.settings_advanced_group_push), AppData.appConfiguration.isGroupPushNotifications()));
        //        mSettingsList.add(new SettingsSwitchModel(2, getString(R.string.settings_advanced_pin_top), AppData.appConfiguration.isPinToTopOnStreaming()));
        mSettingsList!!.add(SettingsSwitchModel(3, getString(R.string.settings_advanced_sounds), AppData.appConfiguration.isSounds))

        when (AppData.appConfiguration.locale) {
            Locales.English.value -> txtAdvancedCurrentLanguage.text = Locales.EnglishName.value
            Locales.Russian.value -> txtAdvancedCurrentLanguage.text = Locales.RussianName.value
        }

        mAdapter = SettingsAdapter(mSettingsList, applicationContext, textClickListener, true)
        flLanguage.setOnClickListener {
            val popupMenu = PopupMenu(this@AdvancedActivity, flLanguage, 0, 0, R.style.popup_menu)
            val menuInflater = popupMenu.menuInflater
            menuInflater.inflate(R.menu.menu_advanced_language, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener({ item ->
                when(item.itemId) {
                    R.id.advanced_english -> {
                        if (AppData.oldLocale == Locales.English.value) {
                            AppData.newLocale = ""
                        } else {
                            AppData.newLocale = Locales.English.value
                            AppData.appConfiguration.locale = Locales.English.value
                            txtAdvancedCurrentLanguage.text = Locales.EnglishName.value
                        }
                    }

                    R.id.advanced_russian -> {
                        if (AppData.oldLocale == Locales.Russian.value) {
                            AppData.newLocale = ""
                        } else {
                            AppData.newLocale = Locales.Russian.value
                            AppData.appConfiguration.locale = Locales.Russian.value
                            txtAdvancedCurrentLanguage.text = Locales.RussianName.value
                        }
                    }
                }

                presenter.updateAppSettings(AppData.appConfiguration)
                false
            })
            popupMenu.show()
        }

        val viewModel = AdvancedViewModel(mAdapter, applicationContext)
        binding.model = viewModel
        binding.click = object : AdvancedViewModel.AdvancedClickHandler {
            override fun onDefaultClick(v: View) {
                AppData.appConfiguration = ConfigurationModel.defaultSettings()
                FileWork(applicationContext).writeToFile(AppData.appConfiguration.exportConfiguration().toString(), FileNames.APP_CONFIGURATION)
                Toast.makeText(applicationContext, getString(R.string.success_reseted), Toast.LENGTH_SHORT).show()
            }

            override fun onClearClick(v: View) {

            }

            override fun onBackClick(v: View) {
                onBackPressed()
            }

            override fun onCacheClick(v: View) {
                try {
                    Reservoir.clear()
                    val fileWork = FileWork(applicationContext)
                    fileWork.writeToFile("", FileNames.CLIENT_SECRET)
                    fileWork.writeToFile("", FileNames.CLIENT_TOKEN)
                    Toast.makeText(applicationContext, R.string.success_cache_clear, Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    Log.e(TAG, "Error clearing cache " + e.localizedMessage)
                }

            }
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left)
    }

    // MARK: - View implementation
    override fun settingsUpdated() {

    }

}
