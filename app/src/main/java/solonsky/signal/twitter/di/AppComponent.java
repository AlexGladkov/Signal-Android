package solonsky.signal.twitter.di;

import javax.inject.Singleton;

import dagger.Component;
import solonsky.signal.twitter.activities.MVPSearchActivity;
import solonsky.signal.twitter.di.modules.NavigationModule;
import solonsky.signal.twitter.presenters.SearchPresenter;

/**
 * Created by sunwi on 08.01.2018.
 * Injection base
 */
@Singleton
@Component(modules = { NavigationModule.class })
public interface AppComponent {

    // Activities
    void inject(MVPSearchActivity activity);

    // Presenters
    void inject(SearchPresenter presenter);
}
