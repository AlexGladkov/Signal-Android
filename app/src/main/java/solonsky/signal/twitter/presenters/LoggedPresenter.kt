package solonsky.signal.twitter.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import solonsky.signal.twitter.providers.LoggedProvider
import solonsky.signal.twitter.views.LoggedView

/**
 * Created by sunwi on 22.01.2018.
 */
@InjectViewState
class LoggedPresenter: MvpPresenter<LoggedView>() {
    private val provider = LoggedProvider(presenter = this@LoggedPresenter)

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
//        provider.fetchUsers()
    }

    fun fetchUsers() {
        provider.fetchUsers()
    }

    fun showMessage(text: String) {
        viewState.showMessage(text = text)
    }
}