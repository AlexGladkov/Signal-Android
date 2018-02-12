package solonsky.signal.twitter.views

import android.os.Bundle

/**
 * Created by agladkov on 25.12.17.
 * Callback from fragment to activity
 */
interface FragmentView {
    fun exitApp()
    fun updateActionBar(isBack: Boolean, isHistory: Boolean, title: String)
    fun navigateTo(screenKey: String, data: Bundle?)
}
