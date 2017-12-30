package solonsky.signal.twitter.views

import solonsky.signal.twitter.models.ChatModel

/**
 * Created by neura on 30.12.17.
 */
interface ChatView {
    fun showError(text: String)
    fun showError(text: Int)
    fun stopRefresh()
    fun refreshList(data: List<ChatModel>)
}