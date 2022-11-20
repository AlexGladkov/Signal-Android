package solonsky.signal.twitter.models;

import android.view.View;


import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import org.joda.time.LocalDateTime;

import solonsky.signal.twitter.BR;
import solonsky.signal.twitter.helpers.AppData;
import solonsky.signal.twitter.helpers.Flags;
import twitter4j.DirectMessage;

/**
 * Created by neura on 23.05.17.
 */

public class DirectModel extends BaseObservable {
    private long id;
    private long otherId;
    private String iconUrl;
    private String username;
    private String lastMessage;
    private int messageCount;
    private LocalDateTime dateTime;
    private boolean isHighlighted;

    private int divideState = Flags.DIVIDER_SHORT;

    public interface DirectClickHandler {
        void onItemClick(View v);
    }

    public DirectModel(long id, long otherId, String iconUrl, String username, String lastMessage,
                       LocalDateTime dateTime, boolean isHighlighted) {
        this.id = id;
        this.otherId = otherId;
        this.iconUrl = iconUrl;
        this.username = username;
        this.messageCount = 0;
        this.lastMessage = lastMessage;
        this.dateTime = dateTime;
        this.isHighlighted = isHighlighted;
        this.divideState = AppData.DIVIDER_SHORT;
    }

    public static DirectModel getInstance(DirectMessage directMessage, Long myId) {
        long otherId = directMessage.getSenderId() == myId ? directMessage.getRecipientId() : directMessage.getSenderId();
        twitter4j.User user = directMessage.getSenderId() == myId ? directMessage.getRecipient() : directMessage.getSender();

        return new DirectModel(
                directMessage.getId(), otherId, user.getOriginalProfileImageURL(), user.getName(),
                directMessage.getText(), new LocalDateTime(directMessage.getCreatedAt()), true);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DirectModel)) return false;
        if (obj == this) return true;

        DirectModel directModel = (DirectModel) obj;
        return this.getOtherId() == directModel.getOtherId();
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public long getOtherId() {
        return otherId;
    }
    public void setOtherId(long otherId) {
        this.otherId = otherId;
    }

    @Bindable
    public String getIconUrl() {
        return iconUrl;
    }
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        notifyPropertyChanged(BR.iconUrl);
    }

    @Bindable
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
        notifyPropertyChanged(BR.username);
    }

    @Bindable
    public String getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
        notifyPropertyChanged(BR.lastMessage);
    }

    @Bindable
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
        notifyPropertyChanged(BR.dateTime);
    }

    @Bindable
    public boolean isHighlighted() {
        return isHighlighted;
    }
    public void setHighlighted(boolean highlighted) {
        isHighlighted = highlighted;
        notifyPropertyChanged(BR.highlighted);
    }

    @Bindable
    public int getDivideState() {
        return divideState;
    }

    public void setDivideState(int divideState) {
        this.divideState = divideState;
        notifyPropertyChanged(BR.divideState);
    }

    @Bindable
    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
        notifyPropertyChanged(BR.messageCount);
    }
}
