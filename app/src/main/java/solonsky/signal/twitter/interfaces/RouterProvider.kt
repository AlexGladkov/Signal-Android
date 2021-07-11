package solonsky.signal.twitter.interfaces

import com.github.terrakok.cicerone.Router


/**
 * Created by agladkov on 11.01.18.
 */
interface RouterProvider {
    fun getRouter(): Router
}
