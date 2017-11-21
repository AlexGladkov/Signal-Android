package solonsky.signal.twitter.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import solonsky.signal.twitter.R
import solonsky.signal.twitter.helpers.App
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.views.SearchView

/**
 * Created by neura on 01.11.17.
 */

@InjectViewState
class SearchPresenter: MvpPresenter<SearchView>() {
    val TAG: String = MediaPresenter::class.java.simpleName

    fun paintStatusBar() {
        if (App.getInstance().isNightEnabled) {
            viewState.updateStatusBarColor(R.color.dark_status_bar_timeline_color)
        } else {
            viewState.updateStatusBarColor(R.color.light_status_bar_timeline_color)
        }
    }

    fun startSearch() {
        viewState.setupSearch(AppData.searchQuery)
        viewState.showFragment()
    }
}