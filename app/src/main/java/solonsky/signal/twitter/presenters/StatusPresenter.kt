package solonsky.signal.twitter.presenters

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.MotionEvent
import com.google.firebase.crash.FirebaseCrash
import com.google.gson.JsonObject
import solonsky.signal.twitter.R
import solonsky.signal.twitter.data.ShareData
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Flags
import solonsky.signal.twitter.helpers.TweetActions
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkMode
import solonsky.signal.twitter.models.ConfigurationModel
import solonsky.signal.twitter.models.ImageModel
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.models.User
import solonsky.signal.twitter.views.StatusView
import java.util.*

/**
 * Created by neura on 03.11.17.
 */

class StatusPresenter(private val viewState: StatusView) {
    val TAG: String = StatusPresenter::class.simpleName.toString()
    private val DOUBLE_CLICK_INTERVAL: Long = 200
    private val LONG_HOLD_TIMEOUT: Long = 500

    /**
     * Open profile from avatar click
     */
    fun avatarClick(user: User) {
        AppData.CURRENT_USER = user
        Flags.userSource = Flags.UserSource.data
        Flags.homeUser = user.id == AppData.ME.id
        viewState.openProfile(user = user)
    }

    fun avatarClick(screenName: String) {
        AppData.CURRENT_SCREEN_NAME = screenName
        Flags.userSource = Flags.UserSource.screenName
        Flags.homeUser = TextUtils.equals(screenName, AppData.ME.screenName)
        viewState.openProfile(screenName = screenName)
    }

    fun showToast(text: String) {
        viewState.showToast(text)
    }

    fun autoTextClick(autoLinkMode: AutoLinkMode, matchedText: String, statusModel: StatusModel) {
        when (autoLinkMode) {
            AutoLinkMode.MODE_HASHTAG -> {
                viewState.openSearch(text = matchedText)
            }
            AutoLinkMode.MODE_MENTION -> avatarClick(matchedText)
            AutoLinkMode.MODE_SHORT -> {
                try {
                    statusModel.urlEntities
                            .asSequence()
                            .filter { it.asJsonObject.get("displayURL").asString == matchedText }
                            .forEach { viewState.openLink(link = it.asJsonObject.get("expandedURL").asString) }
                } catch (e: NullPointerException) {
                    FirebaseCrash.log(e.localizedMessage)
                }
            }
            else -> {

            }
        }
    }

    fun longAutoTextClick(autoLinkMode: AutoLinkMode, matchedText: String) {
        when (autoLinkMode) {
            AutoLinkMode.MODE_HASHTAG -> viewState.showDialog(type = Flags.Dialogs.HASH, title = matchedText, isVideo = false)
            AutoLinkMode.MODE_MENTION -> viewState.showDialog(type = Flags.Dialogs.USER, title = matchedText, isVideo = false)
            AutoLinkMode.MODE_SHORT -> viewState.showDialog(type = Flags.Dialogs.LINK, title = matchedText, isVideo = false)
            else -> TODO()
        }
    }

    private var thisTouchTime: Long = 0
    private var previousTouchTime: Long = 0
    internal var buttonHeldTime: Long = 0
    private var initialX = 0f
    private var initialY = 0f
    private var clickHandled = false
    private var isStartOpen = false

    fun onTouch(event: MotionEvent, statusModel: StatusModel) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isStartOpen = false
                initialX = event.rawX
                initialY = event.rawY
                thisTouchTime = System.currentTimeMillis()
                if (thisTouchTime - previousTouchTime <= DOUBLE_CLICK_INTERVAL) {
                    // Double click detected
                    clickHandled = true
                    performDoubleTap(statusModel)
                } else {
                    // Defer event handling until later
                    clickHandled = false
                }
                previousTouchTime = thisTouchTime
            }

            MotionEvent.ACTION_UP -> {
                if (!clickHandled) {
                    buttonHeldTime = System.currentTimeMillis() - thisTouchTime
                    if (buttonHeldTime > LONG_HOLD_TIMEOUT) {
                        clickHandled = true
                        if (!isStartOpen)
                            performLongTap(statusModel)
                    } else {
                        val clickHandler = @SuppressLint("HandlerLeak")
                        object : Handler() {
                            override fun handleMessage(msg: Message) {
                                if (!clickHandled) {
                                    clickHandled = true
                                    if (!isStartOpen)
                                        performSingleTap(statusModel)
                                }
                            }
                        }
                        val m = Message()
                        clickHandler.sendMessageDelayed(m, DOUBLE_CLICK_INTERVAL)
                    }
                }
            }
        }
    }

    fun makeReply(statusModel: StatusModel) {
        val status = if (statusModel.retweetedStatus != null)
            statusModel.retweetedStatus
        else
            statusModel

        AppData.CURRENT_STATUS_MODEL = status
        AppData.CURRENT_USER = status.user
        Flags.CURRENT_COMPOSE = Flags.COMPOSE_REPLY
        viewState.openCompose()
    }

    fun performSingleTap(statusModel: StatusModel) {
        when (AppData.appConfiguration.shortTap) {
            ConfigurationModel.TAP_SHOW_ACTIONS -> {
                statusModel.isExpand = !statusModel.isExpand
                viewState.changeActions(isExpand = statusModel.isExpand, isHighlighted = statusModel.isHighlighted)
            }
            ConfigurationModel.TAP_VIEW_DETAILS -> {
                AppData.CURRENT_STATUS_MODEL = if (statusModel.isRetweet) statusModel.retweetedStatus else statusModel
                viewState.openDetail()
            }
            ConfigurationModel.TAP_OPEN_MEDIA -> if (statusModel.mediaEntities.size() > 0) {
                performMediaClick(statusModel = statusModel, startPosition = 0)
            } else {
                statusModel.isExpand = !statusModel.isExpand
                viewState.changeActions(isExpand = statusModel.isExpand, isHighlighted = statusModel.isHighlighted)
            }
            ConfigurationModel.TAP_GO_TO_LINK -> if (statusModel.urlEntities.size() > 0) {
                viewState.openLink(link = statusModel.urlEntities.get(0).asJsonObject.get("expandedURL").asString)
            } else {
                statusModel.isExpand = !statusModel.isExpand
                viewState.changeActions(isExpand = statusModel.isExpand, isHighlighted = statusModel.isHighlighted)
            }
        }
    }

    fun performDoubleTap(statusModel: StatusModel) {
        when (AppData.appConfiguration.doubleTap) {
            ConfigurationModel.TAP_REPLY -> {
                makeReply(statusModel = statusModel)
            }
            ConfigurationModel.TAP_QUOTE -> {
                Flags.CURRENT_COMPOSE = Flags.COMPOSE_QUOTE
                AppData.CURRENT_STATUS_MODEL = statusModel

                viewState.openCompose()
            }
            ConfigurationModel.TAP_RETWEET -> {
                val status = if (statusModel.retweetedStatus != null)
                    statusModel.retweetedStatus
                else
                    statusModel

                viewState.makeRt(statusModel = status)

            }
            ConfigurationModel.TAP_LIKE -> {
                statusModel.isFavorited = !statusModel.isFavorited
                TweetActions.favorite(statusModel.isFavorited, statusModel.id) { }
            }
        }
    }

    fun performLongTap(statusModel: StatusModel) {
        val text: String = if (statusModel.retweetedStatus != null)
            statusModel.retweetedStatus.text.toString()
        else
            statusModel.text.toString()

        when (AppData.appConfiguration.longTap) {
            ConfigurationModel.TAP_LAST_SHARING -> {
                if (!ShareData.getInstance().isCacheLoaded) {
                    ShareData.getInstance().loadCache()
                }

                if (ShareData.getInstance().shares.size > 0) {
                    val packageName = ShareData.getInstance().shares[0].split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].replace("ComponentInfo{", "")
                    val packageActivity = ShareData.getInstance().shares[0].split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].replace("}", "")
                    viewState.shareTextWithApp(text, packageName, packageActivity)
                } else {
                    viewState.shareText(text)
                }
            }

            ConfigurationModel.TAP_READ_LATER -> showToast("Read later")
            ConfigurationModel.TAP_TRANSLATE -> viewState.translate(text)
            ConfigurationModel.TAP_SHARE -> viewState.shareText(text)
        }
    }

    private var accelerate = 0
    private var oldOffset = 0
    fun updateArrow(leftOffset: Int, width: Int, arrowOffset: Int, accelerateOffset: Int) {
        isStartOpen = true
        val arrowWidth = (width + arrowOffset)
        val dX: Float
        if (Math.abs(leftOffset) < arrowWidth) {
            dX = (-leftOffset / 2).toFloat()
        } else {
            if (leftOffset < oldOffset && accelerate <= arrowOffset) {
                accelerate += accelerateOffset
            } else if (leftOffset > oldOffset && accelerate >= 0) {
                accelerate -= accelerateOffset
            }
            dX = (-leftOffset / 2).toFloat() - accelerate
        }

        oldOffset = leftOffset
        val diff: Float = ((0 - leftOffset) / 10).toFloat()
        viewState.updateArrow(dX, diff)
    }

    fun onStartOpen() {
        isStartOpen = true
    }

    fun bottomClick(id: Int, statusModel: StatusModel) {
        val text = if (statusModel.retweetedStatus == null) {
            statusModel.text
        } else {
            statusModel.retweetedStatus.text
        }

        when (id) {
            R.id.mvp_status_btn_share -> viewState.shareText(text = text)
            R.id.mvp_status_btn_reply -> makeReply(statusModel = statusModel)
            R.id.mvp_status_btn_like -> {
                statusModel.isFavorited = !statusModel.isFavorited
                viewState.changeLike(statusModel.isFavorited)

                TweetActions.favorite(statusModel.isFavorited, statusModel.id) {
                    statusModel.isFavorited = !statusModel.isFavorited
                    viewState.changeLike(statusModel.isFavorited)
                }
            }
        }
    }

    fun moreClick(itemId: Int, statusModel: StatusModel) {
        val currentStatus = if (statusModel.retweetedStatus == null) {
            statusModel
        } else {
            statusModel.retweetedStatus
        }

        when (itemId) {
            R.id.more_translate -> viewState.translate(text = currentStatus.text)
            R.id.more_copy -> {
                viewState.setClipboard(text = currentStatus.text)
                viewState.showToast(text = R.string.successfully_copied)
            }
            R.id.more_link -> {
                val template = "https://twitter.com/[screen_name]/status/[status_id]"
                val shareText = template.replace(oldValue = "[screen_name]", newValue = currentStatus.user.screenName)
                        .replace(oldValue = "[status_id]", newValue = currentStatus.id.toString())
                viewState.shareText(text = shareText)
            }
            R.id.more_details -> {
                AppData.CURRENT_STATUS_MODEL = currentStatus
                viewState.openDetail()
            }
        }
    }

    fun retweetClick(itemId: Int, statusModel: StatusModel) {
        val currentStatus = if (statusModel.retweetedStatus == null) {
            statusModel
        } else {
            statusModel.retweetedStatus
        }

        when (itemId) {
            R.id.tweet_retweet -> viewState.makeRt(statusModel = currentStatus)
            R.id.tweet_quote -> {
                Flags.CURRENT_COMPOSE = Flags.COMPOSE_QUOTE
                AppData.CURRENT_STATUS_MODEL = currentStatus
                viewState.openCompose()
            }
        }
    }

    fun performMediaClick(statusModel: StatusModel, startPosition: Int) {
        if (statusModel.mediaEntities.size() > 0) {
            val type = statusModel.mediaEntities.get(0).asJsonObject.get("type").asString
            when (type) {
                Flags.MEDIA_GIF -> {
                    AppData.MEDIA_URL = statusModel.mediaEntities.get(0).asJsonObject
                            .get("videoVariants").asJsonArray.get(0).asJsonObject.get("url").asString
                    AppData.MEDIA_TYPE = Flags.MEDIA_TYPE.GIF
                    viewState.openMedia()
                }
                Flags.MEDIA_VIDEO -> {
                    AppData.MEDIA_URL = statusModel.mediaEntities.get(0).asJsonObject
                            .get("videoVariants").asJsonArray.get(0).asJsonObject.get("url").asString
                    AppData.MEDIA_TYPE = Flags.MEDIA_TYPE.VIDEO
                    viewState.openMedia()
                }
                Flags.MEDIA_PHOTO -> {
                    val urls = statusModel.mediaEntities
                            .map { it as JsonObject }
                            .mapTo(ArrayList<String>()) { it.get("mediaURLHttps").asString }

                    viewState.openImages(urls = urls, startPosition = startPosition)
                }
                Flags.MEDIA_YOUTUBE -> viewState.openYoutube(url = statusModel.mediaEntities.get(0)
                        .asJsonObject.get("url").asString)
            }
        }
    }


    fun setupMedia(statusModel: StatusModel) {
        if (statusModel.mediaEntities.size() > 0) {
            when {
                AppData.appConfiguration.thumbnails == ConfigurationModel.THUMB_SMALL -> {
                    val imageUrl = statusModel.mediaEntities.get(0).asJsonObject.get("mediaURLHttps").asString
                    val type = statusModel.mediaEntities.get(0).asJsonObject.get("type").asString
                    viewState.setupSmallMedia(type = type, imageUrl = imageUrl)
                }
                AppData.appConfiguration.thumbnails == ConfigurationModel.THUMB_BIG -> {
                    val imageModels = ArrayList<ImageModel>()
                    for (i in 0 until statusModel.mediaEntities.size()) {
                        val imageModel = ImageModel(statusModel.mediaEntities.get(i)
                                .asJsonObject.get("mediaURLHttps").asString)
                        val type = statusModel.mediaEntities.get(i).asJsonObject.get("type").asString
                        when (type) {
                            Flags.MEDIA_GIF -> imageModel.mediaType = Flags.MEDIA_TYPE.GIF
                            Flags.MEDIA_PHOTO -> imageModel.mediaType = Flags.MEDIA_TYPE.IMAGE
                            Flags.MEDIA_VIDEO -> imageModel.mediaType = Flags.MEDIA_TYPE.VIDEO
                            Flags.MEDIA_YOUTUBE -> {
                                imageModel.mediaType = Flags.MEDIA_TYPE.YOUTUBE
                                imageModel.previewUrl = statusModel.mediaEntities.get(i)
                                        .asJsonObject.get("expandedURL").asString
                                imageModel.imageUrl = statusModel.mediaEntities.get(i)
                                        .asJsonObject.get("mediaURLHttps").asString
                            }
                        }

                        imageModels.add(imageModel)
                    }

                    viewState.setupBigMedia(imageModels = imageModels)
                }
                else -> viewState.setupNoMedia()
            }
        } else {
            viewState.setupNoMedia()
        }
    }

    fun performLongMediaClick(statusModel: StatusModel) {
        var currentStatus = if (statusModel.isRetweet && statusModel.retweetedStatus != null) {
            statusModel.retweetedStatus
        } else {
            statusModel
        }

        if (currentStatus.mediaEntities.size() > 0) {
            if (currentStatus.mediaEntities.get(0).asJsonObject.get("type").asString == "photo") {
                viewState.showDialog(
                        type = Flags.Dialogs.MEDIA,
                        title = currentStatus.mediaEntities.get(0).asJsonObject.get("mediaURL").asString,
                        isVideo = false)
            } else {
                if (currentStatus.mediaEntities.get(0).asJsonObject.get("videoVariants").asJsonArray.size() > 0) {
                    viewState.showDialog(
                            type = Flags.Dialogs.MEDIA,
                            title = currentStatus.mediaEntities.get(0)
                                    .asJsonObject.get("videoVariants").asJsonArray
                                    .get(0).asJsonObject.get("url").asString,
                            isVideo = true)
                }
            }
        }
    }

    fun performLongAvatarClick(statusModel: StatusModel) {
        viewState.showDialog(
                type = Flags.Dialogs.USER,
                title = if (statusModel.isRetweet)
                    statusModel.retweetedStatus.user.screenName
                else
                    statusModel.user.screenName,
                isVideo = false
        )
    }
}