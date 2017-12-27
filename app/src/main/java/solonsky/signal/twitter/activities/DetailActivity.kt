package solonsky.signal.twitter.activities

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast

import com.daimajia.swipe.SwipeLayout
import com.google.gson.JsonObject
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import com.squareup.picasso.Picasso

import org.joda.time.LocalDateTime

import java.util.ArrayList

import solonsky.signal.twitter.R
import solonsky.signal.twitter.adapters.DetailStaggeredAdapter
import solonsky.signal.twitter.adapters.StatusAdapter
import solonsky.signal.twitter.data.FeedData
import solonsky.signal.twitter.data.UsersData
import solonsky.signal.twitter.databinding.ActivityDetailBinding
import solonsky.signal.twitter.draw.CirclePicasso
import solonsky.signal.twitter.fragments.DetailReplyFragment
import solonsky.signal.twitter.fragments.DetailRtFragment
import solonsky.signal.twitter.fragments.DummyFragment
import solonsky.signal.twitter.helpers.*
import solonsky.signal.twitter.libs.DownloadFiles
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkMode
import solonsky.signal.twitter.libs.autoLinkTextView.AutoLinkOnClickListener
import solonsky.signal.twitter.models.ImageModel
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.overlays.ImageActionsOverlay
import solonsky.signal.twitter.viewmodels.DetailViewModel

/**
 * Created by neura on 27.06.17.
 */

class DetailActivity : AppCompatActivity(), SmartTabLayout.TabProvider {
    private var CURRENT_POSITION = 1
    private val TAG = DetailActivity::class.java.simpleName
    private var mActivity: DetailActivity? = null
    private lateinit var binding: ActivityDetailBinding
    private var threadHeight: Int = 0
    private var statusHeight: Int = 0
    private var imageHeight: Int = 0

    private var replyCount: Long = 0
    private val favCount: Long = 0
    private var rtCount: Long = 0

    private val TOOLBAR_MARGIN = 96
    private var viewModel: DetailViewModel? = null

    private val detailReplyFragment = DetailReplyFragment()
    //    private DetailLikeFragment detailLikeFragment = new DetailLikeFragment();
    private val detailRtFragment = DetailRtFragment()

    private var lastFragment: Fragment? = null
    private val threadModels: ArrayList<StatusModel>? = null
    private lateinit var mTxtReply: TextView
    private lateinit var mImgReply: ImageView
    private lateinit var mTxtRt: TextView
    private lateinit var mImgRt: ImageView
    private var isOpen = false

    private var scrollTouchListener: View.OnTouchListener = object : View.OnTouchListener {
        internal var startY = -1
        internal var oldDiff = 0

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (!viewModel!!.isLoaded) return false
            val Y = event.rawY.toInt()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = Y
                    oldDiff = 0
                }
                MotionEvent.ACTION_UP -> if (!isOpen) {
                    changeThreadReadStatus()
                    binding.recyclerDetailConversation.animate().setDuration(300).translationY(0f)
                    binding.rlDetailMain.animate().setDuration(300).translationY(threadHeight.toFloat())
                    isOpen = true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (startY == -1) startY = Y
                    val diff = Y - startY
                    val isDown = diff > oldDiff

                    if ((diff < threadHeight && isDown || diff < threadHeight && !isDown) && !isOpen) {
                        binding.rlDetailMain.translationY = diff.toFloat()
                        binding.recyclerDetailConversation.translationY = (-threadHeight + diff).toFloat()
                    }

                    if (diff >= threadHeight && !isOpen) {
                        isOpen = true
                        changeThreadReadStatus()
                    }
                    oldDiff = diff
                }
            }
            binding.root.invalidate()
            return !isOpen
        }
    }

    private var isShadow = false

    val statusClickListener: StatusAdapter.StatusClickListener = StatusAdapter.StatusClickListener {
        searchText, v -> Log.e(TAG, "Search start - " + searchText) }

    internal var tabY = 100000
    internal var isChange = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (App.getInstance().isNightEnabled) {
            setTheme(R.style.TranslucentDark)
        }

        val handler = Handler()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)

        if (AppData.CURRENT_STATUS_MODEL == null) finish()

        setupSlideBack()
        setupStatusBar()

        binding.scrollDetail.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY == 0 && isOpen && viewModel!!.isLoaded) {
                if (!viewModel!!.isExpanded) {
                    viewModel!!.isExpanded = true
                    val valueAnimator = ValueAnimator.ofInt(19, 0)
                    valueAnimator.duration = THREAD_DURATION
                    valueAnimator.addUpdateListener { animation -> ImageAnimation.setupDisclosureArrow(binding.imgDetailArrow, animation.animatedValue as Int) }
                    valueAnimator.start()
                }
            }

            setupStatus(scrollY)
            setupTabs(scrollY)
        })

        binding.llDetailStatus.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.llDetailStatus.viewTreeObserver.removeOnGlobalLayoutListener(this)
                handler.post {
                    statusHeight = binding.llDetailStatus.height
                    val params = binding.llDetailContent.layoutParams as RelativeLayout.LayoutParams
                    params.topMargin = statusHeight
                    binding.llDetailContent.layoutParams = params
                }
            }
        })

        binding.recyclerDetailMedia.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.llDetailContent.viewTreeObserver.removeOnGlobalLayoutListener(this)
                handler.post { imageHeight = binding.llDetailContent.height }
            }
        })

        mActivity = this

        val shadowParams = binding.viewDetailShadow.layoutParams
        shadowParams.height = if (AppData.CURRENT_STATUS_MODEL.mediaEntities.size() > 0)
            Utilities.convertDpToPixel(4f, applicationContext).toInt()
        else
            0
        binding.viewDetailShadow.layoutParams = shadowParams

        val adapter = FragmentPagerItemAdapter(
                supportFragmentManager, FragmentPagerItems.with(this)
                .add("", DummyFragment::class.java)
                .add("", DummyFragment::class.java)
                .create())

        // Declare the in and out animations and initialize them
        val `in` = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in)
        val out = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_out)
        `in`.duration = THREAD_DURATION / 2
        out.duration = THREAD_DURATION / 2

        binding.txtDetailSubtitle.inAnimation = `in`
        binding.txtDetailSubtitle.outAnimation = out
        binding.scrollDetail.setOnTouchListener(scrollTouchListener)

        binding.stbDetail.setCustomTabView(this)
        binding.vpDetail.adapter = adapter
        binding.stbDetail.setViewPager(binding.vpDetail)
        binding.stbDetail.setOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val oldTab = binding.stbDetail.getTabAt(CURRENT_POSITION)
                val currentTab = binding.stbDetail.getTabAt(position)

                val oldView = oldTab.findViewById<View>(R.id.tab_layout) as RelativeLayout
                val currentView = currentTab.findViewById<View>(R.id.tab_layout) as RelativeLayout

                val textView = currentView.findViewById<View>(R.id.tab_txt) as TextView

                when (position) {
                    0 -> {
                        textView.text = replyCount.toString()
                        selectFragment(detailReplyFragment)
                    }

                    1 -> {
                        textView.text = rtCount.toString()
                        selectFragment(detailRtFragment)
                    }
                }

                oldView.background = resources.getDrawable(R.drawable.tab_shape_transparent)
                currentView.background = resources.getDrawable(if (App.getInstance().isNightEnabled)
                    R.drawable.tab_shape_dark
                else
                    R.drawable.tab_shape_light)

                CURRENT_POSITION = position
            }
        })

        checkQuote()

        val currentStatus = AppData.CURRENT_STATUS_MODEL
        val screenName = if (currentStatus.inReplyToStatusId > -1)
            getString(R.string.in_reply)
        else
            "@" + currentStatus.user.screenName

        val dateClient = ("  •  via " + AppData.CURRENT_STATUS_MODEL.source.replace(">",
                "special").replace("<", "special").split("special".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[2]
                + if (currentStatus.geoLocation == null) "" else "  •  ")
        val postTime = LocalDateTime(currentStatus.createdAt).toString("dd.MM.YY, HH:mm")
        val location = if (currentStatus.place == null) "" else currentStatus.place.get("fullName").asString

        viewModel = DetailViewModel(currentStatus.user.name, screenName,
                currentStatus.user.originalProfileImageURL,
                currentStatus.text, currentStatus.user.location,
                postTime, postTime + dateClient + location,
                mActivity, currentStatus.isFavorited,
                UsersData.getInstance().followingList.contains(currentStatus.user.id) || currentStatus.user.id == AppData.ME.id,
                currentStatus.inReplyToStatusId > -1,
                currentStatus.quotedStatus != null,
                currentStatus.mediaEntities)


        val imagesFiltered = ArrayList<ImageModel>()
        for (image in currentStatus.mediaEntities) {
            val url = (image as JsonObject).get("mediaURL").asString
            val imageModel = ImageModel(url)
            when (image.get("type").asString) {
                "image" -> imageModel.mediaType = Flags.MEDIA_TYPE.IMAGE

                "video" -> {
                    var videoUrl = ""
                    for (variants in image.get("videoVariants").asJsonArray) {
                        videoUrl = variants.asJsonObject.get("url").asString
                    }

                    imageModel.previewUrl = imageModel.imageUrl
                    imageModel.imageUrl = videoUrl
                    imageModel.mediaType = Flags.MEDIA_TYPE.VIDEO
                }

                "animated_gif" -> {
                    var videoUrl = ""
                    for (variants in image.get("videoVariants").asJsonArray) {
                        videoUrl = variants.asJsonObject.get("url").asString
                    }

                    imageModel.previewUrl = imageModel.imageUrl
                    imageModel.imageUrl = videoUrl
                    imageModel.mediaType = Flags.MEDIA_TYPE.GIF
                }

                "youtube" -> {
                    imageModel.previewUrl = image.get("mediaURLHttps").asString
                    imageModel.imageUrl = image.get("url").asString
                    imageModel.mediaType = Flags.MEDIA_TYPE.YOUTUBE
                }
            }

            imagesFiltered.add(imageModel)
        }

        val imagesStaggeredAdapter = DetailStaggeredAdapter(imagesFiltered, mActivity,
                DetailStaggeredAdapter.ImageStaggeredListener { imageModel, v ->
                    when {
                        imageModel.mediaType == Flags.MEDIA_TYPE.IMAGE -> {
                            val urls = imagesFiltered.mapTo(ArrayList<String>()) { it.imageUrl }

                            val imageActionsOverlay = ImageActionsOverlay(mActivity, urls,
                                    AppData.CURRENT_STATUS_MODEL, imagesFiltered.indexOf(imageModel))
                            imageActionsOverlay.imageActionsOverlayClickHandler = object : ImageActionsOverlay.ImageActionsOverlayClickHandler {
                                override fun onBackClick(v: View) {
                                    imageActionsOverlay.imageViewer.onDismiss()
                                }

                                override fun onSaveClick(v: View, url: String) {
                                    val downloadFiles = DownloadFiles(mActivity)
                                    downloadFiles.saveFile(url, mActivity!!.getString(R.string.download_url))
                                }

                                override fun onShareImageClick(v: View, url: String) {

                                }

                                override fun onReplyClick(v: View) {
                                    TweetActions.reply(AppData.CURRENT_STATUS_MODEL, mActivity)
                                    imageActionsOverlay.imageViewer.onDismiss()
                                }

                                override fun onRtClick(v: View) {
                                    TweetActions.retweetPopup(mActivity, v, AppData.CURRENT_STATUS_MODEL) { error -> Toast.makeText(mActivity!!.applicationContext, error, Toast.LENGTH_SHORT).show() }
                                }

                                override fun onLikeClick(v: View) {
                                    val statusModel = AppData.CURRENT_STATUS_MODEL
                                    statusModel.isFavorited = !statusModel.isFavorited
                                    imageActionsOverlay.isFavorited = statusModel.isFavorited
                                    imageActionsOverlay.changeFavorited()
                                    TweetActions.favorite(statusModel.isFavorited, statusModel.id) {
                                        statusModel.isFavorited = !statusModel.isFavorited
                                        imageActionsOverlay.isFavorited = statusModel.isFavorited
                                        imageActionsOverlay.changeFavorited()
                                    }
                                }

                                override fun onShareClick(v: View) {

                                }

                                override fun onMoreClick(v: View) {

                                }
                            }
                        }
                        imageModel.mediaType == Flags.MEDIA_TYPE.YOUTUBE -> mActivity!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(imageModel.imageUrl)))
                        else -> {
                            AppData.MEDIA_URL = imageModel.imageUrl
                            AppData.MEDIA_TYPE = imageModel.mediaType
                            mActivity!!.startActivity(Intent(mActivity!!.applicationContext, MediaActivity::class.java))
                        }
                    }
                })

        val manager = GridLayoutManager(mActivity!!.applicationContext, 2, GridLayoutManager.VERTICAL, false)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0) if (imagesFiltered.size % 2 == 0) 1 else 2 else 1
            }
        }

        binding.recyclerDetailMedia.setHasFixedSize(true)
        binding.recyclerDetailMedia.isNestedScrollingEnabled = false
        binding.recyclerDetailMedia.layoutManager = manager
        binding.recyclerDetailMedia.adapter = imagesStaggeredAdapter
        binding.recyclerDetailMedia.addItemDecoration(
                ListConfig.SpacesItemDecoration(Utilities.convertDpToPixel(2f,
                        mActivity!!.applicationContext).toInt()))

        binding.model = viewModel
        binding.click = object : DetailViewModel.DetailClickHandler {
            override fun onAvatarClick(v: View?) {
                val profileIntent = Intent(applicationContext, MVPProfileActivity::class.java)
                profileIntent.putExtra(Flags.PROFILE_DATA, currentStatus.user)
                startActivity(profileIntent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            override fun onReplyClick(v: View) {
                TweetActions.reply(currentStatus, mActivity)
            }

            override fun onRtClick(v: View) {
                TweetActions.retweetPopup(mActivity, v, currentStatus) { }
            }

            override fun onLikeClick(v: View) {
                currentStatus.isFavorited = !currentStatus.isFavorited
                viewModel!!.isFavorite = currentStatus.isFavorited
                TweetActions.favorite(currentStatus.isFavorited, currentStatus.id) { currentStatus.isFavorited = !currentStatus.isFavorited }
            }

            override fun onShareClick(v: View) {
                TweetActions.share(currentStatus, mActivity)
            }

            override fun onMoreClick(v: View) {
                TweetActions.morePopup(mActivity, v, currentStatus) { statusModel ->
                    val position = FeedData.getInstance().feedStatuses.indexOf(statusModel)
                    FeedData.getInstance().feedStatuses.remove(statusModel)
                    FeedData.getInstance().saveCache(TAG)
                    FeedData.getInstance().updateHandler.onDelete(position)
                    Toast.makeText(applicationContext, getString(R.string.success_deleted), Toast.LENGTH_SHORT).show()
                    onBackPressed()
                }
            }

            override fun onThreadClick(v: View) {
                //                binding.scrollDetail.smoothScrollTo(0, viewModel.isExpanded() ? 0 : threadHeight);
                //                binding.rlDetailMain.animate().translationY(!viewModel.isExpanded() ? 0 : threadHeight).setDuration(300).start();
                if (isOpen) {
                    changeArrowStatus()
                    ObjectAnimator.ofInt(binding.scrollDetail, "scrollY", if (viewModel!!.isExpanded) 0 else threadHeight)
                            .setDuration(THREAD_DURATION).start()
                } else {
                    if (viewModel!!.isLoaded) {
                        binding.rlDetailMain.animate().setDuration(THREAD_DURATION).translationY(threadHeight.toFloat()).start()
                        binding.recyclerDetailConversation.translationY = (-threadHeight).toFloat()
                        binding.recyclerDetailConversation.animate().setDuration(THREAD_DURATION).translationY(0f).start()
                        changeThreadReadStatus()
                        isOpen = true
                    }
                }
            }

            override fun onClientClick(v: View) {
                var link = AppData.CURRENT_STATUS_MODEL.source.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                link = link.substring(6, link.length - 1)
                Utilities.openLink(link, this@DetailActivity)
            }

            override fun onFollowClick(v: View) {

            }

            override fun onQuoteMediaClick(v: View) {
                performContent(AppData.CURRENT_STATUS_MODEL.quotedStatus)
            }
        }

        binding.txtDetailStatus.addAutoLinkMode(
                AutoLinkMode.MODE_HASHTAG,
                AutoLinkMode.MODE_MENTION,
                AutoLinkMode.MODE_URL,
                AutoLinkMode.MODE_SHORT
        )

        val isNight = App.getInstance().isNightEnabled
        binding.txtDetailStatus.setHashtagModeColor(ContextCompat.getColor(applicationContext, if (isNight)
            R.color.dark_tag_color
        else
            R.color.light_tag_color))
        binding.txtDetailStatus.setMentionModeColor(ContextCompat.getColor(applicationContext, if (isNight)
            R.color.dark_highlight_color
        else
            R.color.light_highlight_color))
        binding.txtDetailStatus.setUrlModeColor(ContextCompat.getColor(applicationContext, if (isNight)
            R.color.dark_highlight_color
        else
            R.color.light_highlight_color))
        binding.txtDetailStatus.setSelectedStateColor(ContextCompat.getColor(applicationContext, if (isNight)
            R.color.dark_secondary_text_color
        else
            R.color.light_secondary_text_color))

        val urls = arrayOfNulls<String>(currentStatus.urlEntities.size())
        for (i in 0 until currentStatus.urlEntities.size()) {
            urls[i] = (currentStatus.urlEntities.get(0) as JsonObject).get("displayURL").asString
        }

        binding.txtDetailStatus.shortUrls = urls
        binding.txtDetailStatus.setAutoLinkText(currentStatus.text)
        binding.txtDetailStatus.setAutoLinkOnClickListener(object : AutoLinkOnClickListener {
            override fun onAutoLinkTextClick(autoLinkMode: AutoLinkMode, matchedText: String) {
                when (autoLinkMode) {
                    AutoLinkMode.MODE_HASHTAG -> {
                        AppData.searchQuery = matchedText
                        mActivity!!.startActivity(Intent(applicationContext, SearchActivity::class.java))
                        mActivity!!.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                    AutoLinkMode.MODE_URL -> Utilities.openLink(matchedText.trim(), this@DetailActivity)
                    AutoLinkMode.MODE_MENTION -> {
                        val profileIntent = Intent(applicationContext, MVPProfileActivity::class.java)
                        profileIntent.putExtra(Flags.PROFILE_SCREEN_NAME, matchedText)
                        this@DetailActivity.startActivity(profileIntent)
                        this@DetailActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                    AutoLinkMode.MODE_SHORT -> currentStatus.urlEntities
                            .asSequence()
                            .filter { it.asJsonObject.get("displayURL").asString == matchedText }
                            .forEach { Utilities.openLink(it.asJsonObject.get("expandedURL").asString, mActivity) }
                }
            }

            override fun onAutoLinkLongTextClick(autoLinkMode: AutoLinkMode, matchedText: String) {
                Log.e(TAG, "autolink long click - " + matchedText)
            }
        })

        if (!detailReplyFragment.isAdded)
            supportFragmentManager.beginTransaction().add(R.id.fl_detail,
                    detailReplyFragment, detailReplyFragment.tag).commit()
        supportFragmentManager.beginTransaction().hide(detailReplyFragment).commit()
        if (!detailRtFragment.isAdded)
            supportFragmentManager.beginTransaction().add(R.id.fl_detail,
                    detailRtFragment, detailRtFragment.tag).commit()

        lastFragment = detailRtFragment
        binding.vpDetail.currentItem = 1

        detailRtFragment.loadApi()
        detailReplyFragment.loadApi()
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left)
    }

    override fun createTabView(container: ViewGroup, position: Int, adapter: PagerAdapter): View {
        val inflater = LayoutInflater.from(container.context)
        val res = container.context.resources
        val tab = inflater.inflate(R.layout.tab_item, container, false)
        val imageView = tab.findViewById<View>(R.id.tab_iv) as ImageView
        val textView = tab.findViewById<View>(R.id.tab_txt) as TextView
        val layoutView = tab.findViewById<View>(R.id.tab_layout) as RelativeLayout

        textView.setTextColor(res.getColor(if (App.getInstance().isNightEnabled)
            R.color.dark_primary_text_color
        else
            R.color.light_primary_text_color))
        layoutView.background = if (CURRENT_POSITION == position)
            res.getDrawable(if (App.getInstance().isNightEnabled) R.drawable.tab_shape_dark else R.drawable.tab_shape_light)
        else
            res.getDrawable(R.drawable.tab_shape_transparent)

        val isNightEnabled = App.getInstance().isNightEnabled
        when (position) {
            0 -> {
                textView.text = 0.toString()
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_tiny_reply))
                imageView.setColorFilter(res.getColor(if (isNightEnabled)
                    R.color.dark_reply_tint_color
                else
                    R.color.light_reply_tint_color))

                mTxtReply = textView
                mImgReply = imageView
            }

            1 -> {
                textView.text = 0.toString()
                imageView.setImageDrawable(res.getDrawable(R.drawable.ic_icons_tiny_rt))
                imageView.setColorFilter(res.getColor(if (isNightEnabled)
                    R.color.dark_rt_tint_color
                else
                    R.color.light_rt_tint_color))

                mTxtRt = textView
                mImgRt = imageView
            }
        }

        imageView.alpha = 0.3f
        textView.alpha = 0.3f

        return tab
    }

    private fun changeThreadReadStatus() {
        changeArrowStatus()
        viewModel!!.twitterName = "@" + AppData.CURRENT_STATUS_MODEL.user.screenName
        viewModel!!.isShowThread = true
    }

    private fun changeArrowStatus() {
        val valueAnimator = if (!viewModel!!.isExpanded) ValueAnimator.ofInt(19, 0) else ValueAnimator.ofInt(0, 19)
        valueAnimator.duration = THREAD_DURATION
        valueAnimator.addUpdateListener { animation -> ImageAnimation.setupDisclosureArrow(binding.imgDetailArrow, animation.animatedValue as Int) }
        valueAnimator.start()
        viewModel!!.isExpanded = !viewModel!!.isExpanded
    }

    private fun selectFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        if (fragment.isAdded) {
            fragmentTransaction.hide(lastFragment)
                    .show(fragment).commit()
        }

        lastFragment = fragment
    }

    private fun setupStatus(scrollY: Int) {
        //        int threadDiffHeight = viewModel.isExpanded() ? threadHeight : 0;
        val diff = (Utilities.convertDpToPixel(TOOLBAR_MARGIN.toFloat(), applicationContext) + threadHeight
                - scrollY.toFloat() - Utilities.convertDpToPixel(24f, applicationContext)).toInt()
        val init = (Utilities.convertDpToPixel(TOOLBAR_MARGIN.toFloat(), applicationContext) + threadHeight - Utilities.convertDpToPixel(24f, applicationContext)).toInt()
        val shadowHeight = Utilities.convertDpToPixel(4f, applicationContext).toInt()
        val step = (init - diff) / shadowHeight - shadowHeight

        if (diff <= 0) {
            if (viewModel!!.isExpanded) {
                viewModel!!.isExpanded = false
                val valueAnimator = ValueAnimator.ofInt(0, 19)
                valueAnimator.duration = THREAD_DURATION
                valueAnimator.addUpdateListener { animation -> ImageAnimation.setupDisclosureArrow(binding.imgDetailArrow, animation.animatedValue as Int) }
                valueAnimator.start()
            }

            binding.llDetailStatus.animate().translationY((-diff).toFloat()).setDuration(0).start()
            binding.viewDetailShadow.animate().translationY((-diff).toFloat()).setDuration(0).start()
        } else {
            binding.llDetailStatus.animate().translationY(0f).setDuration(0).start()
            binding.viewDetailShadow.animate().translationY(0f).setDuration(0).start()
        }

        if (isShadow && diff > 0 || !isShadow && diff <= 0) {
            isShadow = !isShadow
            val valueAnimator = ValueAnimator.ofInt(if (diff > 0)
                Utilities.convertDpToPixel(4f, applicationContext).toInt()
            else
                0,
                    if (diff > 0) 0 else Utilities.convertDpToPixel(4f, applicationContext).toInt())
            valueAnimator.addUpdateListener { animation ->
                binding.viewDetailShadow.layoutParams.height = animation.animatedValue as Int
                binding.viewDetailShadow.requestLayout()
            }
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.duration = 150
            valueAnimator.start()
        }
    }

    private fun setupTabs(scrollY: Int) {
        tabY = if (isChange) tabY else binding.stbDetail.y.toInt()
        val diff = -statusHeight + tabY + threadHeight - scrollY

        if (diff < 0) {
            isChange = true
            binding.stbDetail.animate().translationY((-diff).toFloat()).setDuration(0).start()
        } else {
            binding.stbDetail.animate().translationY(0f).setDuration(0).start()
        }
    }

    private fun performContent(statusModel: StatusModel) {
        if (statusModel.mediaEntities.size() > 0) {
            val type = statusModel.mediaEntities.get(0).asJsonObject.get("type").asString
            when (type) {
                Flags.MEDIA_GIF -> {
                    AppData.MEDIA_URL = statusModel.mediaEntities.get(0).asJsonObject
                            .get("videoVariants").asJsonArray.get(0).asJsonObject.get("url").asString
                    AppData.MEDIA_TYPE = Flags.MEDIA_TYPE.GIF
                    mActivity!!.startActivity(Intent(applicationContext, MediaActivity::class.java))
                }
                Flags.MEDIA_VIDEO -> {
                    AppData.MEDIA_TYPE = Flags.MEDIA_TYPE.VIDEO
                    AppData.MEDIA_URL = statusModel.mediaEntities.get(0).asJsonObject
                            .get("videoVariants").asJsonArray.get(0).asJsonObject.get("url").asString
                    mActivity!!.startActivity(Intent(applicationContext, MediaActivity::class.java))
                }
                Flags.MEDIA_PHOTO -> {
                    val urls = statusModel.mediaEntities
                            .asSequence()
                            .map { it as JsonObject }
                            .mapTo(ArrayList<String>()) { it.get("mediaURLHttps").asString }

                    val imageActionsOverlay = ImageActionsOverlay(mActivity, urls, statusModel, 0)
                    imageActionsOverlay.imageActionsOverlayClickHandler = object : ImageActionsOverlay.ImageActionsOverlayClickHandler {
                        override fun onBackClick(v: View) {
                            imageActionsOverlay.imageViewer.onDismiss()
                        }

                        override fun onSaveClick(v: View, url: String) {
                            val downloadFiles = DownloadFiles(mActivity)
                            downloadFiles.saveFile(url, mActivity!!.getString(R.string.download_url))
                        }

                        override fun onShareImageClick(v: View, url: String) {

                        }

                        override fun onReplyClick(v: View) {
                            binding.click?.onReplyClick(v)
                            imageActionsOverlay.imageViewer.onDismiss()
                        }

                        override fun onRtClick(v: View) {
                            TweetActions.morePopup(mActivity, v, statusModel) { }
                        }

                        override fun onLikeClick(v: View) {
                            statusModel.isFavorited = !statusModel.isFavorited
                            imageActionsOverlay.isFavorited = statusModel.isFavorited
                            imageActionsOverlay.changeFavorited()
                            TweetActions.favorite(statusModel.isFavorited, statusModel.id) {
                                statusModel.isFavorited = !statusModel.isFavorited
                                imageActionsOverlay.isFavorited = statusModel.isFavorited
                                imageActionsOverlay.changeFavorited()
                            }
                        }

                        override fun onShareClick(v: View) {
                            binding.click?.onShareClick(v)
                        }

                        override fun onMoreClick(v: View) {
                            binding.click?.onMoreClick(v)
                        }
                    }
                }
            }
        }
    }

    /**
     * Setup slide back function to whole screen
     */

    private fun setupSlideBack() {
        binding.slDetail.showMode = SwipeLayout.ShowMode.LayDown
        binding.slDetail.addDrag(SwipeLayout.DragEdge.Left, binding.viewBottomLeft)
        binding.slDetail.addDrag(SwipeLayout.DragEdge.Right, binding.viewBottomRight)
        binding.slDetail.addSwipeListener(object : SwipeLayout.SwipeListener {
            override fun onStartOpen(layout: SwipeLayout) {}

            override fun onOpen(layout: SwipeLayout) {
                finish()
                overridePendingTransition(0, 0)
            }

            override fun onStartClose(layout: SwipeLayout) {}

            override fun onClose(layout: SwipeLayout) {}

            override fun onUpdate(layout: SwipeLayout, leftOffset: Int, topOffset: Int) {
                val alpha = 0.7f - leftOffset / Utilities.convertDpToPixel(250f, applicationContext)
                binding.viewBottomLeft.alpha = alpha
            }

            override fun onHandRelease(layout: SwipeLayout, xvel: Float, yvel: Float) {}
        })
    }

    /**
     * Setup status bar color
     */
    private fun setupStatusBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        Utilities.setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = resources.getColor(android.R.color.transparent)
        binding.viewStatusBarScrim.setBackgroundColor(resources.getColor(if (App.getInstance().isNightEnabled)
            R.color.dark_background_color
        else
            R.color.light_status_bar_detail_color))

        if (AppData.CURRENT_STATUS_MODEL.inReplyToStatusId > -1)
            binding.tbDetail.setBackgroundColor(resources.getColor(if (App.getInstance().isNightEnabled)
                R.color.dark_background_detail_highlight_color
            else
                R.color.light_background_detail_highlight_color))
    }

    /**
     * Setup quote if it exist
     */
    private fun checkQuote() {
        if (AppData.CURRENT_STATUS_MODEL.quotedStatus != null) {
            binding.txtDetailQuoteCreatedAt.text = DateConverter(applicationContext)
                    .parseTime(LocalDateTime(AppData.CURRENT_STATUS_MODEL
                            .quotedStatus.createdAt))

            binding.txtDetailQuoteTitle.text = AppData.CURRENT_STATUS_MODEL.quotedStatus.user.name
            binding.txtDetailQuoteText.text = AppData.CURRENT_STATUS_MODEL.quotedStatus.text

            //            Dont delete that
            //            for (JsonElement jsonElement : AppData.CURRENT_STATUS_MODEL.getQuotedStatus().getMediaEntities()) {
            //                binding.txtDetailQuoteText.setText(binding.txtDetailQuoteText.getText().toString() + "\n" +
            //                        jsonElement.getAsJsonObject().get("mediaURL").getAsString());
            //            }

            if (AppData.CURRENT_STATUS_MODEL.quotedStatus.mediaEntities.size() > 0) {
                val type = AppData.CURRENT_STATUS_MODEL.quotedStatus.mediaEntities.get(0)
                        .asJsonObject.get("type").asString

                when (type) {
                    Flags.MEDIA_PHOTO -> binding.imgDetailQuoteBadge.visibility = View.GONE
                    Flags.MEDIA_GIF -> {
                        binding.imgDetailQuoteBadge.visibility = View.VISIBLE
                        binding.imgDetailQuoteBadge.setImageDrawable(getDrawable(R.drawable.ic_badges_media_gif))
                    }
                    Flags.MEDIA_VIDEO -> {
                        binding.imgDetailQuoteBadge.visibility = View.VISIBLE
                        binding.imgDetailQuoteBadge.setImageDrawable(getDrawable(R.drawable.ic_badges_media_video))
                    }
                }

                Picasso.with(applicationContext)
                        .load(AppData.CURRENT_STATUS_MODEL.quotedStatus.mediaEntities.get(0)
                                .asJsonObject.get("mediaURL").asString)
                        .resize(Utilities.convertDpToPixel(64f, applicationContext).toInt(),
                                Utilities.convertDpToPixel(64f, applicationContext).toInt())
                        .centerCrop()
                        .transform(CirclePicasso(
                                Utilities.convertDpToPixel(4f, applicationContext),
                                Utilities.convertDpToPixel(0.5f, applicationContext),
                                25, R.color.black))
                        .into(binding.imgDetailQuoteImage)

                if (AppData.CURRENT_STATUS_MODEL.quotedStatus.mediaEntities.size() > 1) {
                    binding.detailQuoteImgMediaCount.visibility = View.VISIBLE
                    binding.detailQuoteTxtMediaCount.visibility = View.VISIBLE
                    binding.detailQuoteTxtMediaCount.text = AppData.CURRENT_STATUS_MODEL.quotedStatus.mediaEntities.size().toString()
                } else {
                    binding.detailQuoteImgMediaCount.visibility = View.INVISIBLE
                    binding.detailQuoteTxtMediaCount.visibility = View.INVISIBLE
                }
            } else {
                binding.detailFlQuoteMedia.visibility = View.GONE
                binding.imgDetailQuoteBadge.visibility = View.GONE
                binding.detailQuoteImgMediaCount.visibility = View.INVISIBLE
                binding.detailQuoteTxtMediaCount.visibility = View.INVISIBLE
            }
        }
    }

    /* Measurements section */

    fun measureThreadHeight() {
        binding.recyclerDetailConversation.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.recyclerDetailConversation.viewTreeObserver.removeOnGlobalLayoutListener(this)
                Handler().post {
                    threadHeight = binding.recyclerDetailConversation.height
                    val scrollHeight = binding.scrollDetail.getChildAt(0).height
                    val screenHeight = Utilities.getScreenHeight(mActivity)
                    val offset = ((Utilities.convertDpToPixel(24f, applicationContext) + screenHeight.toFloat()
                            + threadHeight.toFloat()) - scrollHeight).toInt()
                    viewModel!!.dummyHeight = if (offset < 0) 0 else offset
                }
            }
        })
    }

    fun setReplyCount(replyCount: Long) {
        this.replyCount = replyCount
        mTxtReply.alpha = if (replyCount == 0L) 0.3f else 1.0f
        mImgReply.alpha = if (replyCount == 0L) 0.3f else 1.0f
        mTxtReply.text = replyCount.toString()
    }

    fun setRtCount(rtCount: Long) {
        this.rtCount = rtCount
        mTxtRt.alpha = if (rtCount == 0L) 0.3f else 1.0f
        mImgRt.alpha = if (rtCount == 0L) 0.3f else 1.0f
        mTxtRt.text = rtCount.toString()
    }

    companion object {
        private val THREAD_DURATION: Long = 300
    }
}
