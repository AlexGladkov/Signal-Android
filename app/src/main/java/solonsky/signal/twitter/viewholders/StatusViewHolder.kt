package solonsky.signal.twitter.viewholders

import android.accounts.Account
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.Toast
import com.arellomobile.mvp.MvpDelegate
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.daimajia.swipe.SwipeLayout
import com.google.gson.JsonObject
import com.squareup.picasso.Picasso
import com.twitter.sdk.android.core.models.Tweet
import kotlinx.android.synthetic.main.mvp_cell_status.view.*
import org.joda.time.LocalDateTime
import org.json.JSONObject
import solonsky.signal.twitter.R
import solonsky.signal.twitter.activities.*
import solonsky.signal.twitter.adapters.ImageStaggeredAdapter
import solonsky.signal.twitter.adapters.MVPStatusAdapter
import solonsky.signal.twitter.adapters.StatusAdapter
import solonsky.signal.twitter.api.ActionsApiFactory

import solonsky.signal.twitter.databinding.CellStatusBinding
import solonsky.signal.twitter.draw.CirclePicasso
import solonsky.signal.twitter.helpers.*
import solonsky.signal.twitter.holder.MvpViewHolder
import solonsky.signal.twitter.libs.Animator
import solonsky.signal.twitter.libs.DownloadFiles
import solonsky.signal.twitter.libs.MySwipeLayout
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkMode
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkOnClickListener
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkTextView
import solonsky.signal.twitter.models.ImageModel
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.models.User
import solonsky.signal.twitter.overlays.ImageActionsOverlay
import solonsky.signal.twitter.presenters.StatusPresenter
import solonsky.signal.twitter.views.StatusView
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by neura on 19.09.17.
 */

class StatusViewHolder(itemView: View, screenWidth: Int) : RecyclerView.ViewHolder(itemView), StatusView {
    private val isNight = App.getInstance().isNightEnabled
    private val mView = itemView
    private val dateConverter = DateConverter(itemView.context)
    private val mScreenWidth = screenWidth
    private val TAG: String = StatusViewHolder::class.simpleName.toString()
    var statusClickListener: MVPStatusAdapter.StatusClickListener? = null

    private var mStatus: StatusModel? = null
    var mStatusPresenter: StatusPresenter = StatusPresenter(viewState = this@StatusViewHolder)

    init {
        /* Set borders and colors to avatar */
        mView.mvp_status_civ_avatar.isBorderOverlay = !isNight
        mView.mvp_status_civ_avatar.borderColor = if (isNight) Color.parseColor("#00000000") else Color.parseColor("#1A000000")
        mView.mvp_status_civ_avatar.borderWidth = if (isNight) 0 else Utilities.convertDpToPixel(0.5f, itemView.context).toInt()
    }

    fun bind(statusModel: StatusModel) {
        mStatus = statusModel

        setupText(statusModel = statusModel)
        setupAvatar()
        setupQuote()
        setupMedia()
        setupClicks(statusModel = statusModel)
        applyStyle()
    }

    private var mActivity: WeakReference<Activity>? = null
    fun provideActivity(activity: WeakReference<Activity>) {
        this.mActivity = activity
    }

    private fun setupMedia() {
        mStatus?.let { mStatusPresenter.setupMedia(it) }
    }

    private fun setupQuote() {
        if (mStatus?.quotedStatus == null) return
        val createdAt = if (AppData.appConfiguration.isRelativeDates)
            dateConverter.parseTime(LocalDateTime(mStatus?.quotedStatus?.createdAt))
        else
            dateConverter.parseAbsTime(LocalDateTime(mStatus?.quotedStatus?.createdAt))

        mView.mvp_status_ll_quote.visibility = View.VISIBLE
        mView.mvp_status_txt_quote_text.text = mStatus?.quotedStatus?.text
        mView.mvp_status_txt_quote_username.text = if (AppData.appConfiguration.isRealNames)
            mStatus?.quotedStatus?.user?.name
        else
            mStatus?.quotedStatus?.user?.screenName
        mView.mvp_status_txt_quote_created_at.text = createdAt
    }

    /**
     * Setup avatars
     * @statusModel current status Model
     */
    private fun setupAvatar() {
        val isRound = AppData.appConfiguration.isRoundAvatars
        val isRt = mStatus?.retweetedStatus != null

        val avatarUrl = if (isRt) {
            mStatus?.retweetedStatus?.user?.originalProfileImageURL
        } else {
            mStatus?.user?.originalProfileImageURL
        }

        if (isRound) {
            mView.mvp_status_civ_avatar.visibility = View.VISIBLE
            mView.mvp_status_img_avatar.visibility = View.GONE

            Picasso.with(itemView.context).load(avatarUrl).into(mView.mvp_status_civ_avatar)
        } else {
            mView.mvp_status_civ_avatar.visibility = View.GONE
            mView.mvp_status_img_avatar.visibility = View.VISIBLE

            val size: Int = Utilities.convertDpToPixel(40f, itemView.context).toInt()
            val round = Utilities.convertDpToPixel(4f, itemView.context)
            Picasso.with(itemView.context).load(avatarUrl)
                    .resize(size, size).centerCrop().transform(CirclePicasso(round, 0f, 0, R.color.black))
                    .into(mView.mvp_status_img_avatar)
        }
    }

    /**
     * Setup tweetText, username and timestamp
     * @statusModel current status model
     */
    private fun setupText(statusModel: StatusModel) {
        val isRt = statusModel.retweetedStatus != null
        val createdAt = if (AppData.appConfiguration.isRelativeDates)
            dateConverter.parseTime(LocalDateTime(statusModel.createdAt))
        else
            dateConverter.parseAbsTime(LocalDateTime(statusModel.createdAt))
        val username = if (isRt)
            if (AppData.appConfiguration.isRealNames)
                statusModel.retweetedStatus?.user?.name
            else
                statusModel.retweetedStatus?.user?.screenName
        else if (AppData.appConfiguration.isRealNames)
            mStatus?.user?.name
        else
            mStatus?.user?.screenName

        changeLike(statusModel.isFavorited)
        mView.mvp_status_txt_created_at.text = createdAt
        mView.mvp_status_txt_username.text = username

        mView.mvp_status_txt_text.addAutoLinkMode(
                AutoLinkMode.MODE_HASHTAG, AutoLinkMode.MODE_MENTION,
                AutoLinkMode.MODE_URL, AutoLinkMode.MODE_SHORT)

        val urls = arrayOfNulls<String>(statusModel.urlEntities.size())
        for (i in 0 until statusModel.urlEntities.size()) {
            urls[i] = (statusModel.urlEntities[0] as JsonObject).get("displayURL").asString
        }

        mView.mvp_status_txt_text.shortUrls = urls

        if (isNight) {
            mView.mvp_status_txt_text.setHashtagModeColor(ContextCompat.getColor(itemView.context, R.color.dark_tag_color))
            mView.mvp_status_txt_text.setMentionModeColor(ContextCompat.getColor(itemView.context, R.color.dark_highlight_color))
            mView.mvp_status_txt_text.setUrlModeColor(ContextCompat.getColor(itemView.context, R.color.dark_highlight_color))
            mView.mvp_status_txt_text.setSelectedStateColor(ContextCompat.getColor(itemView.context, R.color.dark_secondary_text_color))
        } else {
            mView.mvp_status_txt_text.setHashtagModeColor(ContextCompat.getColor(itemView.context, R.color.light_tag_color))
            mView.mvp_status_txt_text.setMentionModeColor(ContextCompat.getColor(itemView.context, R.color.light_highlight_color))
            mView.mvp_status_txt_text.setUrlModeColor(ContextCompat.getColor(itemView.context, R.color.light_highlight_color))
            mView.mvp_status_txt_text.setSelectedStateColor(ContextCompat.getColor(itemView.context, R.color.light_secondary_text_color))
        }

        mView.mvp_status_txt_text.setRegularTextViewClick(object : AutoLinkTextView.RegularTextViewClick {
            override fun onTextClicked(v: View?) {
                mStatus?.let { mStatusPresenter.performSingleTap(statusModel = it) }
            }

            override fun onLongTextClicked(v: View?) {
                mStatus?.let { mStatusPresenter.performLongTap(statusModel = it) }
            }

            override fun onDoubleTapClicked(v: View?) {
                mStatus?.let { mStatusPresenter.performDoubleTap(statusModel = it) }
            }

        })

        val tweetText = if (isRt) {
            mStatus?.retweetedStatus?.text
        } else {
            mStatus?.text
        }

        mView.mvp_status_txt_text.setAutoLinkText(tweetText)
        if (TextUtils.isEmpty(tweetText)) {
            mView.mvp_status_txt_text.visibility = View.GONE
        } else {
            mView.mvp_status_txt_text.visibility = View.VISIBLE
        }
    }

    /**
     * Styling cell for pattern
     */
    private fun applyStyle() {
        val styling = Styling(itemView.context, Styling.convertFontToStyle(AppData.appConfiguration.fontSize))
        mView.mvp_status_txt_text.setLineSpacing(styling.textExtra, 1f)
        mView.mvp_status_txt_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.textSize.toFloat())
        mView.mvp_status_txt_quote_username.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.quoteTextSize.toFloat())
        mView.mvp_status_txt_quote_text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.quoteTextSize.toFloat())
        mView.mvp_status_txt_quote_text.setLineSpacing(styling.quoteTextExtra, 1f)
        mView.mvp_status_txt_username.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.textSize.toFloat())
        mView.mvp_status_txt_retweet.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.rtTextSize.toFloat())
        mView.mvp_status_txt_created_at.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.createdAtSize.toFloat())
        mView.mvp_status_txt_quote_created_at.setTextSize(TypedValue.COMPLEX_UNIT_DIP, styling.createdAtSize.toFloat())

        mStatus?.let {
            if (it.isExpand) {
                mView.mvp_status_ll_bottom.visibility = View.VISIBLE
            } else {
                mView.mvp_status_ll_bottom.visibility = View.GONE
            }

            val params = mView.mvp_status_divider.layoutParams as RelativeLayout.LayoutParams
            params.leftMargin = Utilities.convertDpToPixel(72f, itemView.context).toInt()
            mView.mvp_status_divider.layoutParams = params
            mView.mvp_status_divider.setBackgroundColor(itemView.resources
                    .getColor(if (it.isHighlighted)
                        if (isNight) R.color.dark_divider_highlight_color else R.color.light_divider_highlight_color
                    else if (isNight) R.color.dark_divider_color else R.color.light_divider_color))
        }

        val isHighlighted = mStatus!!.isHighlighted
        val colorInt = if (isHighlighted)
            if (isNight)
                R.color.dark_background_highlight_color
            else
                R.color.light_background_highlight_color
        else
            if (isNight)
                R.color.dark_background_color
            else
                R.color.light_background_color

        val surface = if (!isHighlighted) {
            if (isNight)
                R.drawable.dark_status_swipe_background else R.drawable.light_status_swipe_background
        } else {
            if (isNight)
                R.drawable.dark_highlight_swipe_background else R.drawable.light_highlight_swipe_background
        }

        mView.mvp_status_ll_main.setBackgroundColor(itemView.resources.getColor(colorInt))
        mView.mvp_status_view_surface.background = itemView.resources.getDrawable(surface)

        /* Set RT margin Top */
        var params = mView.mvp_status_ll_retweet.layoutParams as LinearLayout.LayoutParams
        params.topMargin = styling.rtMarginTop
        mView.mvp_status_ll_retweet.layoutParams = params

        /* Set Text margin Top */
        params = mView.mvp_status_ll_text.layoutParams as LinearLayout.LayoutParams
        params.topMargin = styling.textMarginTop
        mView.mvp_status_ll_text.layoutParams = params

        /* Set base margin elements */
        mView.mvp_status_ll_body.setPadding(0, 0, 0, styling.baseMargin)

        // Square avatar
        var baseParams = mView.mvp_status_img_avatar.layoutParams as LinearLayout.LayoutParams
        baseParams.topMargin = styling.squareAvatarMarginTop
        mView.mvp_status_img_avatar.layoutParams = baseParams

        // Round avatar
        baseParams = mView.mvp_status_civ_avatar.layoutParams as LinearLayout.LayoutParams
        baseParams.topMargin = styling.baseMargin
        mView.mvp_status_civ_avatar.layoutParams = baseParams

        // Container for username, text and media
        baseParams = mView.mvp_status_ll_container.layoutParams as LinearLayout.LayoutParams
        baseParams.topMargin = styling.baseMargin
        mView.mvp_status_ll_container.layoutParams = baseParams

        // Username
        baseParams = mView.mvp_status_ll_username.layoutParams as LinearLayout.LayoutParams
        mView.mvp_status_ll_username.layoutParams = baseParams

        // Small preview
        baseParams = mView.mvp_status_rl_media.layoutParams as LinearLayout.LayoutParams
        baseParams.topMargin = styling.smallPreviewMarginTop
        mView.mvp_status_rl_media.layoutParams = baseParams

        // Quote margin top
        baseParams = mView.mvp_status_ll_quote.layoutParams as LinearLayout.LayoutParams
        baseParams.topMargin = styling.quoteMarginTop
        mView.mvp_status_ll_quote.layoutParams = baseParams

        // Big preview
        baseParams = mView.mvp_status_rl_preview_big.layoutParams as LinearLayout.LayoutParams
        baseParams.topMargin = styling.bigImageMarginTop
        mView.mvp_status_rl_preview_big.layoutParams = baseParams

        // Quote text
        baseParams = mView.mvp_status_ll_quote_body.layoutParams as LinearLayout.LayoutParams
        baseParams.topMargin = styling.quoteTextMarginTop
        mView.mvp_status_ll_quote_body.layoutParams = baseParams
    }

    val bottomClickListener = View.OnClickListener { v ->
        mStatus?.let { mStatusPresenter.bottomClick(v.id, it) }
    }

    val retweetClickListener = View.OnClickListener { v ->
        val popupMenu = PopupMenu(itemView.context, v, 0, 0, R.style.popup_menu)
        val menuInflater = popupMenu.menuInflater
        menuInflater.inflate(R.menu.menu_retweet, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            mStatus?.let { it -> mStatusPresenter.retweetClick(itemId = item.itemId, statusModel = it) }
            false
        }

        popupMenu.show()
    }

    private val moreClickListener = View.OnClickListener {
        val popupMenu = PopupMenu(itemView.context, itemView.findViewById(R.id.mvp_status_btn_more), 0, 0, R.style.popup_menu)
        val menuInflater = popupMenu.menuInflater
        menuInflater.inflate(R.menu.menu_tweet_more, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            mStatus?.let { mStatusPresenter.moreClick(itemId = item.itemId, statusModel = it) }
            false
        }

        popupMenu.show()
    }

    fun setupClicks(statusModel: StatusModel) {
        val isRt = statusModel.retweetedStatus != null
        val user = if (isRt)
            statusModel.retweetedStatus?.user
        else
            statusModel.user

        mView.mvp_status_btn_like.setOnClickListener(bottomClickListener)
        mView.mvp_status_btn_reply.setOnClickListener(bottomClickListener)
        mView.mvp_status_btn_share.setOnClickListener(bottomClickListener)
        mView.mvp_status_btn_retweet.setOnClickListener(retweetClickListener)
        mView.mvp_status_btn_more.setOnClickListener(moreClickListener)

        mView.mvp_status_civ_avatar.setOnLongClickListener {
            mStatusPresenter.performLongAvatarClick(statusModel = statusModel)
            true
        }

        mView.mvp_status_img_avatar.setOnLongClickListener {
            mStatusPresenter.performLongAvatarClick(statusModel = statusModel)
            true
        }

        mView.mvp_status_civ_avatar.setOnClickListener {
            user?.let { user -> mStatusPresenter.avatarClick(user = user) }
        }

        mView.mvp_status_img_avatar.setOnClickListener {
            user?.let { user -> mStatusPresenter.avatarClick(user = user) }
        }

        mView.mvp_status_txt_text.setAutoLinkOnClickListener(object : AutoLinkOnClickListener {
            override fun onAutoLinkTextClick(autoLinkMode: AutoLinkMode, matchedText: String) {
                mStatusPresenter.autoTextClick(autoLinkMode, matchedText, statusModel)
            }

            override fun onAutoLinkLongTextClick(autoLinkMode: AutoLinkMode, matchedText: String) {
                mStatusPresenter.longAutoTextClick(autoLinkMode, matchedText)
            }

        })

        mView.mvp_status_bottom_wrapper.addSwipeListener(object : SwipeLayout.SwipeListener {
            internal var isOpen = false

            override fun onOpen(layout: SwipeLayout?) {
                if (!isOpen) {
                    isOpen = true
                    if (mStatus != null) {
                        AppData.CURRENT_STATUS_MODEL = statusModel
                        openDetail()
                    }


                    Handler().postDelayed({
                        layout?.close()
                        isOpen = false
                    }, 800)
                }
            }

            override fun onUpdate(layout: SwipeLayout, leftOffset: Int, topOffset: Int) {
                mStatusPresenter.updateArrow(leftOffset = leftOffset, width = mView.mvp_status_img_open.width,
                        arrowOffset = Utilities.convertDpToPixel(16f, itemView.context).toInt(),
                        accelerateOffset = Utilities.convertDpToPixel(0.4f, itemView.context).toInt())
            }

            override fun onStartOpen(layout: SwipeLayout?) {
                mStatusPresenter.onStartOpen()
            }

            override fun onStartClose(layout: SwipeLayout?) {
            }

            override fun onHandRelease(layout: SwipeLayout?, xvel: Float, yvel: Float) {
            }

            override fun onClose(layout: SwipeLayout?) {
            }

        })

        mView.mvp_status_bottom_wrapper.setOnTouchListener { _, event ->
            mStatusPresenter.onTouch(event = event, statusModel = statusModel)
            false
        }

        mView.mvp_status_rl_media.setOnClickListener {
            mStatusPresenter.performMediaClick(statusModel = statusModel, startPosition = 0)
        }

        mView.mvp_status_rl_media.setOnLongClickListener {
            mStatusPresenter.performLongMediaClick(statusModel = statusModel)
            true
        }
    }

    fun setClickListener(statusClickListener: MVPStatusAdapter.StatusClickListener) {
        this.statusClickListener = statusClickListener
    }

    override fun showToast(text: String) {
        Toast.makeText(itemView.context, text, Toast.LENGTH_SHORT).show()
    }

    override fun openDetail() {
        statusClickListener?.openActivity(intent = Intent(itemView.context, DetailActivity::class.java),
                startAnim = R.anim.slide_in_right, endAnim = R.anim.slide_out_left)
    }

    override fun openProfile(user: User) {
        val profileIntent = Intent(itemView.context, MVPProfileActivity::class.java)
        profileIntent.putExtra(Flags.PROFILE_DATA, user)
        statusClickListener?.openActivity(intent = profileIntent, startAnim = R.anim.slide_in_right, endAnim = R.anim.slide_out_left)
    }

    override fun openProfile(screenName: String) {
        val profileIntent = Intent(itemView.context, MVPProfileActivity::class.java)
        profileIntent.putExtra(Flags.PROFILE_SCREEN_NAME, screenName)
        statusClickListener?.openActivity(intent = profileIntent, startAnim = R.anim.slide_in_right, endAnim = R.anim.slide_out_left)
    }

    override fun openCompose() {
        statusClickListener?.openActivity(intent = Intent(itemView.context, ComposeActivity::class.java),
                startAnim = R.anim.slide_in_up, endAnim = R.anim.slide_out_no_animation)
    }

    override fun openMedia() {
        statusClickListener?.openActivity(intent = Intent(itemView.context, MediaActivity::class.java),
                startAnim = R.anim.slide_in_up, endAnim = R.anim.slide_out_no_animation)
    }

    override fun showDialog(type: Flags.Dialogs, title: String, isVideo: Boolean) {
        statusClickListener?.showDialog(type = type, title = title, isVideo = isVideo)
    }

    override fun shareText(text: String) {
        val text: String = if (mStatus?.retweetedStatus != null)
            mStatus?.retweetedStatus?.text.toString()
        else
            mStatus?.text.toString()
        statusClickListener?.shareText(text)
    }

    override fun shareTextWithApp(text: String, packageName: String, packageActivity: String) {
        val text: String = if (mStatus?.retweetedStatus != null)
            mStatus?.retweetedStatus?.text.toString()
        else
            mStatus?.text.toString()
        statusClickListener?.shareTextWithApp(text, packageName, packageActivity)
    }

    override fun updateArrow(dX: Float, diff: Float) {
        mView.mvp_status_img_open.animate().setDuration(0).translationX(dX).start()
        ImageAnimation.setupStatusArrow(mView.mvp_status_img_open, diff.toInt())
    }

    override fun translate(text: String) {
        statusClickListener?.translate(text)
    }

    override fun makeRt(statusModel: StatusModel?) {
        TweetActions.retweet(statusModel, itemView.context) { }
    }

    override fun showToast(text: Int) {
        Toast.makeText(itemView.context, itemView.resources.getString(text), Toast.LENGTH_SHORT).show()
    }

    override fun setClipboard(text: String) {
        val clipboard = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(itemView.context.getString(R.string.app_name), text)
        clipboard.primaryClip = clip
    }

    override fun openSearch(text: String) {
        val intent = Intent(itemView.context, MVPSearchActivity::class.java)
        intent.putExtra(Keys.SearchQuery.value, text)
        statusClickListener?.openActivity(intent = intent,
                startAnim = R.anim.slide_in_right, endAnim = R.anim.slide_out_left)
    }

    override fun openImages(urls: ArrayList<String>, startPosition: Int) {
        val imageActionsOverlay = ImageActionsOverlay(mActivity?.get(), urls, mStatus, startPosition)
        val imageActionsOverlayClickHandler = object : ImageActionsOverlay.ImageActionsOverlayClickHandler {
            override fun onBackClick(v: View?) {
                imageActionsOverlay.imageViewer.onDismiss()
            }

            override fun onSaveClick(v: View?, url: String?) {
                val downloadFiles = DownloadFiles(mActivity?.get())
                downloadFiles.saveFile(url, mActivity?.get()?.getString(R.string.download_url))
            }

            override fun onShareImageClick(v: View?, url: String?) {

            }

            override fun onReplyClick(v: View?) {
                mStatus?.let { it ->
                    mStatusPresenter.bottomClick(R.id.mvp_status_btn_reply, it)
                    imageActionsOverlay.imageViewer.onDismiss()
                }
            }

            override fun onRtClick(v: View?) {
                TweetActions.retweetPopup(mActivity?.get(), v, mStatus, { })
            }

            override fun onLikeClick(v: View?) {
                mStatus?.let { it ->
                    it.isFavorited = !it.isFavorited
                    imageActionsOverlay.isFavorited = it.isFavorited
                    imageActionsOverlay.changeFavorited()
                    TweetActions.favorite(it.isFavorited, it.id) {
                        mStatus?.let { callback ->
                            callback.isFavorited = !callback.isFavorited
                            imageActionsOverlay.isFavorited = callback.isFavorited
                            imageActionsOverlay.changeFavorited()
                        }
                    }
                }
            }

            override fun onShareClick(v: View?) {
                mStatus?.let { it -> mStatusPresenter.bottomClick(R.id.mvp_status_btn_share, it) }
            }

            override fun onMoreClick(v: View?) {
                TweetActions.morePopup(mActivity?.get(), v, mStatus) {

                }
            }
        }

        imageActionsOverlay.imageActionsOverlayClickHandler = imageActionsOverlayClickHandler
    }

    override fun openYoutube(url: String) {
        statusClickListener?.openActivity(intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                startAnim = R.anim.slide_in_up, endAnim = R.anim.slide_out_no_animation)
    }

    override fun openLink(link: String) {
        Utilities.openLink(link, mActivity?.get())
    }

    override fun setupBigMedia(imageModels: ArrayList<ImageModel>) {
        mView.mvp_status_rl_preview_big.visibility = View.VISIBLE
        mView.mvp_status_rl_media.visibility = View.GONE

        val width = (mScreenWidth - Utilities.convertDpToPixel(88f, itemView.context)).toInt()
        val params = mView.mvp_status_recycler_big_media.layoutParams as RelativeLayout.LayoutParams
        params.height = (width * 0.617).toInt()
        mView.mvp_status_recycler_big_media.layoutParams = params

        val imageStaggeredAdapter = ImageStaggeredAdapter(imageModels,
                itemView.context, ImageStaggeredAdapter.ImageStaggeredListener { imageModel, _ ->
            when (imageModel.mediaType) {
                Flags.MEDIA_TYPE.YOUTUBE -> openYoutube(imageModel.previewUrl)
                else -> mStatus?.let { mStatusPresenter.performMediaClick(it, imageModels.indexOf(imageModel)) }
            }
        }, width)

        val manager = GridLayoutManager(itemView.context, 2, GridLayoutManager.VERTICAL, false)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                    if (position == 0) if (imageModels.size % 2 == 0) 1 else 2 else 1
        }

        mView.mvp_status_recycler_big_media.setHasFixedSize(true)
        mView.mvp_status_recycler_big_media.isNestedScrollingEnabled = false
        mView.mvp_status_recycler_big_media.layoutManager = manager
        mView.mvp_status_recycler_big_media.adapter = imageStaggeredAdapter
    }

    override fun setupSmallMedia(type: String, imageUrl: String) {
        mView.mvp_status_rl_media.visibility = View.VISIBLE
        mView.mvp_status_rl_preview_big.visibility = View.GONE

        when (type) {
            Flags.MEDIA_PHOTO -> mView.mvp_status_img_small_badge.visibility = View.GONE
            Flags.MEDIA_GIF -> {
                mView.mvp_status_img_small_badge.visibility = View.VISIBLE
                mView.mvp_status_img_small_badge.setImageDrawable(itemView.context.getDrawable(R.drawable.ic_badges_media_gif))
            }
            Flags.MEDIA_VIDEO -> {
                mView.mvp_status_img_small_badge.visibility = View.VISIBLE
                mView.mvp_status_img_small_badge.setImageDrawable(itemView.context.getDrawable(R.drawable.ic_badges_media_video))
            }
            Flags.MEDIA_YOUTUBE -> {
                mView.mvp_status_img_small_badge.visibility = View.VISIBLE
                mView.mvp_status_img_small_badge.setImageDrawable(itemView.context.getDrawable(R.drawable.ic_badges_media_youtube))
            }
        }

        Picasso.with(itemView.context).load(imageUrl)
                .resize(Utilities.convertDpToPixel(64f, itemView.context).toInt(),
                        Utilities.convertDpToPixel(64f, itemView.context).toInt())
                .centerCrop()
                .transform(CirclePicasso(
                        Utilities.convertDpToPixel(4f, itemView.context),
                        Utilities.convertDpToPixel(0.5f, itemView.context),
                        25, R.color.black
                )).into(mView.mvp_status_img_preview)
    }

    override fun setupNoMedia() {
        mView.mvp_status_rl_media.visibility = View.GONE
        mView.mvp_status_rl_preview_big.visibility = View.GONE
    }

    override fun changeActions(isExpand: Boolean, isHighlighted: Boolean) {
        if (isExpand)
            statusClickListener?.closeOther(currentStatus = mStatus)
        else
            statusClickListener?.closePosition(currentStatus = mStatus)

        val animator = Animator(itemView.context)
        val expandDuration = 150
        val alphaDuration = 150
        val bottomSize = 44

        val colorInt = if (isHighlighted)
            if (isNight)
                R.color.dark_background_highlight_color
            else
                R.color.light_background_highlight_color
        else
            if (isNight)
                R.color.dark_background_color
            else
                R.color.light_background_color

        var colorSurface =
                if (isHighlighted)
                    if (isNight) R.drawable.dark_highlight_swipe_background
                    else R.drawable.light_highlight_swipe_background
                else
                    if (isNight) R.drawable.dark_status_swipe_background
                    else R.drawable.light_status_swipe_background

        val expandColor = if (isNight)
            R.color.dark_background_secondary_color
        else
            R.color.light_background_secondary_color

        val expandSurface = if (isNight)
            R.drawable.dark_expand_swipe_background
        else
            R.drawable.light_expand_swipe_background


        if (isExpand) {
            animator.changeColor(colorInt, expandColor, 150, mView.mvp_status_ll_main)
            mView.mvp_status_view_surface.background = itemView.resources.getDrawable(expandSurface)
            mView.mvp_status_ll_bottom.visibility = View.VISIBLE
            animator.changeHeight(0, Utilities.convertDpToPixel(bottomSize.toFloat(), itemView.context).toInt(),
                    expandDuration, mView.mvp_status_ll_bottom)
            mView.mvp_status_ll_bottom.animate().alpha(1f).setDuration(alphaDuration.toLong()).start()
        } else {
            animator.changeColor(expandColor, colorInt, 150, mView.mvp_status_ll_main)
            mView.mvp_status_view_surface.background = itemView.resources.getDrawable(colorSurface)
            mView.mvp_status_ll_bottom.animate().alpha(0f).setDuration(alphaDuration.toLong()).start()
            animator.changeHeight(Utilities.convertDpToPixel(bottomSize.toFloat(), itemView.context).toInt(), 0,
                    expandDuration, mView.mvp_status_ll_bottom)
        }
    }

    override fun changeLike(isLike: Boolean) {
        if (isLike) {
            mView.mvp_status_img_favorite.visibility = View.VISIBLE
            if (isNight) {
                mView.mvp_status_img_like.setColorFilter(itemView.context.getColor(R.color.dark_like_tint_color))
            } else {
                mView.mvp_status_img_like.setColorFilter(itemView.context.getColor(R.color.light_like_tint_color))
            }
        } else {
            mView.mvp_status_img_favorite.visibility = View.GONE
            mView.mvp_status_img_like.setColorFilter(itemView.context.getColor(R.color.dark_hint_text_color))
        }
    }
}
