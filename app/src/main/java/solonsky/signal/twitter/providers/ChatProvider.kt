package solonsky.signal.twitter.providers

import android.os.Handler
import org.joda.time.LocalDateTime
import solonsky.signal.twitter.activities.ChatActivity
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.models.ChatModel
import solonsky.signal.twitter.viewmodels.ChatViewModel
import solonsky.signal.twitter.views.ChatView
import twitter4j.*
import java.util.*

/**
 * Created by neura on 30.12.17.
 */

class ChatProvider(private val viewState: ChatView) {
    private val asyncTwitter = Utilities.getAsyncTwitter()

    fun refreshData(lastId: Long) {
        val handler = Handler()
        val directMessages: ArrayList<DirectMessage> = ArrayList()
        val chats: List<ChatModel> = ArrayList()

        val paging = Paging()
        paging.sinceId = lastId

        asyncTwitter.addListener(object: TwitterAdapter() {
            override fun onException(te: TwitterException?, method: TwitterMethod?) {
                super.onException(te, method)

                handler.post({
                    viewState.stopRefresh()
                    te?.localizedMessage?.let { viewState.showError(text = it) }
                })
            }

            override fun gotDirectMessages(messages: ResponseList<DirectMessage>) {
                super.gotDirectMessages(messages)
                directMessages.addAll(messages)

                handler.post({
                    asyncTwitter.getSentDirectMessages(paging)
                })
            }

            override fun gotSentDirectMessages(messages: ResponseList<DirectMessage>) {
                super.gotSentDirectMessages(messages)
                directMessages.addAll(messages)
                Collections.sort(directMessages, { p0, p1 ->
                    p0.createdAt.compareTo(p1.createdAt) })

                directMessages.forEach({
                    val isMine = it.recipientId == AppData.ME.id
                    val showArrow = try {
                        chats.isEmpty() || chats.get(chats.size - 1).type != if (isMine) AppData.CHAT_ME else AppData.CHAT_NOT_ME
                    } catch (e: Exception) {
                        true
                    }

                    var text = it.text
                    val shorts = ArrayList<String>()
                    for (urlEntity in it.urlEntities) {
                        text = text.replace(urlEntity.url, urlEntity.displayURL)
                        shorts.add(urlEntity.displayURL)
                    }

                    val chatModel: ChatModel
                    chatModel = if (it.mediaEntities.isNotEmpty()) {
                        ChatModel(
                                it.id, it.senderId,
                                if (isMine) AppData.CHAT_ME else AppData.CHAT_NOT_ME, "",
                                it.mediaEntities[0].mediaURL,
                                it.sender.originalProfileImageURL, LocalDateTime(it.createdAt).toString("HH:mm"),
                                showArrow, showArrow)
                    } else {
                        ChatModel(
                                it.id, it.senderId,
                                if (isMine) AppData.CHAT_ME else AppData.CHAT_NOT_ME, text, "",
                                it.sender.originalProfileImageURL,
                                LocalDateTime(it.createdAt).toString("HH:mm"),
                                showArrow, showArrow)
                    }
                    chatModel.setShortUrls(shorts)
                })

                handler.post({
                    viewState.stopRefresh()
                    viewState.refreshList(chats)
                })
            }
        })

        asyncTwitter.getDirectMessages(paging)
    }
}