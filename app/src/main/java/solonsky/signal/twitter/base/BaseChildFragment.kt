package solonsky.signal.twitter.base

import android.content.Context
import android.os.Bundle
import android.view.animation.Animation
import com.arellomobile.mvp.MvpAppCompatFragment
import solonsky.signal.twitter.interfaces.BackButtonListener
import solonsky.signal.twitter.interfaces.RouterProvider
import solonsky.signal.twitter.views.FragmentView

/**
 * Created by agladkov on 11.01.18.
 * Use this for any child fragment in container (ex. ServiceFragment, ProfileFragment)
 */
open class BaseChildFragment: MvpAppCompatFragment(), BackButtonListener {
    var isAnimationDisabled: Boolean = false
    var isDisabledRecommended: Boolean = false
    private val TAG: String = BaseChildFragment::class.java.simpleName
    private var isFirstCreating: Boolean = true
    protected var mCallback: FragmentView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isFirstCreating = false
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (isAnimationDisabled) {
            val a = object: Animation() {}
            a.duration = 0
            return a
        }

        if (isDisabledRecommended) {
            isAnimationDisabled = true
            isDisabledRecommended = false
        }
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    override fun onResume() {
        isAnimationDisabled = false
        super.onResume()
    }

    override fun onPause() {
        isDisabledRecommended = true
        super.onPause()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        context?.let {
            if (it is FragmentView)
                mCallback = it
        }
    }

    override fun onDetach() {
        super.onDetach()
        mCallback = null
    }

    // MARK: - BackButtonListener implementation
    override fun onBackPressed(): Boolean {
        (parentFragment as RouterProvider).getRouter().exit()
        return true
    }
}
