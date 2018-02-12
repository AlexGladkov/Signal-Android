package solonsky.signal.twitter.di.modules

import dagger.Module
import dagger.Provides
import solonsky.signal.twitter.helpers.LocalCiceroneHolder
import javax.inject.Singleton

/**
 * Created by agladkov on 12.02.18.
 */
@Module
class LocalNavigationModule {

    @Provides
    @Singleton
    fun provideLocalNavigationHolder(): LocalCiceroneHolder {
        return LocalCiceroneHolder()
    }
}