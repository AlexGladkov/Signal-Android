package solonsky.signal.twitter.fragments

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

import solonsky.signal.twitter.R
import solonsky.signal.twitter.adapters.DetailStaggeredAdapter
import solonsky.signal.twitter.adapters.ImageAdapter
import solonsky.signal.twitter.adapters.MediaStaggeredAdapter
import solonsky.signal.twitter.api.ProfileDataApi
import solonsky.signal.twitter.databinding.FragmentProfileMediaBinding
import solonsky.signal.twitter.helpers.AppData
import solonsky.signal.twitter.helpers.Flags
import solonsky.signal.twitter.helpers.ListConfig
import solonsky.signal.twitter.helpers.Utilities
import solonsky.signal.twitter.interfaces.UpdateHandler
import solonsky.signal.twitter.libs.DownloadFiles
import solonsky.signal.twitter.models.ImageModel
import solonsky.signal.twitter.overlays.ImageOverlay
import solonsky.signal.twitter.viewmodels.ProfileMediaViewModel

/**
 * Created by neura on 27.05.17.
 */

class ProfileMediaFragment : Fragment() {
    private val TAG = ProfileMediaFragment::class.java.simpleName
    private var mediaArray = ArrayList<ImageModel>()
    private var imageAdapter: MediaStaggeredAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.e(TAG, "mediaArray ${mediaArray.size}")
        val binding = DataBindingUtil.inflate<FragmentProfileMediaBinding>(inflater!!, R.layout.fragment_profile_media, container, false)
        imageAdapter = MediaStaggeredAdapter(mediaArray,
                activity as AppCompatActivity, MediaStaggeredAdapter.ImageStaggeredListener { imageModel, _ ->

            Log.e(TAG, "image clicked ${imageModel.imageUrl}")
            val urls = mediaArray
                    .asSequence()
                    .filter { it.imageUrl != null }
                    .mapTo(ArrayList<String>()) { it.imageUrl }

            val imageOverlay = ImageOverlay(urls, activity as AppCompatActivity, mediaArray.indexOf(imageModel))
            imageOverlay.setImageOverlayClickHandler(object : ImageOverlay.ImageOverlayClickHandler {
                override fun onBackClick(v: View) {
                    imageOverlay.imageViewer.onDismiss()
                }

                override fun onSaveClick(v: View, url: String) {
                    val downloadFiles = DownloadFiles(activity)
                    downloadFiles.saveFile(url, activity.getString(R.string.download_url))
                }
            })
        })

        val viewModel = ProfileMediaViewModel(context)
        binding.model = viewModel

        val manager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                    if (position == 0) if (ProfileDataApi.getInstance().images.size % 2 == 0) 1 else 2 else 1
        }

        binding.recyclerProfileMedia.setHasFixedSize(true)
        binding.recyclerProfileMedia.isNestedScrollingEnabled = false
        binding.recyclerProfileMedia.layoutManager = manager
        binding.recyclerProfileMedia.adapter = imageAdapter
        binding.recyclerProfileMedia.addItemDecoration(ListConfig.SpacesItemDecoration(Utilities.convertDpToPixel(2f, context).toInt()))

        viewModel.state = if (mediaArray.size == 0)
            AppData.UI_STATE_NO_ITEMS
        else
            AppData.UI_STATE_VISIBLE
        return binding.root
    }

    fun setupMedia(mediaArray: ArrayList<ImageModel>) {
        this.mediaArray.clear()
        this.mediaArray.addAll(mediaArray)
        imageAdapter?.notifyDataSetChanged()
    }
}
