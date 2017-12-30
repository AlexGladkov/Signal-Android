package solonsky.signal.twitter.converters

import solonsky.signal.twitter.models.ChatModel
import solonsky.signal.twitter.models.DirectModel
import twitter4j.DirectMessage

/**
 * Created by neura on 30.12.17.
 */

interface DirectConverter {
    fun apiToModel(directApi: DirectMessage): ChatModel
}