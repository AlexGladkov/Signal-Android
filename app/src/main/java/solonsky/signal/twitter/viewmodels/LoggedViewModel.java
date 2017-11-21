package solonsky.signal.twitter.viewmodels;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.R;
import solonsky.signal.twitter.adapters.SelectorAdapter;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import solonsky.signal.twitter.helpers.ListConfig;

/**
 * Created by neura on 22.05.17.
 */

public class LoggedViewModel extends BaseObservable {
    private final Context context;
    private ListConfig userConfig;
    private String title;
    private String avatar;
    private Flags.STATUS_TYPE statusType;
    private boolean isStaticToolbar = false;
    private boolean isStaticBottomBar = false;
    private boolean isSearch = false;
    private boolean isAdding = false;
    private int feedCount;
    private int toolbarState;



    public interface LoggedClickHandler {
        void onComposeClick(View v);
        void onUserClick(View v);
        void onUpdateClick(View v);
        void onCancelClick(View v);
        void onSettingsClick(View v);
        void onAddClick(View v);
        void onMuteAddClick(View v);
        void onDMClick(View v);
        void onBackSearchClick(View v);
    }

    public LoggedViewModel(SelectorAdapter mAdapter, Context context, int feedCount) {
        this.feedCount = feedCount;
        this.context = context;
        this.avatar = "";
        this.title = context.getString(R.string.title_feed);
        this.toolbarState = AppData.TOOLBAR_LOGGED_MAIN;
        this.statusType = Flags.STATUS_TYPE.TITLE;

        this.userConfig = new ListConfig.Builder(mAdapter)
                .setHasFixedSize(true)
                .setHasNestedScroll(false)
                .setDefaultDividerEnabled(true)
                .build(context);
    }

    @Bindable
    public boolean isAdding() {
        return isAdding;
    }
    public void setAdding(boolean adding) {
        isAdding = adding;
        notifyPropertyChanged(BR.adding);
    }

    public boolean isSearch() {
        return isSearch;
    }

    public void setSearch(boolean search) {
        isSearch = search;
    }

    @Bindable
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
        notifyPropertyChanged(BR.avatar);
    }

    @Bindable
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public int getFeedCount() {
        return feedCount;
    }
    public void setFeedCount(int feedCount) {
        this.feedCount = feedCount;
        notifyPropertyChanged(BR.feedCount);
    }

    @Bindable
    public ListConfig getUserConfig() {
        return userConfig;
    }
    public void setUserConfig(ListConfig userConfig) {
        this.userConfig = userConfig;
        notifyPropertyChanged(BR.userConfig);
    }

    @Bindable
    public int getToolbarState() {
        return toolbarState;
    }
    public void setToolbarState(int toolbarState) {
        this.toolbarState = toolbarState;
        notifyPropertyChanged(BR.toolbarState);
    }

    @Bindable
    public Flags.STATUS_TYPE getStatusType() {
        return statusType;
    }

    public void setStatusType(Flags.STATUS_TYPE statusType) {
        this.statusType = statusType;
        notifyPropertyChanged(BR.statusType);
    }

    public boolean isStaticToolbar() {
        return isStaticToolbar;
    }

    public void setStaticToolbar(boolean staticToolbar) {
        isStaticToolbar = staticToolbar;
    }

    public boolean isStaticBottomBar() {
        return isStaticBottomBar;
    }

    public void setStaticBottomBar(boolean staticBottomBar) {
        isStaticBottomBar = staticBottomBar;
    }
}
