package solonsky.signal.twitter.presenters

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.view.MenuItem
import android.widget.PopupMenu
import com.github.terrakok.cicerone.Router
import com.github.terrakok.cicerone.androidx.FragmentScreen
import com.google.gson.JsonObject
import moxy.InjectViewState
import moxy.MvpPresenter
import solonsky.signal.twitter.R
import solonsky.signal.twitter.fragments.SearchAllFragment
import solonsky.signal.twitter.fragments.SearchHomeFragment
import solonsky.signal.twitter.fragments.SearchMediaFragment
import solonsky.signal.twitter.fragments.SearchPeopleFragment
import solonsky.signal.twitter.helpers.*
import solonsky.signal.twitter.models.NotificationDetailModel
import solonsky.signal.twitter.models.StatusModel
import solonsky.signal.twitter.models.User
import solonsky.signal.twitter.providers.SearchProvider
import solonsky.signal.twitter.views.SearchView
import java.util.*
import javax.inject.Inject

/**
 * Created by neura on 01.11.17.
 */

@InjectViewState
class SearchPresenter : MvpPresenter<SearchView>() {
    val TAG: String = MediaPresenter::class.java.simpleName
    private val searchProvider = SearchProvider(searchPresenter = this@SearchPresenter)
    private var currentPosition = 0

    private val searchedAll: MutableList<StatusModel> = ArrayList()
    private val searchedMedia: MutableList<StatusModel> = ArrayList()
    private val searchedHome: MutableList<StatusModel> = ArrayList()
    private val searchedUsers: MutableList<User> = ArrayList()

    private val sourceAll: MutableList<StatusModel> = ArrayList()
    private val sourceMedia: MutableList<StatusModel> = ArrayList()
    private val sourceUsers: MutableList<User> = ArrayList()
    private val sourceHome: MutableList<StatusModel> = ArrayList()

    private val handler = Handler()

    private var isAllLoaded = false
    private var isUsersLoaded = false
    private var isHomeLoaded = false

    private var allRetweets = true
    private var allPopular = true

    private var userFollowers = false
    private var userVerified = false

    private var mediaImages = true
    private var mediaVideo = true
    private var mediaLinks = true

    private var homeImages = true
    private var homeVideo = true
    private var homeLinks = true
    private var homeRetweets = true
    private var homeMentions = true

    @Inject
    lateinit var router: Router

    init {
        App.getInstance().appComponent.inject(this@SearchPresenter)
    }

    fun fetchRequest(intent: Intent?) {
        if (intent == null) {
            router.exit()
//            router.exitWithMessage("Internal error")
        } else {
            val query = intent.getStringExtra(Keys.SearchQuery.value)
            if (query == null || query == "") {
                router.exit()
//                router.exitWithMessage("Internal error")
            } else {
                viewState.setupSearch(query = query)
                searchProvider.fetchSearchData(searchQuery = query)
            }
        }
    }

    fun startSearch(query: String) {
        viewState.setupSearch(query = query)

        isAllLoaded = false
        isHomeLoaded = false
        isUsersLoaded = false

        searchedUsers.clear()
        searchedMedia.clear()
        searchedAll.clear()
        searchedHome.clear()

        switchPage(position = currentPosition)
        searchProvider.fetchSearchData(searchQuery = query)
    }

    fun switchPage(position: Int) {
        val bundle = Bundle()
        when (position) {
            0 -> {
                bundle.putParcelableArrayList(Keys.SearchList.value, searchedAll as ArrayList<StatusModel>)
                bundle.putBoolean(Keys.SearchLoaded.value, isAllLoaded)
                router.navigateTo(FragmentScreen{SearchAllFragment()})
            }
            1 -> {
                bundle.putParcelableArrayList(Keys.SearchList.value, searchedUsers as ArrayList<User>)
                bundle.putBoolean(Keys.SearchLoaded.value, isUsersLoaded)
                router.navigateTo(FragmentScreen{SearchPeopleFragment()})

//                router.navigateTo(ScreenKeys.SearchUsers.value, bundle)
            }
            2 -> {
                bundle.putParcelableArrayList(Keys.SearchList.value, searchedMedia as ArrayList<StatusModel>)
                bundle.putBoolean(Keys.SearchLoaded.value, isAllLoaded)
                router.navigateTo(FragmentScreen{SearchMediaFragment()})
//                router.navigateTo(ScreenKeys.SearchMedia.value, bundle)
            }
            3 -> {
                bundle.putParcelableArrayList(Keys.SearchList.value, searchedHome as ArrayList<StatusModel>)
                bundle.putBoolean(Keys.SearchLoaded.value, isHomeLoaded)
                router.navigateTo(FragmentScreen{SearchHomeFragment()})

//                router.navigateTo(ScreenKeys.SearchHome.value, bundle)
            }
        }

        currentPosition = position
    }

    fun showError(errorMessage: String) {
        viewState.showMessage(text = errorMessage)
    }

    fun loadMedia(dataList: List<StatusModel>) {
        searchedMedia.addAll(dataList)
        sourceMedia.addAll(dataList)
        if (currentPosition == 2) {
            switchPage(position = currentPosition)
        }
    }

    fun loadAll(dataList: List<StatusModel>) {
        searchedAll.addAll(dataList)
        sourceAll.addAll(dataList)
        isAllLoaded = true
        if (currentPosition == 0) {
            switchPage(position = currentPosition)
        }
    }

    fun loadHome(dataList: List<StatusModel>) {
        searchedHome.addAll(dataList)
        sourceHome.addAll(dataList)
        isHomeLoaded = true
        if (currentPosition == 3) {
            switchPage(position = currentPosition)
        }
    }

    fun loadUsers(dataList: List<User>) {
        searchedUsers.addAll(dataList)
        sourceUsers.addAll(dataList)
        isUsersLoaded = true
        if (currentPosition == 1) {
            switchPage(position = currentPosition)
        }
    }

    fun onBackClick() {
        viewState.performExit()
    }

    fun onMoreClick() {
        when (currentPosition) {
            0 -> viewState.openPopup(R.menu.menu_search_all)
            1 -> viewState.openPopup(R.menu.menu_search_people)
            2 -> viewState.openPopup(R.menu.menu_search_media)
            3 -> viewState.openPopup(R.menu.menu_search_home)
        }
    }

    private fun filterAll() {
        Thread({
            searchedAll.clear()
            sourceAll.forEach({
                if (allRetweets || !allRetweets && !it.isRetweet && (allPopular ||
                        !allPopular && it.retweetCount <= 1000)) {
                    searchedAll.add(it)
                }
            })

            handler.post {
                switchPage(position = currentPosition)
            }
        }).start()
    }

    private fun filterMedia() {
        Thread({
            searchedMedia.clear()
            sourceMedia.forEach({
                it.mediaEntities
                        .map { it as JsonObject }
                        .filter { media ->
                            (media.get("type").asString == "photo" ||
                                    media.get("type").asString == "animated-gif") && mediaImages ||
                                    media.get("type").asString == "video" && mediaVideo ||
                                    it.urlEntities.size() > 0 && mediaLinks
                        }
                        .forEach { _ -> searchedMedia.add(it) }

                if (it.urlEntities.size() > 0 && mediaLinks) {
                    searchedMedia.add(it)
                }
            })

            handler.post {
                switchPage(position = currentPosition)
            }
        }).start()
    }

    private fun filterHome() {
        Thread({
            searchedHome.clear()
            sourceHome.forEach({ status ->
                if (status.isRetweet && homeRetweets) {
                    searchedHome.add(status)
                    return@forEach
                }

                if (status.inReplyToStatusId == -1L && status.text.contains("@") && homeMentions) {
                    searchedHome.add(status)
                    return@forEach
                }

                if (status.mediaEntities.size() > 0) {
                    var isAdded = false
                    for (media in status.getMediaEntities()) {
                        val mediaEntity = media as JsonObject

                        if ((mediaEntity.get("type").asString == Flags.MEDIA_PHOTO ||
                                mediaEntity.get("type").asString == Flags.MEDIA_GIF) && homeImages) {
                            searchedHome.add(status)
                            isAdded = true
                            break
                        }

                        if (mediaEntity.get("type").asString == Flags.MEDIA_VIDEO && homeVideo) {
                            searchedHome.add(status)
                            isAdded = true
                            break
                        }
                    }

                    if (isAdded) return@forEach
                }

                if (status.urlEntities.size() > 0 && homeLinks) {
                    searchedHome.add(status)
                }
            })

            handler.post {
                switchPage(position = currentPosition)
            }
        }).start()
    }

    private fun filterUsers() {
        Thread({
            searchedUsers.clear()
            sourceUsers.forEach({
                if ((!userVerified || it.isVerified) && (!userFollowers || it.isFollowRequestSent)) {
                    searchedUsers.add(it)
                }
            })

            handler.post {
                switchPage(position = currentPosition)
            }
        }).start()
    }

    fun tunePopup(popupMenu: PopupMenu) {
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.search_include_retweets -> {
                    if (currentPosition == 0) {
                        allRetweets = !allRetweets
                        popupMenu.menu.getItem(1).isChecked = allRetweets
                        filterAll()
                    } else {
                        homeRetweets = !homeRetweets
                        popupMenu.menu.getItem(4).isChecked = homeRetweets
                        filterHome()
                    }
                }

                R.id.search_popular -> {
                    allPopular = !allPopular
                    popupMenu.menu.getItem(2).isChecked = allPopular
                    filterAll()
                }

                R.id.search_followers_only -> {
                    userFollowers = !userFollowers
                    popupMenu.menu.getItem(1).isChecked = userFollowers
                    filterUsers()
                }

                R.id.search_verified_only -> {
                    userVerified = !userVerified
                    popupMenu.menu.getItem(2).isChecked = userVerified
                    filterUsers()
                }

                R.id.search_images -> {
                    if (currentPosition == 2) {
                        mediaImages = !mediaImages
                        popupMenu.menu.getItem(1).isChecked = mediaImages
                        filterMedia()
                    } else {
                        homeImages = !homeImages
                        popupMenu.menu.getItem(1).isChecked = homeImages
                        filterHome()
                    }
                }

                R.id.search_video -> {
                    if (currentPosition == 2) {
                        mediaVideo = !mediaVideo
                        popupMenu.menu.getItem(2).isChecked = mediaVideo
                        filterMedia()
                    } else {
                        homeVideo = !homeVideo
                        popupMenu.menu.getItem(2).isChecked = homeVideo
                        filterHome()
                    }
                }

                R.id.search_links -> {
                    if (currentPosition == 2) {
                        mediaLinks = !mediaLinks
                        popupMenu.menu.getItem(3).isChecked = mediaLinks
                        filterMedia()
                    } else {
                        homeLinks = !homeLinks
                        popupMenu.menu.getItem(3).isChecked = homeLinks
                        filterHome()
                    }
                }

                R.id.search_include_mentions -> {
                    homeMentions = !homeMentions
                    popupMenu.menu.getItem(5).isChecked = homeMentions
                    filterHome()
                }
            }

            true
        }

        when (currentPosition) {
            0 -> {
                popupMenu.menu.getItem(1).isChecked = allRetweets
                popupMenu.menu.getItem(2).isChecked = allPopular
            }
            1 -> {
                popupMenu.menu.getItem(1).isChecked = userFollowers
                popupMenu.menu.getItem(2).isChecked = userVerified
            }
            2 -> {
                popupMenu.menu.getItem(1).isChecked = mediaImages
                popupMenu.menu.getItem(2).isChecked = mediaVideo
                popupMenu.menu.getItem(3).isChecked = mediaLinks
            }
            3 -> {
                popupMenu.menu.getItem(1).isChecked = homeImages
                popupMenu.menu.getItem(2).isChecked = homeVideo
                popupMenu.menu.getItem(3).isChecked = homeLinks
                popupMenu.menu.getItem(4).isChecked = homeRetweets
                popupMenu.menu.getItem(5).isChecked = homeMentions
            }
        }
    }
}