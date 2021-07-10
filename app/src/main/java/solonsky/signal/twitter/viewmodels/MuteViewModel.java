package solonsky.signal.twitter.viewmodels;

import android.content.Context;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.adapters.RemoveAdapter;
import solonsky.signal.twitter.helpers.ListConfig;

/**
 * Created by neura on 26.05.17.
 */

public class MuteViewModel extends BaseObservable {
    private boolean hasUsers;
    private boolean hasKeywords;
    private boolean hasHashs;
    private boolean hasClients;

    private ListConfig usersConfig;
    private ListConfig keywordsConfig;
    private ListConfig hashtagsConfig;
    private ListConfig clientsConfig;

    public MuteViewModel(RemoveAdapter usersAdapter, RemoveAdapter keywordsAdapter,
                         RemoveAdapter hashtagsAdapter, RemoveAdapter clientsAdapter, Context context) {
        this.usersConfig = new ListConfig.Builder(usersAdapter)
                .setDefaultDividerEnabled(true)
                .setLayoutManagerProvider(new ListConfig.SimpleLinearLayoutManagerProvider())
                .setHasFixedSize(true)
                .setHasNestedScroll(false)
                .build(context);

        this.keywordsConfig = new ListConfig.Builder(keywordsAdapter)
                .setDefaultDividerEnabled(true)
                .setLayoutManagerProvider(new ListConfig.SimpleLinearLayoutManagerProvider())
                .setHasFixedSize(true)
                .setHasNestedScroll(false)
                .build(context);

        this.hashtagsConfig = new ListConfig.Builder(hashtagsAdapter)
                .setDefaultDividerEnabled(true)
                .setLayoutManagerProvider(new ListConfig.SimpleLinearLayoutManagerProvider())
                .setHasFixedSize(true)
                .setHasNestedScroll(false)
                .build(context);

        this.clientsConfig = new ListConfig.Builder(clientsAdapter)
                .setDefaultDividerEnabled(true)
                .setLayoutManagerProvider(new ListConfig.SimpleLinearLayoutManagerProvider())
                .setHasFixedSize(true)
                .setHasNestedScroll(false)
                .build(context);

        this.hasClients = false;
        this.hasKeywords = false;
        this.hasHashs = false;
        this.hasUsers = false;
    }

    @Bindable
    public ListConfig getUsersConfig() {
        return usersConfig;
    }
    public void setUsersConfig(ListConfig usersConfig) {
        this.usersConfig = usersConfig;
        notifyPropertyChanged(BR.usersConfig);
    }

    @Bindable
    public ListConfig getKeywordsConfig() {
        return keywordsConfig;
    }
    public void setKeywordsConfig(ListConfig keywordsConfig) {
        this.keywordsConfig = keywordsConfig;
        notifyPropertyChanged(BR.keywordsConfig);
    }

    @Bindable
    public ListConfig getHashtagsConfig() {
        return hashtagsConfig;
    }
    public void setHashtagsConfig(ListConfig hashtagsConfig) {
        this.hashtagsConfig = hashtagsConfig;
        notifyPropertyChanged(BR.hashtagsConfig);
    }

    @Bindable
    public ListConfig getClientsConfig() {
        return clientsConfig;
    }
    public void setClientsConfig(ListConfig clientsConfig) {
        this.clientsConfig = clientsConfig;
        notifyPropertyChanged(BR.clientsConfig);
    }

    @Bindable
    public boolean isHasUsers() {
        return hasUsers;
    }
    public void setHasUsers(boolean hasUsers) {
        this.hasUsers = hasUsers;
        notifyPropertyChanged(BR.hasUsers);
    }

    @Bindable
    public boolean isHasKeywords() {
        return hasKeywords;
    }
    public void setHasKeywords(boolean hasKeywords) {
        this.hasKeywords = hasKeywords;
        notifyPropertyChanged(BR.hasKeywords);
    }

    @Bindable
    public boolean isHasHashs() {
        return hasHashs;
    }
    public void setHasHashs(boolean hasHashs) {
        this.hasHashs = hasHashs;
        notifyPropertyChanged(BR.hasHashs);
    }

    @Bindable
    public boolean isHasClients() {
        return hasClients;
    }
    public void setHasClients(boolean hasClients) {
        this.hasClients = hasClients;
        notifyPropertyChanged(BR.hasClients);
    }
}
