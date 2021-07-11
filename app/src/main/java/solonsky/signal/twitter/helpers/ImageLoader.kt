package solonsky.signal.twitter.helpers

import android.widget.ImageView
import com.squareup.picasso.Picasso
import com.stfalcon.frescoimageviewer.ImageViewer

/**
 * Created by neura on 10.02.18.
 */
class ImageLoader {

    companion object {
        fun loadImage(url: String?, imageView: ImageView?) {
            if (url == null || imageView == null || url == "") return
            Picasso.get().load(url).into(imageView)
        }
    }
}