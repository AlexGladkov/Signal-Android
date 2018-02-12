package solonsky.signal.twitter.di;

import javax.inject.Singleton;

import dagger.Component;
import solonsky.signal.twitter.activities.LoggedActivity;
import solonsky.signal.twitter.activities.MVPSearchActivity;
import solonsky.signal.twitter.di.modules.LocalNavigationModule;
import solonsky.signal.twitter.di.modules.NavModule;
import solonsky.signal.twitter.interfaces.RouterProvider;
import solonsky.signal.twitter.presenters.SearchPresenter;

/**
 * Created by sunwi on 08.01.2018.
 * Injection base
 */
@Singleton
@Component(modules = { NavModule.class, LocalNavigationModule.class })
public interface AppComponent {

    // Activities
    void inject(MVPSearchActivity activity);
    void inject(LoggedActivity activity);

    // Presenters
    void inject(SearchPresenter presenter);
}
