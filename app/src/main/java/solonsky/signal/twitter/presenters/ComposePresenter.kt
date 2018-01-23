package solonsky.signal.twitter.presenters

import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import solonsky.signal.twitter.models.User
import solonsky.signal.twitter.models.UserModel
import solonsky.signal.twitter.providers.ComposeProvider
import solonsky.signal.twitter.views.ComposeView

/**
 * Created by sunwi on 23.01.2018.
 */
@InjectViewState
class ComposePresenter: MvpPresenter<ComposeView>() {
    private val provider = ComposeProvider(presenter = this@ComposePresenter)

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        provider.fetchMentions()
    }

    fun setupData(users: MutableList<UserModel>) {
        viewState.setupMentions(users)
    }
}